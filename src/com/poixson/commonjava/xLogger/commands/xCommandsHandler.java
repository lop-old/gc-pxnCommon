package com.poixson.commonjava.xLogger.commands;

import com.poixson.commonjava.xEvents.xHandler;


public class xCommandsHandler extends xHandler<xCommandListener> {



	public xCommandsHandler() {
	}



	// event type
	@Override
	protected Class<? extends xEventData> getEventDataType() {
		return xCommandEvent.class;
	}



}
