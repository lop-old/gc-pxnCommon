package com.poixson.utils.xScheduler;

import com.poixson.utils.xEnableable;


public abstract class xSchedulerTrigger implements xEnableable {

	private volatile boolean enabled = false;
	private volatile boolean repeating = true;



	public abstract long untilNextTrigger();
//TODO: remove this?
//	public abstract boolean hasTriggered();



	public void unregister() {
		this.setDisabled();
	}



	@Override
	public boolean isEnabled() {
		return this.enabled;
	}
	@Override
	public boolean notEnabled() {
		return ! this.enabled;
	}
	@Override
	public void setEnabled() {
		this.setEnabled(true);
	}
	@Override
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}



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



}
