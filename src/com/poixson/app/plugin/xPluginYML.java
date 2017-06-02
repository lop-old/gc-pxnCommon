package com.poixson.app.plugin;

import java.util.Map;

import com.poixson.app.appDefines;
import com.poixson.utils.Utils;
import com.poixson.utils.xConfig.xConfig;


public class xPluginYML extends xConfig {

	private final String name;
	private final String version;
	private final String author;
	private final String website;
	private final String mainClass;



	public xPluginYML(final Map<String, Object> datamap) {
		this(datamap, null);
	}
	public xPluginYML(final Map<String, Object> datamap, final String mainClassKey) {
		super(datamap);
		this.name    = this.getString(appDefines.PLUGIN_NAME);
		this.version = this.getString(appDefines.PLUGIN_VERSION);
		this.author  = this.getString(appDefines.PLUGIN_AUTHOR);
		this.website = this.getString(appDefines.PLUGIN_WEBSITE);
		{
			final String key = (
				Utils.isEmpty(mainClassKey)
				? appDefines.DEFAULT_PLUGIN_CLASS_KEY
				: mainClassKey
			);
			this.mainClass = this.getStr(key, null);
		}
	}
	@Override
	public xPluginYML clone() {
		return new xPluginYML(super.datamap);
	}



	// plugin name
	public String getPluginName() {
		return this.name;
	}
	// plugin version
	public String getPluginVersion() {
		return this.version;
	}
	// plugin author
	public String getPluginAuthor() {
		return this.author;
	}
	// plugin website
	public String getPluginWebsite() {
		return this.website;
	}
	// plugin main class
	public String getMainClass() {
		return this.mainClass;
	}



}
