package com.poixson.plugins;

import com.poixson.tools.Keeper;


public final class xPluginDefines {
	private xPluginDefines() {}
	static { Keeper.add(new xPluginDefines()); }



	public static final String DEFAULT_PLUGINS_DIR      = "plugins/";
	public static final String DEFAULT_PLUGIN_CLASS_KEY = "Main Class";

	// plugin.yml keys
	public static final String PLUGIN_YML_NAME        = "Plugin Name";
	public static final String PLUGIN_YML_VERSION     = "Plugin Version";
	public static final String PLUGIN_YML_APP_VERSION = "App Version";
	public static final String PLUGIN_YML_COMMIT      = "Commit";
	public static final String PLUGIN_YML_AUTHOR      = "Author";
	public static final String PLUGIN_YML_WEBSITE     = "Website";



}
