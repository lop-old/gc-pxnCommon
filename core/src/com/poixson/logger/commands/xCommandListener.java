package com.poixson.logger;

import com.poixson.tools.events.xEventListener;


public interface xCommandListener extends xEventListener {


	public void onCommand(final xCommandEvent event);


}
