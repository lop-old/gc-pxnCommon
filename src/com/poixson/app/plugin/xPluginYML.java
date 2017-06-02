package com.poixson.app.plugin;

import java.util.Map;
import java.util.Set;

import com.poixson.app.appDefines;
import com.poixson.utils.Utils;
import com.poixson.utils.xConfig.xConfig;


public class xPluginYML extends xConfig {

	private final String name;
	private final String version;
	private final String author;
	private final String website;
	private final String[] mainClasses;



	public xPluginYML(final Map<String, Object> datamap) {
		super(datamap);
		this.name    = this.getString(appDefines.PLUGIN_NAME);
		this.version = this.getString(appDefines.PLUGIN_VERSION);
		this.author  = this.getString(appDefines.PLUGIN_AUTHOR);
		this.website = this.getString(appDefines.PLUGIN_WEBSITE);
		{
			final String mainClass = this.getStr("Main Class", null);
			if (Utils.notBlank(mainClass)) {
				this.mainClasses =
					new String[] {
						mainClass
					};
			} else {
				final Set<String> mainClassSet =
					this.getStringSet("Main Class");
				this.mainClasses = (
					mainClassSet == null
					? null
					: mainClassSet.toArray(new String[0])
				);
			}
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
	// main classes
	public String[] getMainClasses() {
		return this.mainClasses;
	}



}
