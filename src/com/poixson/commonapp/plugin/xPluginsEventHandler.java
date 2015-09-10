package com.poixson.commonapp.plugin;

import com.poixson.commonjava.xEvents.xEventData;
import com.poixson.commonjava.xEvents.xEventListener;
import com.poixson.commonjava.xEvents.xHandlerGeneric;


public class xPluginsHandler extends xHandlerGeneric {



	public static xPluginsHandler get() {
		return new xPluginsHandler();
	}
	protected xPluginsHandler() {
		super();
	}



	// listener type
	@Override
	protected Class<? extends xEventListener> getEventListenerType() {
		return xPluginEventListener.class;
	}
	// event type
	@Override
	protected Class<? extends xEventData> getEventDataType() {
		return xPluginEvent.class;
	}



}
