package com.poixson.commonjava.scheduler.triggers;

import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.scheduler.xScheduledTask;
import com.poixson.commonjava.scheduler.xScheduler;
import com.poixson.commonjava.xLogger.xLogTest;


public class TickTest extends xRunnable {

	public final xScheduledTask task;



	public TickTest(final xScheduler sched) {
		this();
		if(sched != null)
			sched.schedule(this.task);
	}
	public TickTest() {
		super("TickTest");
		try {
			this.task = xScheduledTask.get();
			this.task.setRunnable(this);
			this.task.setRepeating(false);
			this.task.setTrigger(triggerInterval.get("1s"));
		} catch (Exception e) {
			xLogTest.trace(e);
			throw e;
		}
	}



	@Override
	public void run() {
		xLogTest.publish("Interval Test Triggered!");
	}



	public boolean hasFinished() {
		return (this.task.getRunCount() > 0);
	}



}
