/*
package com.poixson.commonjava.scheduler.triggers;

import com.poixson.commonjava.Utils.xClock;
import com.poixson.commonjava.scheduler.cron.CronPattern;
import com.poixson.commonjava.scheduler.cron.CronPredictor;


public class TriggerCron implements TriggerType {

	protected final CronPredictor predictor;

	protected volatile long next = -1L;



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
		this(new CronPattern(pattern));
	}
	public TriggerCron(final CronPattern pattern) {
		this(new CronPredictor(pattern));
	}
	public TriggerCron(final CronPredictor predictor) {
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
*/
