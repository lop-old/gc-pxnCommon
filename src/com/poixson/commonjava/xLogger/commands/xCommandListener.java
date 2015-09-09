package com.poixson.commonjava.xLogger.commands;

import com.poixson.commonjava.xEvents.xEventListener;


public interface xCommandListener extends xEventListener {


	public void onCommand(final xCommandEvent event);


}
