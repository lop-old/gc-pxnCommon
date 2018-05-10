package com.poixson.app.commands;

import com.poixson.app.xApp;
import com.poixson.app.xCommandSpec;


public class Commands_Standard {



	@xCommandSpec(Name="exit", Aliases="e,quit")
	public void __COMMAND_exit() {
		xApp.shutdown();
	}
	@xCommandSpec(Name="kill", Aliases="k")
	public void __COMMAND__kill() {
		xApp.kill();
		System.exit(1);
	}



}
