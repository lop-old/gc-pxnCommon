/*
package com.poixson.commonapp.plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.context.DefaultContextLoader;

import com.poixson.commonapp.config.xConfig;
import com.poixson.commonapp.config.xConfigException;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsDirFile;
import com.poixson.commonjava.Utils.exceptions.RequiredArgumentException;
import com.poixson.commonjava.xLogger.xLog;


public class xPluginManager {
//	private static final String LOG_NAME = "PluginManager";
	private static final boolean INDEPENDENT_CLASS_LOADERS = false;

	private static volatile xPluginManager manager = null;
	private static final Object lock = new Object();

	private volatile JarClassLoader jcl = null;
	private final Object jclLock = new Object();



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



	// get jar class loader
	protected JarClassLoader getJarClassLoader() {
		// independent
		if(INDEPENDENT_CLASS_LOADERS)
			return new JarClassLoader();
		// shared context
		if(this.jcl == null) {
			synchronized(this.jclLock) {
				if(this.jcl == null) {
					this.jcl = new JarClassLoader();
					final DefaultContextLoader context = new DefaultContextLoader(this.jcl);
					context.loadContext();
				}
			}
		}
		return this.jcl;
	}



	// load all plugins from dir
	public void loadAll() {
		this.loadAll((String) null);
	}
	public void loadAll(final String dir) {
		this.loadAll(new File(
				utils.isEmpty(dir) ? "plugins/" : dir
		));
	}
	public void loadAll(final File dir) {
		if(dir == null) throw new RequiredArgumentException("dir");
		// create plugins dir if needed
		if(!dir.isDirectory())
			dir.mkdir();
		// list dir contents
		final File[] files = utilsDirFile.listContents(dir, ".jar");
		if(files == null) throw new RuntimeException("Failed to load jar file contents!");
		// no plugins found
		if(files.length == 0) {
			log().warning("No plugins found to load.");
			return;
		}
		// load plugin jars
		int count = 0;
		synchronized(this.plugins) {
			for(final File f : files) {
				final PluginDAO dao = this.load(f);
				if(dao == null) continue;
				// plugin already loaded
				if(this.plugins.containsKey(dao.name)) {
					log().warning("Plugin already loaded with name: "+dao.name);
					continue;
				}
				log().finest("Found plugin file: "+f.toString());
				this.plugins.put(dao.name, dao);
				count++;
			}
		}
		log().info("Found [ "+Integer.toString(count)+" ] plugins.");
	}
	public PluginDAO load(final File file) {
		if(file == null) throw new RequiredArgumentException("file");
		if(!file.exists()) {
			log().warning("Plugin file not found: "+file.toString());
			return null;
		}
		// load plugin.yml from jar
		final xPluginYML yml = (xPluginYML) xConfig.LoadJar(
				file,
				"plugin.yml",
				xPluginYML.class
		);
		if(yml == null) {
			log().warning("plugin.yml not found in plugin: "+file.toString());
			return null;
		}
		final String pluginName = yml.getPluginName();
		if(utils.isEmpty(pluginName)) {
			log().warning("Plugin name not set: "+file.toString());
			return null;
		}
		// check required libraries
		final List<String> required;
		try {
			required = yml.getStringList("Requires");
		} catch (xConfigException e) {
			log().trace(e);
			return null;
		}
		if(utils.notEmpty(required)) {
			for(final String libPath : required) {
				if(!utils.isLibAvailable(libPath)) {
					log().fatal("Plugin requires library: "+libPath);
					return null;
				}
			}
		}
		// plugin jar loaded
		return new PluginDAO(
			pluginName,
			file,
			yml
		);
	}



	// init all plugins
	public void initAll() {
		this.initAll("Main Class");
	}
	public void initAll(final String classField) {
		if(utils.isEmpty(classField)) throw new RequiredArgumentException("classField");
		// init plugins
		synchronized(this.plugins) {
			for(final PluginDAO dao : this.plugins.values()) {
				if(dao.plugin != null) continue;
				this.init(dao);
			}
		}
	}
	public void init(final PluginDAO dao) {
		if(dao == null) throw new RequiredArgumentException("dao");
		final xLog log = dao.log;
		// already inited
		if(dao.plugin != null) {
			log.finer("Plugin already inited");
			return;
		}
		// plugin main class
		final String className = dao.yml.getStr(this.classFieldName, null);
//		log.finest("Init Plugin: "+this.classFieldName+": "+className);
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
		final JarClassLoader jcl = this.getJarClassLoader();
		jcl.add(url);
		final String clssNam =
			className.endsWith(".class")
			? className.substring(0, className.length() - 6)
			: className;
		// load class
		final Class<?> clss;
		try {
			clss = jcl.loadClass(clssNam);
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
					dao.log.finest(this.classFieldName+": "+dao.plugin.getClass().getName());
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
		if(plugin == null) throw new RequiredArgumentException("plugin");
		synchronized(this.plugins) {
			this.plugins.put(plugin.name, plugin);
		}
	}
	public void removePlugin(final String name) {
		if(utils.isEmpty(name)) throw new RequiredArgumentException("name");
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
	private static volatile xLog _log = null;
	public static xLog log() {
		if(_log == null)
			_log = xLog.getRoot();
		return _log;
	}



}
*/
