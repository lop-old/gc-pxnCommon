package com.poixson.commonjava.Utils;


public class CoolDown {

	protected final xTime coolDuration = xTime.get();
	protected volatile long last = -1L;
	protected final Object lock = new Object();


	public static CoolDown get() {
		return new CoolDown();
	}
	public static CoolDown get(long ms) {
		CoolDown cool = CoolDown.get();
		cool.setDuration(ms);
		return cool;
	}
	public static CoolDown get(String time) {
		CoolDown cool = CoolDown.get();
		cool.setDuration(time);
		return cool;
	}
	public static CoolDown get(xTime time) {
		CoolDown cool = CoolDown.get();
		cool.setDuration(time);
		return cool;
	}
	private CoolDown() {
	}


	public boolean runAgain() {
		synchronized(lock) {
			long current = utilsSystem.getSystemMillis();
			// first run
			if(last == -1L) {
				last = current;
				return true;
			}
			long since = current - last;
			// run again
			if(since >= coolDuration.getMS()) {
				last = current;
				return true;
			}
		}
		// cooling
		return false;
	}


	public void reset() {
		synchronized(lock) {
			last = -1L;
		}
	}


	// set duration
	public void setDuration(long ms) {
		this.coolDuration.set(ms, xTimeU.MS);
	}
	public void setDuration(String time) {
		this.coolDuration.set(time);
	}
	public void setDuration(xTime time) {
		this.coolDuration.set(time);
	}
	// get duration
	public xTime getDuration() {
		return coolDuration.clone();
	}


}
