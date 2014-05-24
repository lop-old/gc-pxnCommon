package com.poixson.commonapp.plugin;

import java.io.File;

import com.poixson.commonapp.xConfigYML;


public class xPluginYML {

	protected final xConfigYML config;


	public static xPluginYML get(final File jarFile) {
		final String fileName = "plugin.yml";
		return new xPluginYML(xConfigYML.load(jarFile, fileName));
	}
	public xPluginYML(final xConfigYML config) {
		if(config == null) throw new NullPointerException();
		this.config = config;
	}


	public String getPluginName() {
		return this.config.getString("Plugin Name");
	}
	public String getPluginVersion() {
		return this.config.getString("Plugin Version");
	}
	public String getPluginAuthor() {
		return this.config.getString("Author");
	}
	public String getPluginWebsite() {
		return this.config.getString("Website");
	}


}
