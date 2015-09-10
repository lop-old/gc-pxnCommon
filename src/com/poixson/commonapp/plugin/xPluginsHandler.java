package com.poixson.commonapp.plugin;

import com.poixson.commonjava.xEvents.xEventData;
import com.poixson.commonjava.xEvents.xHandler;


public class xPluginsHandler extends xHandler<xPluginEventListener> {



	public static xPluginsHandler get() {
		return new xPluginsHandler();
	}
	protected xPluginsHandler() {
		super();
	}



	// event type
	@Override
	protected Class<? extends xEventData> getEventDataType() {
		return xPluginEvent.class;
	}



}
