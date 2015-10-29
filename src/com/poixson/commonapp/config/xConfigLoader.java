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
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.xLogger.xLog;


class xConfigLoader {
	public static final String LOG_NAME = "CONFIG";



//TODO: these need to be updated
//	// file
//	public static xConfig Load(final String file) {
//		return Load(
//				file,
//				xConfig.class
//		);
//	}
//	// file, class
//	public static xConfig Load(final String file, final Class<? extends xConfig> clss) {
//		return Load(
//				file,
//				clss,
//				(Class<? extends Object>) null
//		);
//	}
//	// file, class, injar
//	public static xConfig Load(final String file,
//			final Class<? extends xConfig> clss,
//			final Class<? extends Object>  checkInJar) {
//		return Load(
//				(String) null,
//				file,
//				clss,
//				checkInJar
//		);
//	}
//	// path, file, class, injar
//	public static xConfig Load(final String path, final String file,
//			final Class<? extends xConfig> clss,
//			final Class<? extends Object> checkInJar) {
//		return Load(
//				log(),
//				path,
//				file,
//				clss,
//				checkInJar
//		);
//	}
	public static xConfig Load(final xLog log,
			final String path, final String file,
			final Class<? extends xConfig> clss,
			final Class<? extends Object> checkInJar) {
		if(utils.isEmpty(file)) throw new NullPointerException("file argument is required!");
		if(clss == null)        throw new NullPointerException("clss argument is required!");
		// load file.yml
		{
			final String fullpath = (utils.isEmpty(path) ? "" : utilsString.ensureEnds(File.separator, path))+file;
			(log == null ? logger() : log)
				.fine("Loading config file: "+fullpath);
			final InputStream in = utilsDirFile.OpenFile(
					new File(fullpath)
			);
			if(in != null) {
				return LoadStream(
						in,
						clss
				);
			}
		}
		// try loading as resource
		if(checkInJar != null) {
			final String filepath = utilsString.ensureStarts(File.separator, file);
			(log == null ? logger() : log)
				.fine("Looking in jar for file: "+filepath+"  "+checkInJar.getName());
			final InputStream in = utilsDirFile.OpenResource(
					checkInJar,
					filepath
			);
			if(in != null) {
				(log == null ? logger() : log)
					.fine("Loaded config from jar: "+filepath);
				final xConfig config = LoadStream(
						in,
						clss
				);
				if(config != null) {
					config.loadedFromResource = true;
					Save(
							(utils.isEmpty(path) ? null : new File(path)),
							new File(file),
							config.datamap
					);
					return config;
				}
			}
		}
		(log == null ? logger() : log)
			.fine("Config file not found! "+file);
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
			return LoadStream(
					in.fileInput,
					clss
			);
		} finally {
			utils.safeClose(in);
		}
	}



	public static <T> xConfig LoadStream(final InputStream in, final Class<? extends xConfig> clss) {
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
			logger().trace(e);
		} finally {
			utils.safeClose(in);
		}
		return null;
	}



	public static boolean Save(final File file,
			final Map<String, Object> datamap) {
		return Save(
				(File) null,
				file,
				datamap
		);
	}
	public static boolean Save(final File path, final File file,
			final Map<String, Object> datamap) {
		if(file == null)           throw new NullPointerException("file argument is required!");
		if(utils.isEmpty(datamap)) throw new NullPointerException("datamap argument is required!");
		if(path != null && !path.isDirectory()) {
			if(path.mkdirs()) {
				logger().info("Created directory: "+path.toString());
			} else {
				logger().severe("Failed to create directory: "+path.toString());
				return false;
			}
		}
		final String filePath = utilsDirFile.buildFilePath(path, file);
		PrintWriter out = null;
		try {
			final Yaml yml = new Yaml();
			out = new PrintWriter(filePath);
			out.print(
					yml.dumpAs(datamap, Tag.MAP, FlowStyle.BLOCK)
			);
			logger().fine("Saved config file: "+filePath);
			return true;
		} catch (FileNotFoundException e) {
			logger().trace(e);
			return false;
		} finally {
			utils.safeClose(out);
		}
	}



	// logger
	private volatile xLog _log         = null;
	private static   xLog _log_default = null;
	public xLog log() {
		final xLog local = this._log;
		if(local != null)
			return local;
		return logger();
	}
	public void setLog(final xLog log) {
		this._log = log;
	}
	public static xLog logger() {
		if(_log_default == null)
			_log_default = xLog.getRoot(LOG_NAME);
		return _log_default;
	}



}
