package com.poixson.tools.events;


public interface xEventListener {


	public static enum ListenerPriority {
		HIGHEST,
		HIGH,
		NORMAL,
		LOW,
		LOWEST
	}

	public String getName();
	@Override
	public String toString();


}
