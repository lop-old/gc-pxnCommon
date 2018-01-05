package com.poixson.logger;

import com.poixson.utils.xEvents.xEventListener;


public interface xCommandListener extends xEventListener {


	public void onCommand(final xCommandEvent event);


}
