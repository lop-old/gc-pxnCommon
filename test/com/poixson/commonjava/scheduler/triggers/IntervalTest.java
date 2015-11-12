/*
package com.poixson.commonjava.scheduler.triggers;

import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.logger.xLogTest;
import com.poixson.commonjava.scheduler.xScheduledTask;
import com.poixson.commonjava.scheduler.xScheduler;
import com.poixson.commonjava.xLogger.xLog;


public class ClockTest extends xRunnable {

	public final xScheduledTask task;



	public ClockTest(final xScheduler sched) {
		this();
		if(sched != null)
			sched.schedule(this.task);
	}
	public ClockTest() {
		super("ClockTest");
		this.task = xScheduledTask.get();
		this.task.setRunnable(this);
		this.task.setRepeating(false);
		this.task.setTrigger(TriggerClock.get("1:00"));
	}



	@Override
	public void run() {
		xLogTest.publish("Clock Test Triggered!");
	}



	public boolean hasFinished() {
		return (this.task.getRunCount() > 0);
	}



}
*/