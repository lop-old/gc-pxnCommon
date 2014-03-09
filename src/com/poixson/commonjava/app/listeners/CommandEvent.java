package com.poixson.commonjava.app.listeners;

import com.poixson.commonjava.EventListener.xEventMeta;
import com.poixson.commonjava.Utils.utils;


public class CommandEvent extends xEventMeta {

	public final String commandStr;


	public CommandEvent(final String commandStr) {
		if(utils.isEmpty(commandStr)) throw new NullPointerException("commandStr cannot be null");
		this.commandStr = commandStr;
	}


}
