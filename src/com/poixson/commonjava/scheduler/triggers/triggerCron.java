package com.poixson.commonjava.scheduler.triggers;


public class triggerCron implements TriggerType {



	public static triggerCron get(final String value) {
		return new triggerCron(value);
	}



	public triggerCron(final String value) {
	}



	@Override
	public long untilNextTrigger() {
		return -1;
	}



}
