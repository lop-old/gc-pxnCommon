package com.poixson.app.plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.poixson.utils.xLogger.xLog;


public class xPluginDAO {

	public final String name;
	public final File file;
	public final xPluginYML yml;
	public volatile xJavaPlugin plugin = null;
	public final Map<String, String> classes = new HashMap<String, String>();
	public final xLog log;



	public PluginDAO(final String name, final File file, final xPluginYML yml) {
		this.name = name;
		this.file = file;
		this.yml  = yml;
		this.log  = xLog.getRoot()
				.getWeak(name);
	}



}
