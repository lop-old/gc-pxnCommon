package com.poixson.commonjava.scheduler.triggers;

import com.poixson.commonjava.Utils.xClock;
import com.poixson.commonjava.scheduler.cron.CronPattern;
import com.poixson.commonjava.scheduler.cron.CronPredictor;


public class triggerCron implements TriggerType {

	protected final CronPredictor predictor;

	protected volatile long next = -1L;



	public static triggerCron get(final String pattern) {
		return new triggerCron(pattern);
	}
	public static triggerCron get(final CronPattern pattern) {
		return new triggerCron(pattern);
	}
	public static triggerCron get(final CronPredictor pattern) {
		return new triggerCron(pattern);
	}



	public triggerCron(final String pattern) {
		this(new CronPattern(pattern));
	}
	public triggerCron(final CronPattern pattern) {
		this(new CronPredictor(pattern));
	}
	public triggerCron(final CronPredictor predictor) {
		this.predictor = predictor;
	}



	@Override
	public long untilNextTrigger() {
		final long now = getCurrentMillis();
		long until = this.predictor.untilNextMatching(now);
//xLog.getRoot().publish("until next: "+xTime.get(until, xTimeU.MS).toFullString());
		return until;
	}



	public static long getCurrentMillis() {
		return xClock.get().millis();
	}



}
