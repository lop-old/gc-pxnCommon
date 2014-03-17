package com.poixson.commonjava.EventListener;


public class xEventMeta {


	// event handled
	private volatile boolean handled = false;
	public void setHandled() {
		setHandled(true);
	}
	public void setHandled(final boolean handled) {
		this.handled = handled;
	}
	public boolean isHandled() {
		return handled;
	}


	// event cancelled
	private volatile boolean cancelled = false;
	public void setCancelled() {
		setCancelled(true);
	}
	public void setCancelled(final boolean cancelled) {
		this.cancelled = cancelled;
	}
	public boolean isCancelled() {
		return cancelled;
	}


	/**
	 * Reset state for next listener.
	 */
	public void reset() {}


}
