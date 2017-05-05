package com.poixson.utils.xScheduler;


public interface xSchedulerTrigger {


	public long untilNextTrigger();
	public boolean hasTriggered();


}
