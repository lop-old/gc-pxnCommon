package com.poixson.commonjava.scheduler.triggers;

import com.poixson.commonjava.Utils.xClock;
import com.poixson.commonjava.Utils.xTime;


public class triggerInterval implements TriggerType {

	protected final xTime delay    = xTime.get();
	protected final xTime interval = xTime.get();
	protected volatile long last = -1;



	public static triggerInterval get(final String delay, final String interval) {
		return new triggerInterval(delay, interval);
	}
	public static triggerInterval get(final xTime delay, final xTime interval) {
		return new triggerInterval(delay, interval);
	}
	public static triggerInterval get(final String interval) {
		return new triggerInterval(interval, interval);
	}
	public static triggerInterval get(final xTime interval) {
		return new triggerInterval(interval, interval);
	}



	public triggerInterval(final String delay, final String interval) {
		this.delay.set(delay);
		this.interval.set(interval);
	}
	public triggerInterval(final xTime delay, final xTime interval) {
		this.delay.set(delay);
		this.interval.set(interval);
	}



	@Override
	public long untilNextTrigger() {
		final long now = getCurrentMillis();
		final long delay    = this.delay.getMS();
		final long interval = this.interval.getMS();
		if(interval == 0)
			return -1;
		// first trigger
		if(this.last == -1) {
			this.last = now;
			return (delay > 0 ? delay : 0);
		}
		final long until = this.interval.getMS() - (now - this.last);
//xLog.getRoot().warning("UNTIL: "+Long.toString(until)+"   "+this.interval.getString());
		// trigger now
		if(until <= 0) {
			this.last = now;
			return 0;
		}
		// sleep time
		return until;
	}



	public static long getCurrentMillis() {
		return xClock.get().millis();
	}



}
