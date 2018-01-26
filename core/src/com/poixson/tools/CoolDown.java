package com.poixson.tools;

import com.poixson.utils.Utils;


public class CoolDown {

	protected final xTime duration = xTime.getNew();
	protected long last = -1L;

	protected final Object lock = new Object();



	public static CoolDown getNew() {
		return new CoolDown();
	}
	public static CoolDown getNew(final long ms) {
		CoolDown cool = CoolDown.getNew();
		cool.setDuration(ms);
		return cool;
	}
	public static CoolDown getNew(final String time) {
		CoolDown cool = CoolDown.getNew();
		cool.setDuration(time);
		return cool;
	}
	public static CoolDown getNew(final xTime time) {
		CoolDown cool = CoolDown.getNew();
		cool.setDuration(time);
		return cool;
	}
	private CoolDown() {
	}



	/**
	 * Checks the state of the cooldown period.
	 * @return true if period reached and reset, false if not yet reached.
	 */
	public boolean runAgain() {
		synchronized (this.lock) {
			final long current = this.getCurrent();
			final long last    = this.last;
			// first run
			if (last == -1L) {
				this.last = current;
				return true;
			}
			// run again
			final long duration = this.duration.getMS();
			if (duration <= 0L)
				return false;
			if (current - last >= duration) {
				this.last = current;
				return true;
			}
		}
		// cooling
		return false;
	}



	public long getCurrent() {
		return Utils.getSystemMillis();
	}
	public long getLast() {
		return this.last;
	}



	public long getTimeSince() {
		final long last = this.last;
		return (
			last == -1L
			? -1L
			: this.getCurrent() - last
		);
	}
	public long getTimeUntil() {
		final long last = this.last;
		if (last == -1L)
			return -1L;
		final long duration = this.duration.getMS();
		if (duration <= 0L)
			return -1L;
		return ( (last + duration) - this.getCurrent() );
	}



	public void resetClean() {
		synchronized (this.lock) {
			this.last = -1L;
		}
	}
	public void resetRun() {
		synchronized (this.lock) {
			this.last = this.getCurrent();
		}
	}



	// set duration
	public void setDuration(final long ms) {
		this.duration.set(ms, xTimeU.MS);
	}
	public void setDuration(final String time) {
		this.duration.set(time);
	}
	public void setDuration(final xTime time) {
		this.duration.set(time);
	}
	// get duration
	public xTime getDuration() {
		return this.duration.clone();
	}



}
