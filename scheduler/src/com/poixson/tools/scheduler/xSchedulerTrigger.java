package com.poixson.tools.scheduler;

import com.poixson.abstractions.xEnableable;


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
		return this.enable(true);
	}
	public xSchedulerTrigger disable() {
		return this.enable(false);
	}
	public xSchedulerTrigger enable(final boolean enabled) {
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

	public void setRepeat() {
		this.setRepeat(true);
	}
	public void disableRepeat() {
		this.setRepeat(false);
	}
	public void setRunOnce() {
		this.setRepeat(false);
	}
	public void setRepeat(final boolean repeating) {
		this.repeating = repeating;
	}

	public xSchedulerTrigger repeat() {
		return this.repeat(true);
	}
	public xSchedulerTrigger noRepeat() {
		return this.repeat(false);
	}
	public xSchedulerTrigger runOnce() {
		return this.repeat(false);
	}
	public xSchedulerTrigger repeat(final boolean repeating) {
		this.setRepeat(repeating);
		return this;
	}



}
