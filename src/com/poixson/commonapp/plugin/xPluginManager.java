package com.poixson.commonapp.plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.xeustechnologies.jcl.JarClassLoader;

import com.poixson.commonapp.config.xConfigLoader;
import com.poixson.commonjava.Utils.utils;
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



	private final Map<String, PluginDAO> plugins = new HashMap<String, PluginDAO>();
	private volatile String classFieldName = null;



	protected class PluginDAO {

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
			this.log = log().getWeak(name);
		}

	}



	// load all plugins from dir
	public void loadAll() {
		loadAll(null);
	}
	public void loadAll(final String path) {
		final File dir = new File(path == null ? "plugins/" : path);
		// create plugins directory if needed
		if(!dir.isDirectory())
			dir.mkdir();
		final String[] extensions = new String[] {
			".jar"
		};
		final File[] files = utilsDirFile.listContents(dir, extensions);
		if(files == null) throw new NullPointerException();
		// no plugins found
		if(files.length == 0) {
			log().warning("No plugins found to load.");
			return;
		}
		// load plugin jars
		synchronized(this.plugins) {
			for(final File f : files) {
				final PluginDAO dao = load(f);
				if(dao == null) continue;
				// plugin already loaded
				if(this.plugins.containsKey(dao.name)) {
					log().warning("Plugin already loaded with name: "+dao.name);
					continue;
				}
				log().finer("Found plugin file: "+f.toString());
				addPlugin(dao);
			}
		}
	}
	public PluginDAO load(final File file) {
		if(file == null) throw new NullPointerException();
		if(!file.exists()) {
			log().warning("Plugin file not found: "+file.toString());
			return null;
		}
		// load plugin.yml from jar
		xPluginYML yml = (xPluginYML) xConfigLoader.LoadJar(file, "plugin.yml", xPluginYML.class);
		if(yml == null) {
			log().warning("plugin.yml not found in plugin: "+file.toString());
			return null;
		}
		final String pluginName = yml.getPluginName();
		if(utils.isEmpty(pluginName)) {
			log().warning("Plugin name not set: "+file.toString());
			return null;
		}
		return new PluginDAO(
			pluginName,
			file,
			yml
		);
	}



	// init all plugins
	public void initAll() {
		initAll("Main Class");
	}
	public void initAll(final String classField) {
		if(utils.isEmpty(classField)) throw new NullPointerException();
		// init plugins
		synchronized(this.plugins) {
			for(final PluginDAO dao : this.plugins.values()) {
				if(dao.plugin != null) continue;
				init(dao);
			}
		}
	}
	public void init(final PluginDAO dao) {
		if(dao == null) throw new NullPointerException();
		final xLog log = dao.log;
		// plugin main class
		final String className = dao.yml.getString(this.classFieldName);
//		log.finer("Init: "+this.classFieldName+": "+className);
		if(utils.isEmpty(className)) {
			log.severe("Plugin doesn't contain a "+this.classFieldName+" field");
			dao.plugin = null;
			return;
		}
		// new instance
		log().info("Loading plugin: "+dao.name+" "+dao.yml.getPluginVersion());
		dao.plugin = loadPluginJar(dao, className);
		dao.plugin.doInit(this, dao.yml);
	}
	public xJavaPlugin loadPluginJar(final PluginDAO dao, final String className) {
		final xLog log = dao.log;
		// file to url
		final URL url;
		try {
			url = dao.file.toURI().toURL();
		} catch (MalformedURLException e) {
			log.trace(e);
			return null;
		}
		// java class loader
		final JarClassLoader jcl = new JarClassLoader();
		jcl.add(url);
		final String clssName =
			className.endsWith(".class")
			? className.substring(0, className.length() - 6)
			: className;
		// load class
		final Class<?> clss;
		try {
			clss = jcl.loadClass(clssName);
		} catch (ClassNotFoundException e) {
			log.trace(e);
			return null;
		}
		final Constructor<?> construct;
		try {
			construct = clss.getConstructor();
		} catch (NoSuchMethodException e) {
			log.trace(e);
			return null;
		} catch (SecurityException e) {
			log.trace(e);
			return null;
		}
		final xJavaPlugin plugin;
		try {
			plugin = (xJavaPlugin) construct.newInstance();
		} catch (InstantiationException e) {
			log.trace(e);
			return null;
		} catch (IllegalAccessException e) {
			log.trace(e);
			return null;
		} catch (IllegalArgumentException e) {
			log.trace(e);
			return null;
		} catch (InvocationTargetException e) {
			log.trace(e);
			return null;
		}
		return plugin;
	}



	public void unloadAll() {
		synchronized(this.plugins){
			for(final PluginDAO dao : this.plugins.values()) {
				try {
					dao.plugin.doUnload();
				} catch (Exception e) {
					dao.log.trace(e);
				}
			}
		}
	}
//	public void unload() {
//	}



	public void enableAll() {
		synchronized(this.plugins) {
			for(final PluginDAO dao : this.plugins.values()) {
				try {
					dao.plugin.doEnable();
				} catch (Exception e) {
					dao.log.trace(e);
				}
			}
		}
	}
	public void disableAll() {
		synchronized(this.plugins){
			for(final PluginDAO dao : this.plugins.values()) {
				try {
					dao.plugin.doDisable();
				} catch (Exception e) {
					dao.log.trace(e);
				}
			}
		}
	}



	public void addPlugin(final PluginDAO plugin) {
		if(plugin == null) throw new NullPointerException();
		synchronized(this.plugins) {
			this.plugins.put(plugin.name, plugin);
		}
	}
	public void removePlugin(final String name) {
		if(utils.isEmpty(name)) throw new NullPointerException();
		synchronized(this.plugins) {
			final PluginDAO dao = this.plugins.get(name);
			if(dao.plugin != null) {
				if(dao.plugin.isEnabled()) {
					try {
						dao.plugin.doDisable();
					} catch (Exception e) {
						dao.log.trace(e);
					}
				}
				try {
					dao.plugin.doUnload();
				} catch (Exception e) {
					dao.log.trace(e);
				}
			}
			this.plugins.remove(name);
		}
	}



	public void setClassField(final String field) {
		synchronized(this.plugins){
			this.classFieldName = field;
		}
	}



	// logger
	private volatile xLog _log = null;
	public xLog log() {
		if(this._log == null)
			this._log = xLog.getRoot("PluginManager");
		return this._log;
	}



}
