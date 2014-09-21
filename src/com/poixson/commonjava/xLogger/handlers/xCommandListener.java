package com.poixson.commonjava.xLogger.handlers;

import com.poixson.commonjava.EventListener.xListener;
import com.poixson.commonjava.xLogger.handlers.xCommandEvent;


public interface xCommandListener extends xListener {


	public void onCommand(final xCommandEvent event);


}
