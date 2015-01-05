package com.poixson.commonjava.scheduler.triggers;


public class triggerClock implements TriggerType {



	public static triggerClock get(final String time) {
		return new triggerClock(time);
	}



	public triggerClock(final String time) {
	}



	@Override
	public long untilNextTrigger() {
		return -1;
	}



}
