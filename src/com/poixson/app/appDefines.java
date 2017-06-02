package com.poixson.commonapp;

import com.poixson.utils.Keeper;


public final class appDefines {
	private appDefines() {}
	{
		Keeper.add(new appDefines());
	}



//TODO:
//	// defaults
//	public static final xTime DEFAULT_TICK_INTERVAL = xTime.get("60s");

	// plugin.yml keys
	public static final String PLUGIN_NAME    = "Plugin Name";
	public static final String PLUGIN_VERSION = "Plugin Version";
	public static final String PLUGIN_AUTHOR  = "Plugin Author";
	public static final String PLUGIN_WEBSITE = "Plugin Website";



}
