package com.poixson.commonjava.Utils;


public class CoolDown {

	protected final xTime coolDuration = xTime.get();
	protected volatile long last = -1L;
	protected final Object lock = new Object();


	public static CoolDown get() {
		return new CoolDown();
	}
	public static CoolDown get(final long ms) {
		CoolDown cool = CoolDown.get();
		cool.setDuration(ms);
		return cool;
	}
	public static CoolDown get(final String time) {
		CoolDown cool = CoolDown.get();
		cool.setDuration(time);
		return cool;
	}
	public static CoolDown get(final xTime time) {
		CoolDown cool = CoolDown.get();
		cool.setDuration(time);
		return cool;
	}
	private CoolDown() {
	}


	public boolean runAgain() {
		synchronized(this.lock) {
			final long current = utils.getSystemMillis();
			// first run
			if(this.last == -1L) {
				this.last = current;
				return true;
			}
			final long since = current - this.last;
			// run again
			if(since >= this.coolDuration.getMS()) {
				this.last = current;
				return true;
			}
		}
		// cooling
		return false;
	}


	public void reset() {
		synchronized(this.lock) {
			this.last = -1L;
		}
	}


	// set duration
	public void setDuration(final long ms) {
		this.coolDuration.set(ms, xTimeU.MS);
	}
	public void setDuration(final String time) {
		this.coolDuration.set(time);
	}
	public void setDuration(final xTime time) {
		this.coolDuration.set(time);
	}
	// get duration
	public xTime getDuration() {
		return this.coolDuration.clone();
	}


}
