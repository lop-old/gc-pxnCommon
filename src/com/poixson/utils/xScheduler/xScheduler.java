package com.poixson.utils.xScheduler;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import com.poixson.utils.Keeper;
import com.poixson.utils.NumberUtils;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.xStartable;
import com.poixson.utils.xTime;
import com.poixson.utils.xLogger.xLevel;
import com.poixson.utils.xLogger.xLog;


public class xScheduler implements xStartable {

	private static final String MANAGER_THREAD_NAME = "xSched";
	private static final String MAIN_SCHED_NAME     = "main";

	private static final ConcurrentHashMap<String, xScheduler> instances =
			new ConcurrentHashMap<String, xScheduler>();

	private final String schedName;
	private final Set<xSchedulerTask> tasks = new CopyOnWriteArraySet<xSchedulerTask>();

	private final xTime threadSleepTime = xTime.get("5s");

	// manager thread
	private final Thread thread;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private volatile boolean stopping = false;

	// manager thread sleep
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
		this.thread.setName(MANAGER_THREAD_NAME);
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
		while (true) {
			if (this.stopping || !this.isRunning())
				break;
			long sleep = threadSleep;
			// check task triggers
			final Iterator<xSchedulerTask> it = this.tasks.iterator();
			this.changes = false;
			while (it.hasNext()) {
				final xSchedulerTask task = it.next();
				final long untilNext = task.untilNextTrigger();
				// disabled
				if (untilNext == -1L)
					continue;
				// trigger now
				if (untilNext <= 0L) {
					Thread.interrupted();
					task.trigger();
				}
				if (untilNext < sleep) {
					sleep = untilNext;
				}
			}
			// calculate sleep
			if (sleep > 0L) {
				final long sleepLess = (
					sleep < threadSleep
					? (long) Math.floor( ((double) sleep) * 0.95 )
					: threadSleep
				);
				if (sleepLess > 0L && !this.changes) {
					if (this.log().isLoggable(xLevel.DETAIL)) {
						final double sleepLessSec = ((double)sleepLess) / 1000.0;
						log().finest(
							(new StringBuilder())
								.append("Sleeping: ")
								.append(NumberUtils.FormatDecimal("#.###", sleepLessSec))
								.append("s ..")
								.toString()
						);
					}
					// sleep until next check
					if (!this.changes) {
						this.sleeping = true;
						if (!this.changes) {
							ThreadUtils.Sleep(sleepLess);
						}
						this.sleeping = false;
					}
				}
			}
		}
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
		return this.tasks.remove(task);
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
				.get(this.getName());
		this._log = new SoftReference<xLog>(log);
		return log;
	}



}
