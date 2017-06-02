package com.poixson.app.plugin;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.poixson.utils.Utils;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLog;


public class xPluginManager<T extends xJavaPlugin> {
	private static final String LOG_NAME = "xPluginManager";

	private final ConcurrentMap<String, T> plugins =
		new ConcurrentHashMap<String, T>();



	public xPluginManager() {}



	/**
	 * Adds a plugin to the manager.
	 * @param plugin A new plugin instance to manage.
	 * @return true if successful, false if plugin already registered.
	 */
	public boolean register(final T plugin) {
		if (plugin == null) throw new RuntimeException("Missing plugin instance!");
		final String pluginName = plugin.getPluginName();
		final T existing =
			this.plugins.putIfAbsent(pluginName, plugin);
		if (existing != null) {
			this.log()
				.warning("Plugin already loaded: {}", pluginName);
			return false;
		}
		plugin.onInit();
		return true;
	}



	public boolean unregister(final String pluginName) {
		if (Utils.isBlank(pluginName))
			return false;
		final T existing = this.plugins.remove(pluginName);
		return (existing != null);
	}
	public boolean unregister(final T plugin) {
		if (plugin == null)
			return false;
		final String pluginName = plugin.getPluginName();
		return this.unregister(pluginName);
	}
	public void unloadAll() {
		final Iterator<Entry<String, T>> it = this.plugins.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<String, T> entry = it.next();
			final T plugin = entry.getValue();
			this.disable(plugin);
			this.unregister(plugin);
		}
	}



	public boolean isPluginLoaded(final String pluginName) {
		if (Utils.isBlank(pluginName)) throw new RequiredArgumentException("pluginName");
		return
			this.plugins
				.containsKey(pluginName);
	}
	public boolean isPluginLoaded(final T plugin) {
		if (plugin == null) throw new RequiredArgumentException("plugin");
		return
			this.isPluginLoaded(
				plugin.getPluginName()
			);
	}



	public T getPlugin(final String pluginName) {
		if (Utils.isEmpty(pluginName))
			return null;
		return this.plugins.get(pluginName);
	}



	public void enableAll() {
		final int count = this.plugins.size();
		this.log()
			.info(
				"Starting [ {} ] plugin{}..",
				count,
				(count == 1 ? "" : "s")
			);
		final Iterator<Entry<String, T>> it = this.plugins.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<String, T> entry = it.next();
			final T plugin = entry.getValue();
			plugin.doEnable();
		}
	}
	public void enable(final T plugin) {
		if (plugin == null) throw new RequiredArgumentException("plugin");
		plugin.doEnable();
	}
	public void enable(final String pluginName) {
		if (Utils.isEmpty(pluginName)) throw new RequiredArgumentException("pluginName");
		final T plugin = this.getPlugin(pluginName);
		if (plugin == null) throw new RuntimeException("Unknown plugin: "+pluginName);
		this.enable(plugin);
	}



	public void disableAll() {
		final Iterator<Entry<String, T>> it = this.plugins.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<String, T> entry = it.next();
			final T plugin = entry.getValue();
			plugin.doDisable();
		}
	}
	public void disable(final T plugin) {
		if (plugin == null) throw new RequiredArgumentException("plugin");
		plugin.doDisable();
	}
	public void disable(final String pluginName) {
		if (Utils.isEmpty(pluginName)) throw new RequiredArgumentException("pluginName");
		final T plugin = this.getPlugin(pluginName);
		if (plugin == null) throw new RuntimeException("Unknown plugin: "+pluginName);
		this.disable(plugin);
	}



	// logger
	private volatile SoftReference<xLog> _log = null;
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null)
				return log;
		}
		final xLog log =
			xLog.getRoot()
				.get(LOG_NAME);
		this._log = new SoftReference<xLog>(log);
		return log;
	}



}
