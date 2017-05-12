package com.poixson.utils.xScheduler;

import com.poixson.utils.xEnableable;


public abstract class xSchedulerTrigger implements xEnableable {

	// trigger config
	private volatile boolean enabled   = true;
	private volatile boolean repeating = true;



	public xSchedulerTrigger() {
	}



	public abstract long untilNextTrigger(final long now);



	public void unregister() {
		this.setDisabled();
	}



	// ------------------------------------------------------------------------------- //
	// trigger config



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

	public xSchedulerTrigger enable() {
		return this.enabled(true);
	}
	public xSchedulerTrigger disable() {
		return this.enabled(false);
	}
	public xSchedulerTrigger enabled(final boolean enabled) {
		this.setEnabled(enabled);
		return this;
	}



	// repeating trigger
	public boolean isRepeating() {
		return this.repeating;
	}
	public boolean notRepeating() {
		return ! this.isRepeating();
	}
	public xSchedulerTrigger setRepeating() {
		return this.setRepeating(true);
	}
	public xSchedulerTrigger setRunOnce() {
		return this.setRepeating(false);
	}
	public xSchedulerTrigger setRepeating(final boolean repeating) {
		this.repeating = repeating;
		return this;
	}



}
