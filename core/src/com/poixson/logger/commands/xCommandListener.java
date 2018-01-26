package com.poixson.logger.commands;

import com.poixson.tools.events.xEventListener;


public interface xCommandListener extends xEventListener {


	public void onCommand(final xCommandEvent event);


}
