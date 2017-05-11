package com.poixson.utils.xScheduler;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.poixson.utils.ReflectUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.xEnableable;
import com.poixson.utils.xRunnable;
import com.poixson.utils.xLogger.xLog;
import com.poixson.utils.xThreadPool.xThreadPool;
import com.poixson.utils.xThreadPool.xThreadPoolFactory;


public class xSchedulerTask extends xRunnable implements xEnableable {

	private final xScheduler sched;

	private static final AtomicInteger taskCount = new AtomicInteger(0);
	private final int taskIndex;

	// task config
	private volatile boolean enabled   = true;
	protected volatile boolean finished  = false;

	private volatile xRunnable   run  = null;
	private volatile xThreadPool pool = null;

	// triggers
	private final Set<xSchedulerTrigger> triggers =
			new CopyOnWriteArraySet<xSchedulerTrigger>();

	// task run count
	private final AtomicLong runCount = new AtomicLong(0L);



	public static xSchedulerTask get() {
		return new xSchedulerTask(null);
	}
	public xSchedulerTask(final xScheduler sched) {
		this.sched = (
			sched == null
			? xScheduler.getMainSched()
			: sched
		);
		this.taskIndex = taskCount.incrementAndGet();
	}



	public void register() {
		final xScheduler sched = this.sched;
		if (sched == null)
			throw new RequiredArgumentException("sched");
		sched.add(this);
	}
	public void unregister() {
		final Iterator<xSchedulerTrigger> it = this.triggers.iterator();
		while (it.hasNext()) {
			it.next()
				.unregister();
		}
	}



	public long untilSoonestTrigger() {
		if (this.notEnabled())
			return Long.MIN_VALUE;
		if (this.finished)
			return -1L;
		if (this.triggers.isEmpty())
			return -1L;
		final Iterator<xSchedulerTrigger> it = this.triggers.iterator();
		long lowest = Long.MAX_VALUE;
		while (it.hasNext()) {
			final xSchedulerTrigger trigger = it.next();
			final long untilNext = trigger.untilNextTrigger();
			if (untilNext < lowest) {
				lowest = untilNext;
				if (lowest <= 0L)
					break;
			}
		}
		if (lowest == Long.MAX_VALUE)
			return -1L;
		return lowest;
	}
	public boolean hasFinished() {
		return this.finished;
	}



	public void doTrigger() {
		if (this.notEnabled()) {
			this.log().warning("Skipping disabled task.. this should only happen rarely. ");
			return;
		}
		// run task
		this.runCount.incrementAndGet();
		final xThreadPool threadPool = this.getThreadPool();
		threadPool.runLater(this);
	}



	@Override
	public boolean hasTriggered() {
		return (this.runCount.get() > 0L);
	}



	// run task
	@Override
	public void run() {
		if (this.run != null) {
			final xRunnable run = this.run;
			if (run != null) {
				run.run();
				return;
			}
		}
		this.setDisabled();
		throw new RequiredArgumentException("run");
	}



	// task run count
	public long getRunCount() {
		return this.runCount
				.get();
	}
	public long resetRunCount() {
		return this.runCount
				.getAndSet(0L);
	}



	// ------------------------------------------------------------------------------- //
	// task config



	// task enabled
	@Override
	public boolean isEnabled() {
		if (!this.enabled)
			return false;
		final Iterator<xSchedulerTrigger> it = this.triggers.iterator();
		while (it.hasNext()) {
			final xSchedulerTrigger trigger = it.next();
			if (trigger.isEnabled())
				return true;
		}
		return false;
	}
	@Override
	public boolean notEnabled() {
		return ! this.isEnabled();
	}
	@Override
	public void setEnabled() {
		this.setEnabled(true);
	}
	@Override
	public void setDisabled() {
		this.setEnabled(false);
	}
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}



	// repeating
	public boolean isRepeating() {
		final Iterator<xSchedulerTrigger> it = this.triggers.iterator();
		while (it.hasNext()) {
			if (it.next().isRepeating())
				return true;
		}
		return false;
	}
	public boolean notRepeating() {
		return ! this.isRepeating();
	}



	// runnable
	public xRunnable getRunnable() {
		return this.run;
	}
	public xSchedulerTask setRunnable(final Runnable run) {
		this.run = xRunnable.cast(run);
		return this;
	}



	// task name
	@Override
	public String getTaskName() {
		if (this.run != null) {
			final xRunnable run = this.run;
			if (run != null) {
				final String runTaskName = run.getTaskName();
				if (Utils.notEmpty(runTaskName)) {
					return runTaskName;
				}
			}
		}
		final String taskName = super.getTaskName();
		return (
			Utils.isEmpty(taskName)
			? "task"+Integer.toString(this.taskIndex)
			: taskName
		);
	}
	@Override
	public void setTaskName(final String taskName) {
		super.setTaskName(taskName);
		final xRunnable run = this.run;
		if (run != null) {
			run.setTaskName(taskName);
		}
	}
	@Override
	public boolean taskNameEquals(final String taskName) {
		final xRunnable run = this.run;
		if (run != null) {
			final String runTaskName = run.getTaskName();
			if (Utils.isEmpty(runTaskName))
				return Utils.isEmpty(runTaskName);
			return runTaskName.equals(taskName);
		}
		return super.taskNameEquals(taskName);
	}



	// thread pool
	public xThreadPool getThreadPool() {
		final xThreadPool pool = this.pool;
		return (
			pool == null
			? xThreadPoolFactory.getMainPool()
			: pool
		);
	}
	public xSchedulerTask setThreadPool(final String poolName) {
		return
			this.setThreadPool(
				xThreadPoolFactory.get(poolName)
			);
	}
	public xSchedulerTask setThreadPool(final xThreadPool pool) {
		this.pool = pool;
		return this;
	}



	// trigger
	public xSchedulerTrigger[] getTriggers() {
		return this.triggers.toArray(new xSchedulerTrigger[0]);
	}
	public int getTriggersCount() {
		return this.triggers.size();
	}
	public xSchedulerTask addTrigger(final xSchedulerTrigger trigger) {
		if (trigger != null) {
			this.triggers.add(trigger);
		}
		return this;
	}
	public xSchedulerTask clearTriggers() {
		if (!this.triggers.isEmpty()) {
			final Iterator<xSchedulerTrigger> it = this.triggers.iterator();
			final Set<xSchedulerTrigger> removing = new HashSet<xSchedulerTrigger>();
			while (it.hasNext()) {
				final xSchedulerTrigger trigger = it.next();
				trigger.unregister();
				removing.add(trigger);
			}
			for (final xSchedulerTrigger trigger : removing) {
				this.triggers.remove(trigger);
			}
		}
		return this;
	}



	// logger
	private volatile SoftReference<xLog> _log = null;
	private volatile String _className = null;
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null)
				return log;
		}
		if (this._className == null) {
			this._className =
				ReflectUtils.getClassName(
					this.getClass()
				);
		}
		final xLog log =
			this.sched.log()
				.getWeak(this.getTaskName());
		this._log = new SoftReference<xLog>(log);
		return log;
	}



}
