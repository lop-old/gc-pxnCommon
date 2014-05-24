package com.poixson.commonapp.config;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsDirFile;
import com.poixson.commonjava.xLogger.xLog;


public final class xConfigLoader {


	private xConfigLoader() {}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}


	// load generic yml file
	public static xConfig Load(final String file) {
		return Load(file, xConfig.class);
	}
	public static xConfig Load(final File file) {
		return Load(file, xConfig.class);
	}


	// load extended xConfig
	public static xConfig Load(final String file, final Class<? extends xConfig> clss) {
		if(file == null || file.isEmpty()) throw new NullPointerException();
		return Load(new File(file), clss);
	}
	@SuppressWarnings("resource")
	public static xConfig Load(final File file, final Class<? extends xConfig> clss) {
		if(file == null) throw new NullPointerException();
		if(clss == null) throw new NullPointerException();
		log().info("Loading config file: "+file.toString());
		final InputStream in = utilsDirFile.OpenFile(file);
		if(in == null) return null;
		return Load(in, clss);
	}


	// load from jar
	public static xConfig LoadJar(final File jarFile, final String ymlFile) {
		return LoadJar(jarFile, ymlFile, xConfig.class);
	}
	@SuppressWarnings("resource")
	public static xConfig LoadJar(final File jarFile, final String ymlFile, final Class<? extends xConfig> clss) {
		if(jarFile == null) throw new NullPointerException();
		final utilsDirFile.InputJar in = utilsDirFile.OpenJarResource(jarFile, ymlFile);
		if(in == null) return null;
		try {
			return Load(in.fileInput, clss);
		} finally {
			utils.safeClose(in);
		}
	}


	public static <T> xConfig Load(final InputStream in, final Class<? extends xConfig> clss) {
		if(in == null) throw new NullPointerException();
		try {
			final Yaml yml = new Yaml();
			@SuppressWarnings("unchecked")
			final Map<String, Object> data = (HashMap<String, Object>) yml.load(in);
			if(data == null || data.isEmpty())
				return null;
			@SuppressWarnings("unchecked")
			final Constructor<? extends Map<String, Object>> construct =
				(Constructor<? extends Map<String, Object>>) clss.getDeclaredConstructor(Map.class);
			return (xConfig) construct.newInstance(data);
		} catch (InstantiationException e) {
			log().trace(e);
		} catch (IllegalAccessException e) {
			log().trace(e);
		} catch (IllegalArgumentException e) {
			log().trace(e);
		} catch (InvocationTargetException e) {
			log().trace(e);
		} catch (NoSuchMethodException e) {
			log().trace(e);
		} catch (SecurityException e) {
			log().trace(e);
		} finally {
			utils.safeClose(in);
		}
		return null;
	}


	// logger
	public static xLog log() {
		return xLog.getRoot();
	}


}
