package com.poixson.commonapp.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsDirFile;
import com.poixson.commonjava.xLogger.xLog;


public final class xConfigLoader {

	private xConfigLoader() {}



	// load generic yml file
	public static xConfig Load(final String file) {
		return Load(file, xConfig.class);
	}
	public static xConfig Load(final File file) {
		return Load(file, xConfig.class);
	}



	// load extended xConfig
	public static xConfig Load(final String file, final Class<? extends xConfig> clss) {
		return Load(file, clss, false);
	}
	public static xConfig Load(final String file, final Class<? extends xConfig> clss, boolean checkInJar) {
		if(utils.isEmpty(file)) throw new NullPointerException("file argument is required!");
		return Load(new File(file), clss, checkInJar);
	}
	public static xConfig Load(final File file, final Class<? extends xConfig> clss) {
		return Load(file, clss, false);
	}
	public static xConfig Load(final File file, final Class<? extends xConfig> clss, boolean checkInJar) {
		if(file == null) throw new NullPointerException("file argument is required!");
		if(clss == null) throw new NullPointerException("clss argument is required!");
		final String fileName = file.toString();
		// load file.yml
		{
			log().fine("Loading config file: "+fileName);
			final InputStream in = utilsDirFile.OpenFile(file);
			if(in != null)
				return Load(in, clss);
		}
		// try loading as resource
		if(checkInJar) {
			final InputStream in = utilsDirFile.OpenResource(fileName);
			if(in != null) {
				log().fine("Loaded config from jar: "+fileName);
				final xConfig config = Load(in, clss);
				if(config != null) {
					config.loadedFromResource = true;
					Save(file, config.datamap);
					return config;
				}
			}
		}
		return null;
	}



	// load from jar
	public static xConfig LoadJar(final File jarFile, final String ymlFile) {
		return LoadJar(jarFile, ymlFile, xConfig.class);
	}
	public static xConfig LoadJar(final File jarFile, final String ymlFile, final Class<? extends xConfig> clss) {
		if(jarFile == null)        throw new NullPointerException("jarFile argument is required!");
		if(utils.isEmpty(ymlFile)) throw new NullPointerException("yamlFile argument is required!");
		if(clss == null)           throw new NullPointerException("clss argument is required!");
		final utilsDirFile.InputJar in = utilsDirFile.OpenJarResource(jarFile, ymlFile);
		if(in == null) return null;
		try {
			return Load(in.fileInput, clss);
		} finally {
			utils.safeClose(in);
		}
	}



	public static <T> xConfig Load(final InputStream in, final Class<? extends xConfig> clss) {
		if(in   == null) throw new NullPointerException("in argument is required!");
		if(clss == null) throw new NullPointerException("clss argument is required!");
		try {
			final Yaml yml = new Yaml();
			@SuppressWarnings("unchecked")
			final Map<String, Object> datamap = (HashMap<String, Object>) yml.load(in);
			if(utils.isEmpty(datamap))
				return null;
			@SuppressWarnings("unchecked")
			final Constructor<? extends Map<String, Object>> construct =
				(Constructor<? extends Map<String, Object>>) clss.getDeclaredConstructor(Map.class);
			return (xConfig) construct.newInstance(datamap);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
				InvocationTargetException | NoSuchMethodException | SecurityException e) {
			log().trace(e);
		} finally {
			utils.safeClose(in);
		}
		return null;
	}
	public static boolean Save(final File file, final Map<String, Object> datamap) {
		if(file == null)           throw new NullPointerException("file argument is required!");
		if(utils.isEmpty(datamap)) throw new NullPointerException("datamap argument is required!");
		final Yaml yml = new Yaml();
		PrintWriter out = null;
		try {
			out = new PrintWriter(file);
			out.print(
				yml.dumpAs(datamap, Tag.MAP, FlowStyle.BLOCK)
			);
			log().fine("Saved config file: "+file.toString());
			return true;
		} catch (FileNotFoundException e) {
			log().trace(e);
			return false;
		} finally {
			utils.safeClose(out);
		}
	}



	// logger
	public static xLog log() {
		return xLog.getRoot();
	}



}
