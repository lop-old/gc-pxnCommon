package com.poixson.commonapp.plugin;

import java.util.Map;

import com.poixson.commonapp.config.xConfig;


public class xPluginYML extends xConfig {

	// key names
	public static final String PLUGIN_NAME    = "Plugin Name";
	public static final String PLUGIN_VERSION = "Plugin Version";
	public static final String PLUGIN_AUTHOR  = "Author";
	public static final String PLUGIN_WEBSITE = "Website";



	public xPluginYML(Map<String, Object> data) {
		super(data);
	}



	// plugin name
	public String getPluginName() {
		return getString(PLUGIN_NAME);
	}



	// plugin version
	public String getPluginVersion() {
		return getString(PLUGIN_VERSION);
	}



	// plugin author
	public String getPluginAuthor() {
		return getString(PLUGIN_AUTHOR);
	}



	// plugin website
	public String getPluginWebsite() {
		return getString(PLUGIN_WEBSITE);
	}



}
