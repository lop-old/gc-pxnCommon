package com.poixson.utils.xScheduler;

import com.poixson.utils.xEnableable;


public abstract class xSchedulerTrigger implements xEnableable {

	private volatile boolean enabled = false;



	public abstract long untilNextTrigger();
//TODO: remove this?
//	public abstract boolean hasTriggered();



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



}
