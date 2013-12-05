package com.poixson.commonjava.Language;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.Utils.utilsDirFile;
import com.poixson.commonjava.Utils.utilsSan;
import com.poixson.commonjava.xLogger.xLog;
import com.poixson.webxbukkit.WebAPI;


public abstract class xLanguage {

	protected final HashMap<String, String> phrases = new HashMap<String, String>();
	protected volatile String lang = null;


	public xLanguage() {
		reloadDefaults();
	}
	public void reloadDefaults() {
		synchronized(phrases) {
			phrases.clear();
			this.defaults();
		}
	}


	// default phrases
	protected abstract void defaults();
	protected void addDefault(String name, String msg) {
		if(name == null || name.isEmpty()) throw new NullPointerException("name cannot be null");
		if(msg  == null || msg.isEmpty() ) throw new NullPointerException("msg cannot be null");
		phrases.put(name, msg);
	}


	// load language file
	public void load(Plugin plugin, String lang) {
		// defaults only
		if(lang == null || lang.isEmpty()) {
			reloadDefaults();
			return;
		}
		//if(lang.length() != 2) throw new IllegalArgumentException("lang must be specified as 2 letters");
		if(plugin == null)
			plugin = WebAPI.get();
		lang = utilsSan.FileName(lang);
		YamlConfiguration yml = loadYml(plugin, lang);
		if(yml == null) {
			log().warning("Failed to load "+lang+".yml");
			return;
		}
		synchronized(phrases) {
			// load messages/phrases
			reloadDefaults();
			for(String key : yml.getKeys(false))
				phrases.put(key, yml.getString(key));
		}
		this.lang = lang;
	}


	// find and load lang.yml
	private YamlConfiguration loadYml(Plugin plugin, String lang) {
		YamlConfiguration yml = null;
		// load from plugins/name/languages/lang.yml
		yml = loadFromFileSystem(plugin, lang);
		if(yml != null) {
			log().stats("Loaded language file "+lang+".yml");
			return yml;
		}
		// load from jar resource
		yml = loadFromResource(plugin, lang);
		if(yml != null) {
			log().stats("Loaded language resource "+lang+".yml");
			return yml;
		}
		return null;
	}
	// load from plugins/name/languages/lang.yml
	private YamlConfiguration loadFromFileSystem(Plugin plugin, String lang) {
		// build file path
		String langDir = utilsDirFile.mergePaths(
			WebAPI.getPluginDir(plugin),
			"languages"
		);
		File file = new File(
			utilsDirFile.buildFilePath(
				langDir,
				lang,
				".yml"
			)
		);
		// file exists
		if(!file.exists() || !file.canRead())
			return null;
		return YamlConfiguration.loadConfiguration(file);
	}
	// load from jar resource
	private YamlConfiguration loadFromResource(Plugin plugin, String lang) {
		String path = "languages/"+lang+".yml";
		InputStream stream = plugin.getClass().getClassLoader().getResourceAsStream(path);
		if(stream == null)
			return null;
		return YamlConfiguration.loadConfiguration(stream);
	}


	// get message/phrase
	public String getMsg(String key) {
		if(key == null || key.isEmpty()) throw new NullPointerException("key cannot be null");
		synchronized(phrases) {
			if(phrases.containsKey(key)) {
				String phrase = phrases.get(key);
				if(phrase != null && !phrase.isEmpty())
					return phrase;
			}
		}
		log().severe("Language message/phrase not found: "+key);
		return "<lang:"+key+">";
	}


	// logger
	private static volatile xLog _log = null;
	private static final Object logLock = new Object();
	public static xLog log() {
		if(_log == null) {
			synchronized(logLock) {
				if(_log == null)
					_log = xVars.log();
			}
		}
		return _log;
	}
	public static void setLog(xLog log) {
		synchronized(logLock) {
			_log = log;
		}
	}


}
