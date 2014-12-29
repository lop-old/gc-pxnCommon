package com.poixson.commonjava.scheduler;

import java.util.concurrent.atomic.AtomicInteger;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.Utils.xThreadPool;
import com.poixson.commonjava.scheduler.triggers.xSchedulerTrigger;
import com.poixson.commonjava.xLogger.xLog;


public class xScheduledTask {

	protected volatile boolean repeating = false;
	protected volatile xRunnable run = null;
	protected volatile xThreadPool pool = null;
	protected volatile xSchedulerTrigger trigger = null;

	// task run count
	protected final AtomicInteger count = new AtomicInteger(0);



	public static xScheduledTask get() {
		return new xScheduledTask();
	}
	public xScheduledTask() {
	}



	public long untilNextTrigger() {
		final xSchedulerTrigger trigger = this.trigger;
		if(trigger == null)
			return -1;
		return trigger.untilNextTrigger();
	}



	public void trigger() {
		final xRunnable r = this.run;
		if(r == null) {
			xLog.getRoot().warning("Scheduled task has null runnable");
			return;
		}
		// get thread pool
		xThreadPool p = this.pool;
		// use main thread pool
		if(p == null)
			p = xThreadPool.get();
		// run task
		p.runLater(r);
		this.count.incrementAndGet();
	}



	public int getRunCount() {
		return this.count.get();
	}



	public xScheduledTask setRunnable(final Runnable run) {
		this.run = xRunnable.cast(run);
		return this;
	}
	public xScheduledTask setRepeating(final boolean repeating) {
		this.repeating = repeating;
		return this;
	}
	public xScheduledTask setThreadPool(final xThreadPool pool) {
		this.pool = pool;
		return this;
	}
	public xScheduledTask setTrigger(final xSchedulerTrigger trigger) {
		this.trigger = trigger;
		return this;
	}



	public String getTaskName() {
		final xRunnable r = this.run;
		if(r == null) return null;
		return r.getTaskName();
	}
	public boolean taskNameEquals(final String name) {
		final String n = this.getTaskName();
		if(utils.isEmpty(name))
			return utils.isEmpty(n);
		return name.equals(n);
	}



}
