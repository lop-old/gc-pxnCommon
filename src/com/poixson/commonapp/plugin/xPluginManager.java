package com.poixson.commonapp.plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.poixson.commonjava.Utils.utilsDirFile;
import com.poixson.commonjava.xLogger.xLog;


public class xPluginManager {

	private static volatile xPluginManager manager = null;
	private static final Object lock = new Object();


	public static xPluginManager get() {
		if(manager == null) {
			synchronized(lock) {
				if(manager == null)
					manager = new xPluginManager();
			}
		}
		return manager;
	}
	protected xPluginManager() {
	}


	protected static class PluginDAO {

		public final String name;
		public final String version;
		public final File file;
		public volatile xJavaPlugin plugin = null;
		public final Map<String, String> classes = new HashMap<String, String>();

		public PluginDAO(final String name, final String version, final File file) {
			this.name = name;
			this.version = version;
			this.file = file;
		}

	}


	// load all plugins from dir
	public void loadAll(final String path) {
		final File dir = new File(path == null ? "plugins/" : path);
		// create plugins directory if needed
		if(!dir.isDirectory())
			dir.mkdir();
		final String[] extensions = new String[] {
			".jar"
		};
		final File[] files = utilsDirFile.listContents(dir, extensions);
		if(files == null) {
throw new NullPointerException();
		}
		// no plugins found
		if(files.length == 0) {
			log().warning("No plugins found to load.");
			return;
		}
		for(final File f : files) {
System.out.println("File: "+f.toString());
			load(f);
		}


	}
	public void load( final File file) {
	}


	public void unloadAll() {
	}
	public void unload() {
	}


	public void enableAll() {
	}
	public void disableAll() {
	}


	// logger
	private volatile xLog _log = null;
	private final Object logLock = new Object();
	public xLog log() {
		if(this._log == null) {
			synchronized(this.logLock) {
				if(this._log == null)
					this._log = xLog.getRoot("PluginManager");
			}
		}
		return this._log;
	}


}
