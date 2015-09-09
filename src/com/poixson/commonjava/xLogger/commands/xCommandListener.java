package com.poixson.commonjava.xLogger.handlers;

import com.poixson.commonjava.xEvents.xEventListener;


public interface xCommandListener extends xEventListener {


	public void onCommand(final xCommandEvent event);


}
