package com.poixson.utils.xScheduler.trigger;

import com.poixson.utils.xScheduler.xSchedulerTrigger;


public class TriggerInterval extends xSchedulerTrigger {

//	protected final xTime delay    = xTime.get();
//	protected final xTime interval = xTime.get();
//	protected volatile long last = -1L;



//TODO:
/*
	public static TriggerTick get(final String delay, final String interval) {
		return
			new TriggerTick(
				delay,
				interval
			);
	}
	public static TriggerTick get(final xTime delay, final xTime interval) {
		return
			new TriggerTick(
				delay,
				interval
			);
	}
	public static TriggerTick get(final String interval) {
		return
			new TriggerTick(
				interval,
				interval
			);
	}
	public static TriggerTick get(final xTime interval) {
		return
			new TriggerTick(
				interval,
				interval
			);
	}



	public TriggerTick(final String delay, final String interval) {
		this.delay.set(delay);
		this.interval.set(interval);
	}
	public TriggerTick(final xTime delay, final xTime interval) {
		this.delay.set(delay);
		this.interval.set(interval);
	}
*/



	@Override
	public long untilNextTrigger() {
//TODO:
/*
		final long now = getCurrentMillis();
		final long delay    = this.delay.getMS();
		final long interval = this.interval.getMS();
		if (interval == 0L)
			return -1L;
		// first trigger
		if (this.last == -1L) {
			this.last = now;
			return (delay > 0L ? delay : 0L);
		}
		final long until = this.interval.getMS() - (now - this.last);
//xLog.getRoot().warning("UNTIL: "+Long.toString(until)+"   "+this.interval.getString());
		// trigger now
		if (until <= 0L) {
			this.last = now;
			return 0L;
		}
		// sleep time
		return until;
*/
return Long.MIN_VALUE;
	}



//	@Override
//	public boolean hasTriggered() {
//	}



}
