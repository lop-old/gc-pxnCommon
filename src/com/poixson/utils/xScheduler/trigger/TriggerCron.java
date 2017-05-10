package com.poixson.utils.xScheduler.trigger;

import com.poixson.utils.xScheduler.xSchedulerTrigger;


public class TriggerCron extends xSchedulerTrigger {

//	protected final CronPredictor predictor;

//	protected volatile long next = -1L;



//TODO:
/*
	public static TriggerCron get(final String pattern) {
		return new TriggerCron(pattern);
	}
	public static TriggerCron get(final CronPattern pattern) {
		return new TriggerCron(pattern);
	}
	public static TriggerCron get(final CronPredictor pattern) {
		return new TriggerCron(pattern);
	}



	public TriggerCron(final String pattern) {
		this(
			new CronPattern(pattern)
		);
	}
	public TriggerCron(final CronPattern pattern) {
		this(
			new CronPredictor(pattern)
		);
	}
	public TriggerCron(final CronPredictor predictor) {
		this.predictor = predictor;
	}
*/



	@Override
	public long untilNextTrigger() {
//TODO:
//		long until = this.predictor.untilNextMatching(now);
//xLog.getRoot().publish("until next: "+xTime.get(until, xTimeU.MS).toFullString());
//		return until;
return Long.MIN_VALUE;
	}



	@Override
	public boolean hasTriggered() {
//TODO:
return false;
	}



}
