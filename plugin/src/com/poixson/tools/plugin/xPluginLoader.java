package com.poixson.app.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.jar.JarFile;

import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.context.DefaultContextLoader;
import org.xeustechnologies.jcl.context.JclContext;

import com.poixson.utils.Utils;
import com.poixson.utils.ioUtils;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLog;


public class xPluginLoader<T extends xJavaPlugin> {

	protected final xPluginManager<T> manager;

	private volatile String pluginMainClassKey = null;



	public xPluginLoader(final xPluginManager<T> manager) {
		if (manager == null) throw RequiredArgumentException.getNew("manager");
		this.manager = manager;
		final JarClassLoader jcl = new JarClassLoader();
		final DefaultContextLoader context = new DefaultContextLoader(jcl);
		context.loadContext();
	}



	protected xPluginYML LoadPluginYML(final File file) {
		if (file == null)   throw RequiredArgumentException.getNew("file");
		if (!file.exists()) throw new RuntimeException("Plugin file not found: "+file.getPath());
		JarFile jarFile = null;
		InputStream in  = null;
		xPluginYML yml  = null;
		try {
			jarFile = new JarFile(file);
			in = ioUtils.OpenFileFromJar(
				jarFile,
				"plugin.yml"
			);
			if (in == null)
				throw new IOException("Failed to load plugin.yml from: "+file.getName());
			final Map<String, Object> datamap =
				ioUtils.LoadYamlFromStream(in);
			if (datamap == null)
				throw new IOException("Failed to load yaml from jar file: "+file.getName());
			yml = new xPluginYML(
				datamap,
				this.pluginMainClassKey
			);
		} catch (IOException e) {
			this.log()
				.trace(e);
			return null;
		} finally {
			Utils.safeClose(in);
			Utils.safeClose(jarFile);
		}
		// validate yml key values
		if (Utils.isEmpty(yml.getPluginName())) {
			this.log()
				.warning(
					"Plugin name not set in plugin.yml: {}",
					file.getName()
				);
			return null;
		}
		if (Utils.isEmpty(yml.getMainClass())) {
			this.log()
				.warning(
					"'{}' not set in plugin.yml: {}",
					this.pluginMainClassKey,
					file.getName()
				);
			return null;
		}
		return yml;
	}
	protected T LoadPluginClass(final File file, final xPluginYML yml) {
		if (file == null)   throw RequiredArgumentException.getNew("file");
		if (yml  == null)   throw RequiredArgumentException.getNew("yml");
		if (!file.exists()) throw new RuntimeException("Plugin file not found: "+file.getPath());
		this.log()
			.info(
				"Loading plugin: {} {}",
				yml.getPluginName(),
				yml.getPluginVersion()
			);
		// check plugin already loaded
		if (this.manager != null) {
			if ( this.manager.isPluginLoaded(yml.getPluginName()) ) {
				this.log()
					.warning("Plugin already loaded: {}", yml.getPluginName());
				return null;
			}
		}
//TODO: plugins can depend on other plugins
//		// check required libraries
//		final Set<String> required;
//		try {
//			required = cfg.getStringSet("Requires");
//		} catch (Exception e) {
//			this.log()
//				.trace(e);
//			return null;
//		}
//		// check plugin dependencies
//		if (Utils.notEmpty(required)) {
//			for (final String libPath : required) {
//				if (!Utils.isLibAvailable(libPath)) {
//					this.log()
//						.fatal("Plugin requires library: {}", libPath);
//					return null;
//				}
//			}
//		}
		final String mainClassStr = yml.getMainClass();
		if (Utils.isBlank(mainClassStr))
			return null;
		final String classStr = (
			mainClassStr.endsWith(".class")
			? mainClassStr.substring(0, mainClassStr.length() - 6)
			: mainClassStr
		);
		// load plugin class
		final Class<T> clss;
		try {
			final JarClassLoader jcl = this.getJarClassLoader();
			final URL url = file.toURI().toURL();
			this.log()
				.detail("Adding to class-loader: {}", file.getName());
			jcl.add(url);
			this.log().detail("Attempting to load plugin main class: {}", classStr);
			clss = this.CastClass(
				jcl.loadClass(classStr)
			);
		} catch (ClassNotFoundException e) {
			this.log()
				.trace(e);
			return null;
		} catch (MalformedURLException e) {
			this.log()
				.trace(e);
			return null;
		}
		// get plugin class constructor
		final Constructor<T> construct;
		try {
			construct = clss.getConstructor();
		} catch (NoSuchMethodException | SecurityException e) {
			this.log()
				.trace(e);
			return null;
		}
		// new plugin class instance
		final T plugin;
		try {
			plugin = construct.newInstance();
		} catch (InstantiationException | IllegalAccessException
		| IllegalArgumentException | InvocationTargetException e) {
			this.log()
				.trace(e);
			return null;
		}
		plugin.init(this.manager, yml);
		// register with manager
		if (this.manager != null) {
			this.manager.register(plugin);
		}
		return plugin;
	}
	@SuppressWarnings("unchecked")
	protected Class<T> CastClass(final Class<?> clss) {
		return (Class<T>) clss;
	}



	public JarClassLoader getJarClassLoader() {
		return JclContext.get();
	}
	public JarClassLoader getJarClassLoader(final String group) {
		return JclContext.get(group);
	}
//TODO: default to isolated class loaders?
/*
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
*/



	public xPluginLoader<T> setPluginMainClassKey(final String key) {
		this.pluginMainClassKey = key;
		return this;
	}



	// logger
	public xLog log() {
		return (
			this.manager == null
			? xLog.getRoot()
			: this.manager.log()
		);
	}



}
