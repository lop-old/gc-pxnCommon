package com.poixson.app.commands.groups;

import com.poixson.app.xApp;
import com.poixson.app.commands.xCommand;


public class Commands_Standard {



	@xCommand(Name="exit", Aliases="e,stop,quit")
	public void __COMMAND_exit() {
		xApp.shutdown();
	}
	@xCommand(Name="kill", Aliases="k")
	public void __COMMAND__kill() {
		xApp.kill();
		System.exit(1);
	}



}
