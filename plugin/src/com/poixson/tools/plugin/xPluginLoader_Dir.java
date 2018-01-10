package com.poixson.tools.plugin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.utils.FileUtils;
import com.poixson.utils.Utils;


// load all plugins from dir
public class xPluginLoader_Dir<T extends xJavaPlugin> extends xPluginLoader_File<T> {

	private static final String DEFAULT_PLUGINS_DIR = xPluginDefines.DEFAULT_PLUGINS_DIR;



	public xPluginLoader_Dir(final xPluginManager<T> manager) {
		super(manager);
	}



	public Set<T> LoadFromDir() {
		return
			this.LoadFromDir(
				(File) null
			);
	}
	public Set<T> LoadFromDir(final String pathStr) {
		if (Utils.isEmpty(pathStr)) throw new RequiredArgumentException("pathStr");
		return
			this.LoadFromDir(
				Utils.isEmpty(pathStr)
				? (File) null
				: new File(pathStr)
			);
	}
	public Set<T> LoadFromDir(final File path) {
		final File dir;
		if (path == null) {
			if (Utils.isEmpty(DEFAULT_PLUGINS_DIR))
				throw new NullPointerException("DEFAULT_PLUGINS_DIR");
			dir = new File(DEFAULT_PLUGINS_DIR);
		} else {
			dir = path;
		}
		final Set<T> plugins = new HashSet<T>();
		// create plugins dir if not existing
		if (!dir.exists()) {
			if (!dir.mkdirs())
				throw new RuntimeException("Failed to create plugins directory: "+dir.getName());
			this.log()
				.info("Created directory: {}", dir.getName());
		}
		// list dir contents
		final File[] files =
			FileUtils.ListDirContents(
				dir,
				".jar"
			);
		if (files == null) throw new RuntimeException("Failed to list plugins directory!");
		// no plugins found
		if (Utils.isEmpty(files)) {
			this.log()
				.warning("No plugins found to load.");
			return new HashSet<T>(0);
		}
		// load found jars
		for (final File f : files) {
			if (f == null)
				continue;
			// new plugin instance
			final T plugin = this.LoadJar(f);
			if (plugin == null)
				continue;
			plugins.add(plugin);
		}
		if (plugins.isEmpty()) {
			this.log()
				.warning("No plugins found to load.");
			return new HashSet<T>(0);
		}
		this.log()
			.info(
				"Found [ {} ] plugin{}.",
				Integer.valueOf(plugins.size()),
				( plugins.size() > 2 ? "s" : "")
			);
		return plugins;
	}



}
