package com.poixson.utils.xConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.ioUtils;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLog;


public final class xConfigLoaders {
	private xConfigLoaders() {}



	// new xConfig child instance
	public static <T extends xConfig> T newConfig(
			final Map<String, Object> datamap, final Class<T> cfgClass) {
		if (datamap == null)  throw new RequiredArgumentException("datamap");
		if (cfgClass == null) throw new RequiredArgumentException("cfgClass");
		// get construct
		final Constructor<? extends xConfig> construct;
		try {
			construct = cfgClass.getDeclaredConstructor(Map.class);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		if (construct == null)
			throw new RuntimeException("xConfig constructor not found!");
		// get new instance
		try {
			return castConfig(
				construct.newInstance(datamap)
			);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	@SuppressWarnings("unchecked")
	private static <T extends xConfig> T castConfig(final Object obj) {
		if (obj == null)
			return null;
		return (T) obj;
	}



	// from file
	public static <T extends xConfig> T fromFile(
			final String filePath, final Class<T> cfgClass) {
		if (Utils.isEmpty(filePath)) throw new RequiredArgumentException("filePath");
		if (cfgClass == null)        throw new RequiredArgumentException("cfgClass");
		final String fileStr = StringUtils.ForceEnds(".yml", filePath);
		final File file = new File(fileStr);
		if (!file.isFile())
			return null;
		InputStream in = null;
		try {
			in = ioUtils.OpenFile(fileStr);
			if (in == null)
				return null;
			final Map<String, Object> datamap =
				ioUtils.LoadYamlFromStream(in);
			if (datamap == null)
				return null;
			final T cfg =
				newConfig(
					datamap,
					cfgClass
				);
			return cfg;
		} catch (FileNotFoundException ignore) {
		} finally {
			Utils.safeClose(in);
		}
		return null;
	}



	// from jar resource
	public static <T extends xConfig> T fromJar(
			final String filePath, final Class<T> cfgClass) {
		if (Utils.isEmpty(filePath)) throw new RequiredArgumentException("filePath");
		if (cfgClass == null)        throw new RequiredArgumentException("cfgClass");
		final String fileStr = StringUtils.ForceEnds(".yml", filePath);
		InputStream in = null;
		try {
			in = ioUtils.OpenResource(cfgClass, fileStr);
			if (in == null)
				return null;
			final Map<String, Object> datamap =
				ioUtils.LoadYamlFromStream(in);
			if (datamap == null)
				return null;
			final T cfg =
				newConfig(
					datamap,
					cfgClass
				);
			return cfg;
		} finally {
			Utils.safeClose(in);
		}
	}



	// from file or jar (copy from jar to filesystem if doesn't exist)
	public static <T extends xConfig> T fromFileOrJar(
			final String filePath, final Class<T> cfgClass)
			throws CreateDefaultYmlFileException {
		if (Utils.isEmpty(filePath)) throw new RequiredArgumentException("filePath");
		if (cfgClass == null)        throw new RequiredArgumentException("cfgClass");
		final String fileStr = StringUtils.ForceEnds(".yml", filePath);
		try {
			// attempt loading from file
			{
				final T cfg = fromFile(fileStr, cfgClass);
				if (cfg != null)
					return cfg;
			}
			// attempt loading from resource
			{
				final T cfg = fromJar(fileStr, cfgClass);
				if (cfg != null) {
					// copy default file
					try {
						xLog.getRoot()
							.info("Creating default file: {}", fileStr);
						ioUtils.ExportResource(
							fileStr,
							ioUtils.OpenResource(cfgClass, fileStr)
						);
					} catch (Exception e) {
						throw new CreateDefaultYmlFileException(fileStr, e);
					}
					return cfg;
				}
			}
		} catch (Exception e) {
			xLog.getRoot()
				.trace(e);
		}
		return null;
	}



}
