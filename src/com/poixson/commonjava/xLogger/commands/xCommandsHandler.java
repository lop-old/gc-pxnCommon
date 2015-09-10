package com.poixson.commonjava.xLogger.commands;

import com.poixson.commonjava.xEvents.xEventData;
import com.poixson.commonjava.xEvents.xHandler;


public class xCommandsHandler extends xHandler<xCommandListener> {



	public static xCommandsHandler get() {
		return new xCommandsHandler();
	}
	protected xCommandsHandler() {
		super();
	}



	// event type
	@Override
	protected Class<? extends xEventData> getEventDataType() {
		return xCommandEvent.class;
	}



}
