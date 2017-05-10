package com.poixson.utils.xScheduler.trigger;

import com.poixson.utils.xScheduler.xSchedulerTrigger;


public class TriggerClock extends xSchedulerTrigger {



//TODO:
	public static TriggerClock get(final String time) {
		return new TriggerClock(time);
	}



//TODO:
	public TriggerClock(final String time) {
	}



	@Override
	public long untilNextTrigger() {
//TODO:
return Long.MIN_VALUE;
	}



	@Override
	public boolean hasTriggered() {
//TODO:
return false;
	}



}
