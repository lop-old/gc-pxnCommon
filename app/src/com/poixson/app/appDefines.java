package com.poixson.app;

import com.poixson.utils.Keeper;


public final class appDefines {
	private appDefines() {}
	{ Keeper.add(new appDefines()); }



//TODO:
//	// defaults
//	public static final xTime DEFAULT_TICK_INTERVAL = xTime.getNew("60s");



	// plugins
	public static final String DEFAULT_PLUGINS_DIR      = "plugins/";
	public static final String DEFAULT_PLUGIN_CLASS_KEY = "Main Class";

	// plugin.yml keys
	public static final String PLUGIN_NAME    = "Plugin Name";
	public static final String PLUGIN_VERSION = "Plugin Version";
	public static final String PLUGIN_AUTHOR  = "Plugin Author";
	public static final String PLUGIN_WEBSITE = "Plugin Website";



}
