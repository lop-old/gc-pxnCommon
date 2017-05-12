package com.poixson.utils.xScheduler;

import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import com.poixson.utils.Keeper;
import com.poixson.utils.NumberUtils;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.xClock;
import com.poixson.utils.xStartable;
import com.poixson.utils.xTime;
import com.poixson.utils.xLogger.xLevel;
import com.poixson.utils.xLogger.xLog;


public class xScheduler implements xStartable {

	private static final String LOG_NAME        = "xSched";
	private static final String MAIN_SCHED_NAME = "main";

	private static final ConcurrentHashMap<String, xScheduler> instances =
			new ConcurrentHashMap<String, xScheduler>();

	private final String schedName;
	private final Set<xSchedulerTask> tasks = new CopyOnWriteArraySet<xSchedulerTask>();

	// manager thread
	private final Thread thread;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private volatile boolean stopping = false;

	// manager thread sleep
	private final xTime threadSleepTime = xTime.get("5s");
	private final double threadSleepInhibitPercent = 0.95;
	private volatile boolean sleeping = false;
	private volatile boolean changes  = false;



	public static xScheduler getMainSched() {
		return get(MAIN_SCHED_NAME);
	}
	public static xScheduler get(final String schedName) {
		final String name = (
			Utils.isBlank(schedName)
			? MAIN_SCHED_NAME
			: schedName
		);
		{
			final xScheduler sched = instances.get(name);
			if (sched != null)
				return sched;
		}
		synchronized (instances) {
			if (instances.containsKey(name))
				return instances.get(name);
			final xScheduler sched = new xScheduler(name);
			instances.put(
				name,
				sched
			);
			return sched;
		}

	}



	private xScheduler(final String schedName) {
		if (Utils.isBlank(schedName)) throw new IllegalArgumentException("shedName");
		this.schedName = schedName;
		this.thread = new Thread(this);
		this.thread.setDaemon(false);
		this.thread.setName(LOG_NAME);
		Keeper.add(this);
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}



	@Override
	public void Start() {
		if (this.stopping)    throw new RuntimeException("Scheduler already stopping");
		if (this.isRunning()) throw new RuntimeException("Scheduler already running");
		this.thread.start();
	}
	@Override
	public void Stop() {
		this.stopping = true;
		this.wakeManager();
	}



	// manager loop
	@Override
	public void run() {
		if (this.stopping)
			return;
		if (!this.running.compareAndSet(false, true))
			return;
		this.log().fine("Starting sched manager..");
		final long threadSleep = this.threadSleepTime.getMS();
		final Set<xSchedulerTask> finishedTasks = new HashSet<xSchedulerTask>();
		while (true) {
			if (this.stopping || !this.isRunning())
				break;
			long sleep = threadSleep;
			finishedTasks.clear();
			// check task triggers
			{
				final Iterator<xSchedulerTask> it = this.tasks.iterator();
				this.changes = false;
				final long now = getClockMillis();
				while (it.hasNext()) {
					final xSchedulerTask task = it.next();
					final long untilNext = task.untilSoonestTrigger(now);
					// disabled
					if (untilNext == Long.MIN_VALUE)
						continue;
					// trigger now
					if (untilNext <= 0L) {
						// clear thread interrupt
						Thread.interrupted();
						task.doTrigger();
						// mark for removal
						if (task.notRepeating()) {
							finishedTasks.add(task);
						}
						// running again soon
						if (task.untilSoonestTrigger(now) < 0L) {
							this.changes = true;
							continue;
						}
					}
					if (untilNext < sleep) {
						sleep = untilNext;
					}
				}
			}
			// remove finished tasks
			if (!finishedTasks.isEmpty()) {
				final Iterator<xSchedulerTask> it = finishedTasks.iterator();
				while (it.hasNext()) {
					final xSchedulerTask task = it.next();
					task.unregister();
					this.tasks.remove(task);
				}
			}
			// no sleep needed
			if (this.changes || sleep <= 0L)
				continue;
			// calculate sleep
			final long sleepLess = (
				sleep <= threadSleep
				? (long) Math.ceil( ((double) sleep) * this.threadSleepInhibitPercent )
				: threadSleep
			);
			// no sleep needed
			if (this.changes || sleep <= 0L)
				continue;
			// log sleep time
			if (this.log().isLoggable(xLevel.DETAIL)) {
				final double sleepLessSec = ((double)sleepLess) / 1000.0;
				log().finest(
					(new StringBuilder())
						.append("Sleeping.. ")
						.append(NumberUtils.FormatDecimal("0.000", sleepLessSec))
						.append('s')
						.toString()
				);
			}
			// sleep until next check
			this.sleeping = true;
			if (!this.changes) {
				ThreadUtils.Sleep(sleepLess);
			}
			this.sleeping = false;
		}
		finishedTasks.clear();
		log().fine("Stopped sched manager thread");
		this.stopping = true;
		this.running.set(false);
		Keeper.remove(this);
	}
	public void wakeManager() {
		this.changes = true;
		if (this.sleeping) {
			try {
				this.thread.interrupt();
			} catch (Exception ignore) {}
		}
	}



	@Override
	public boolean isRunning() {
		if (this.stopping)
			return false;
		return this.running.get();
	}
	@Override
	public boolean isStopping() {
		return this.stopping;
	}



	// ------------------------------------------------------------------------------- //
	// scheduler config



	// scheduler name
	public String getName() {
		return this.schedName;
	}



	// tasks
	public void add(final xSchedulerTask task) {
		this.tasks.add(task);
		this.wakeManager();
	}
//TODO:
/*
	public boolean hasTask(final String taskName) {
		if (Utils.isBlank(taskName))
			return false;
		final Iterator<xSchedulerTask> it = this.tasks.iterator();
		while (it.hasNext()) {
			final String name = it.next().getTaskName();
			if (taskName.equals(name))
				return true;
			if (it.next().taskNameEquals(taskName)) {
				return true;
			}
		}
		return false;
	}
*/



	public boolean cancel(final String taskName) {
		if (Utils.isBlank(taskName))
			return false;
		boolean found = false;
		final Iterator<xSchedulerTask> it = this.tasks.iterator();
		while (it.hasNext()) {
			final String name = it.next().getTaskName();
			if (taskName.equals(name)) {
				it.remove();
				found = true;
			}
		}
		return found;
	}
	public boolean cancel(final xSchedulerTask task) {
		if (task == null)
			return false;
		if (this.tasks.contains(task)) {
			task.unregister();
			return this.tasks.remove(task);
		}
		return false;
	}



	private static volatile SoftReference<xClock> _clock = null;

	public static long getClockMillis() {
		return getClock()
				.millis();
	}
	public static xClock getClock() {
		if (_clock != null) {
			final xClock clock = _clock.get();
			if (clock != null)
				return clock;
		}
		final xClock clock = xClock.get(false);
		_clock = new SoftReference<xClock>(clock);
		return clock;
	}



	// logger
	private volatile SoftReference<xLog> _log = null;
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null)
				return log;
		}
		final xLog log =
			xLog.getRoot()
				.get(LOG_NAME)
				.getWeak(this.getName());
		this._log = new SoftReference<xLog>(log);
		return log;
	}



}
