package com.poixson.commonjava.xEvents;


public interface xEventListener {


	public static enum ListenerPriority {
		HIGHEST,
		HIGH,
		NORMAL,
		LOW,
		LOWEST
	}

	public String getName();
	public String toString();


}
