/*
package com.poixson.commonjava.xLogger.commands;

import com.poixson.commonjava.xEvents.xEventData;
import com.poixson.commonjava.xEvents.xEventListener;
import com.poixson.commonjava.xEvents.xHandlerSimple;


public class xCommandsHandler extends xHandlerSimple {
	private static final String LISTENER_METHOD_NAME = "onCommand";



	public static xCommandsHandler get() {
		return new xCommandsHandler();
	}
	protected xCommandsHandler() {
		super();
	}



	// listener type
	@Override
	protected Class<? extends xEventListener> getEventListenerType() {
		return xCommandListener.class;
	}
	// event type
	@Override
	protected Class<? extends xEventData> getEventDataType() {
		return xCommandEvent.class;
	}
	// fixed method name
	@Override
	protected String getMethodName() {
		return LISTENER_METHOD_NAME;
	}



}
*/
