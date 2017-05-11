package com.poixson.utils.xScheduler;

import java.lang.ref.SoftReference;

import com.poixson.utils.xClock;
import com.poixson.utils.xEnableable;


public abstract class xSchedulerTrigger implements xEnableable {

	// trigger config
	private volatile boolean enabled   = true;
	private volatile boolean repeating = true;



	public xSchedulerTrigger() {
	}



	public abstract long untilNextTrigger();



	public void unregister() {
		this.setDisabled();
	}



	// trigger enabled
	@Override
	public boolean isEnabled() {
		return this.enabled;
	}
	@Override
	public boolean notEnabled() {
		return ! this.isEnabled();
	}
	@Override
	public void setEnabled() {
		this.setEnabled(true);
	}
	@Override
	public void setDisabled() {
		this.setEnabled(false);
	}
	@Override
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}



	// repeating trigger
	public boolean isRepeating() {
		return this.repeating;
	}
	public boolean notRepeating() {
		return ! this.isRepeating();
	}
	public void setRepeating() {
		this.setRepeating(true);
	}
	public void setRunOnce() {
		this.setRepeating(false);
	}
	public void setRepeating(final boolean repeating) {
		this.repeating = repeating;
	}



	private static volatile SoftReference<xClock> _clock = null;

	public static long getCurrentMillis() {
		return getClock()
				.millis();
	}
	public static xClock getClock() {
		if (_clock != null) {
			final xClock clock = _clock.get();
			if (clock != null)
				return clock;
		}
		final xClock clock = xClock.get(false);
		_clock = new SoftReference<xClock>(clock);
		return clock;
	}



}
