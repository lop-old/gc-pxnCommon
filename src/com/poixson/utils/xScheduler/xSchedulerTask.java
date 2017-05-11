package com.poixson.utils.xScheduler;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

import com.poixson.utils.ReflectUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.xRunnable;
import com.poixson.utils.xLogger.xLog;
import com.poixson.utils.xThreadPool.xThreadPool;
import com.poixson.utils.xThreadPool.xThreadPoolFactory;


public class xSchedulerTask {

	protected final xScheduler sched;

	// task config
	protected volatile boolean finished  = false;

	protected volatile xRunnable   run  = null;
	protected volatile xThreadPool pool = null;
	protected volatile Set<xSchedulerTrigger> triggers =
			new CopyOnWriteArraySet<xSchedulerTrigger>();

	// task run count
	protected final AtomicLong runCount = new AtomicLong(0L);



	public static xSchedulerTask get() {
		return new xSchedulerTask(null);
	}
	public xSchedulerTask(final xScheduler sched) {
		this.sched = (
			sched == null
			? xScheduler.getMainSched()
			: sched
		);
	}



	public long untilSoonestTrigger() {
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
		final xRunnable r = this.getRunnable();
		if (r == null) {
			this.log()
				.warning("Scheduled task has null runnable");
			return;
		}
		// run task
		final xThreadPool threadPool = this.getThreadPool();
		threadPool.runLater(r);
		final Iterator<xSchedulerTrigger> it = this.triggers.iterator();
		while (it.hasNext()) {
			it.next()
				.hasTriggered();
		}
		this.runCount.incrementAndGet();
	}



	@Override
	public boolean hasTriggered() {
		return (this.runCount.get() > 0L);
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
	public String getTaskName() {
		final xRunnable run = this.run;
		if (run == null)
			return null;
		return run.getTaskName();
	}
	public boolean taskNameEquals(final String name) {
		final String thisName = this.getTaskName();
		if (Utils.isEmpty(name))
			return Utils.isEmpty(thisName);
		return name.equals(thisName);
	}
	public xSchedulerTask setTaskName(final String name) {
		final xRunnable run = this.run;
		if (run == null)
			throw new IllegalArgumentException("run not set!");
		run.setTaskName(name);
		return this;
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
		this.triggers.clear();
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
