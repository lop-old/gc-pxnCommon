package com.poixson.app.commands;

import com.poixson.app.xApp;
import com.poixson.app.xCommandSpec;


public class Commands_Standard {



	@xCommandSpec(Name="exit", Aliases="e,quit")
	public void commandExit() {
		xApp.shutdown();
	}



}
