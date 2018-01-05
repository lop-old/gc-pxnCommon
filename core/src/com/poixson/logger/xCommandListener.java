package com.poixson.utils.xLogger;

import com.poixson.utils.xEvents.xEventListener;


public interface xCommandListener extends xEventListener {


	public void onCommand(final xCommandEvent event);


}
