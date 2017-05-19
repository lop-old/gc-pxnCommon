package com.poixson.utils.xConfig;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.poixson.utils.DirsFiles;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLog;


public class xConfigFactory {

	private xLog log = null;

	private String fileName    = null;
	private String filePath    = null;
	private String packagePath = null;

	private Class<? extends xConfig> cfgClass    = null;
	private Class<? extends Object>  jarResClass = null;

	public static enum FileSource {
		FILESYSTEM,
		RESOURCE,
		AUTO
	};
	private FileSource source = FileSource.AUTO;



	// new xConfig child instance
	public static xConfig newConfigInstance(
			final Map<String, Object> datamap) {
		return newConfigInstance(null, datamap);
	}
	public static xConfig newConfigInstance(
			final Class<? extends xConfig> cfgClass,
			final Map<String, Object> datamap) {
		if (datamap == null)  throw new RequiredArgumentException("datamap");
		// get constructor
		final Constructor<? extends xConfig> construct;
		try {
			final Class<? extends xConfig> clss = (
				cfgClass == null
				? xConfig.class
				: cfgClass
			);
			construct = clss.getDeclaredConstructor(Map.class);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		if (construct == null)
			throw new RuntimeException("xConfig constructor not found!");
		// get new instance
		final xConfig config;
		try {
			config = construct.newInstance(datamap);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return config;
	}



	public static xConfigFactory builder() {
		return new xConfigFactory();
	}
	public xConfigFactory() {
	}
	@Override
	public xConfigFactory clone() {
		final xConfigFactory factory = builder();
		{
			final xLog   log         = this.log;
			final String fileName    = this.fileName;
			final String filePath    = this.filePath;
			final String packagePath = this.packagePath;
			final Class<? extends xConfig> cfgClass    = this.cfgClass;
			final Class<? extends Object>  jarResClass = this.jarResClass;
			if (log != null)
				factory.setLog(log);
			if (Utils.notEmpty(fileName))
				factory.setFileName(fileName);
			if (Utils.notEmpty(filePath))
				factory.setFilePath(filePath);
			if (Utils.notEmpty(packagePath))
				factory.setPackagePath(packagePath);
			if (cfgClass != null)
				factory.setConfigClass(cfgClass);
			if (jarResClass != null)
				factory.setJarResourceClass(jarResClass);
		}
		return factory;
	}



	// ------------------------------------------------------------------------------- //
	// loaders



	// quick helpers
	public xConfig loadFile() {
		this.setFileSource(FileSource.FILESYSTEM);
		return this.load();
	}
	public xConfig loadResource() {
		this.setFileSource(FileSource.RESOURCE);
		return this.load();
	}
	public xConfig loadAuto() {
		this.setFileSource(FileSource.AUTO);
		return this.load();
	}



	public xConfig load() {
		final FileSource source = this.source;
		final String fileName   = this.fileName;
		if (source == null)          throw new RequiredArgumentException("source");
		if (Utils.isEmpty(fileName)) throw new RequiredArgumentException("fileName");
		// load from file
		if (source.equals(FileSource.FILESYSTEM) || source.equals(FileSource.AUTO)) {
			final Map<String, Object> datamap =
				this._loadFromFile();
			if (datamap != null) {
				final xConfig cfg =
					newConfigInstance(
						this.cfgClass,
						datamap
					);
				return cfg;
			}
		}
		// load from jar resource
		if (source.equals(FileSource.RESOURCE) || source.equals(FileSource.AUTO)) {
			final Map<String, Object> datamap =
				this._loadFromJar();
			if (datamap != null) {
				final xConfig cfg =
					newConfigInstance(
						this.cfgClass,
						datamap
					);
				return cfg;
			}
		}
		return null;
	}
	private Map<String, Object> _loadFromFile() {
		final xLog log = this.log();
		final String path =
			DirsFiles.buildFilePath(
				this.filePath,
				this.fileName,
				"yml"
			);
		InputStream in = null;
		try {
			in = DirsFiles.OpenFile(path);
			if (in != null) {
				if (log != null) {
					log.finer("Loaded config file: "+path);
				}
				return this._loadFromStream(in);
			}
		} finally {
			Utils.safeClose(in);
		}
		return null;
	}
	private Map<String, Object> _loadFromJar() {
		final xLog log = this.log();
		final Class<? extends Object> jarResClass = this.getJarResourceClass();
		final String path =
			StringUtils.ForceStarts(
				"/",
				DirsFiles.buildFilePath(
					this.packagePath,
					this.fileName,
					"yml"
				)
			);
		InputStream in = null;
		try {
			in = DirsFiles.OpenResource(
				jarResClass,
				path
			);
			if (in != null) {
				if (log != null) {
					log.fine("Loaded config from jar: "+path);
				}
				return this._loadFromStream(in);
			}
		} finally {
			Utils.safeClose(in);
		}
		return null;
//TODO: is this useful?
//			final xConfig cfg = this.loadFromStream(in);
//cfg.setFromResource();
//			if (this.source.equals(FileSource.AUTO)) {
//				Save(
//					(utils.isEmpty(path) ? null : new File(path)),
//					new File(file),
//					config.datamap
//				);
//			}
	}
	private Map<String, Object> _loadFromStream(final InputStream in) {
		if (in == null) throw new NullPointerException();
		final Yaml yml = new Yaml();
		try {
			@SuppressWarnings("unchecked")
			final Map<String, Object> datamap =
				(HashMap<String, Object>)
				yml.load(in);
			return datamap;
		} finally {
			Utils.safeClose(in);
		}
	}



//TODO:
/*
	/ **
	 * Loads a .yml config file from the file system or a jar resource.
	 * @param log The logger to use, or default to null
	 * @param path File system path to the config file
	 * @param file Name of the config file to load
	 * @param clss xConfig class to create to handle loading the config
	 * @param checkInJar A class contained in the jar in which to look
	 *        for a default config file
	 * @return xConfig instance containing the loaded yaml data
	 * @throws xConfigException
	 * /



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
		if(file == null)           throw new RequiredArgumentException("file");
		if(utils.isEmpty(datamap)) throw new RequiredArgumentException("datamap");
		if(path != null && !path.isDirectory()) {
			if(path.mkdirs()) {
				getLogger().info("Created directory: "+path.toString());
			} else {
				getLogger().severe("Failed to create directory: "+path.toString());
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
			getLogger().fine("Saved config file: "+filePath);
			return true;
		} catch (FileNotFoundException e) {
			getLogger().trace(e);
			return false;
		} finally {
			utils.safeClose(out);
		}
	}
*/



	// ------------------------------------------------------------------------------- //
	// factory configs



	// file name
	public xConfigFactory setFileName(final String fileStr) {
		this.fileName = fileStr;
		return this;
	}
	// file path
	public xConfigFactory setFilePath(final String pathStr) {
		this.filePath = pathStr;
		return this;
	}
	// jar package path
	public xConfigFactory setPackagePath(final String packagePath) {
		this.packagePath = packagePath;
		return this;
	}



	// definition class
	public xConfigFactory setConfigClass(final Class<? extends xConfig> cfgClass) {
		this.cfgClass = cfgClass;
		return this;
	}
	public Class<? extends xConfig> getConfigClass() {
		final Class<? extends xConfig> cfgClass = this.cfgClass;
		return (
			cfgClass == null
			? xConfig.class
			: cfgClass
		);
	}



	// class contained in source jar
	public xConfigFactory setJarResourceClass(final Class<? extends Object> jarResClass) {
		this.jarResClass = jarResClass;
		return this;
	}
	public Class<? extends Object> getJarResourceClass() {
		final Class<? extends Object> jarResClass = this.jarResClass;
		return (
			jarResClass == null
			? xConfig.class
			: jarResClass
		);
	}



	// config file source
	public xConfigFactory setFileSource(final FileSource source) {
		if (source == null) throw new RequiredArgumentException("source");
		this.source = source;
		return this;
	}
	public xConfigFactory enableFilesystemSource() {
		final FileSource source = this.source;
		if (source == null) {
			return this.setFileSource(FileSource.FILESYSTEM);
		}
		if (source.equals(FileSource.RESOURCE)) {
			return this.setFileSource(FileSource.AUTO);
		}
		return this;
	}
	public xConfigFactory enableResource() {
		final FileSource source = this.source;
		if (source == null) {
			return this.setFileSource(FileSource.RESOURCE);
		}
		if (source.equals(FileSource.FILESYSTEM)) {
			return this.setFileSource(FileSource.AUTO);
		}
		return this;
	}
	public xConfigFactory disableFilesystem() {
		return this.setFileSource(FileSource.RESOURCE);
	}
	public xConfigFactory disableResource() {
		return this.setFileSource(FileSource.FILESYSTEM);
	}



	// optional logger
	public xConfigFactory setLog(final xLog log) {
		this.log = log;
		return this;
	}
	public xLog log() {
		return this.log;
	}



}
