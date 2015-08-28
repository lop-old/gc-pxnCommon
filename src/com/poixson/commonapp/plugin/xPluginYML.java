package com.poixson.commonapp.plugin;

import java.util.Map;

import com.poixson.commonapp.appDefines;
import com.poixson.commonapp.config.xConfig;


public class xPluginYML extends xConfig {




	public xPluginYML(final Map<String, Object> data) {
		super(data);
	}



	// plugin name
	public String getPluginName() {
		return this.getString(appDefines.PLUGIN_NAME);
	}



	// plugin version
	public String getPluginVersion() {
		return this.getString(appDefines.PLUGIN_VERSION);
	}



	// plugin author
	public String getPluginAuthor() {
		return this.getString(appDefines.PLUGIN_AUTHOR);
	}



	// plugin website
	public String getPluginWebsite() {
		return this.getString(appDefines.PLUGIN_WEBSITE);
	}



}
