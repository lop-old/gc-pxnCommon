package com.poixson.commonjava.xLogger.handlers;

import com.poixson.commonjava.xEvents.xListener;


public interface xCommandListener extends xListener {


	public void onCommand(final xCommandEvent event);


}
