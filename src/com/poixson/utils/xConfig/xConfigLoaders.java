package com.poixson.utils.xConfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.poixson.utils.exceptions.RequiredArgumentException;


public final class xConfigLoaders {
	private xConfigLoaders() {}



	// new xConfig child instance
	public static <T extends xConfig> T newConfig(
			final Map<String, Object> datamap, final Class<T> cfgClass) {
		if (datamap == null) throw new RequiredArgumentException("datamap");
		// get construct
		final Constructor<? extends xConfig> construct;
		final Class<? extends xConfig> clss = (
			cfgClass == null
			? xConfig.class
			: cfgClass
		);
		try {
			construct = clss.getDeclaredConstructor(Map.class);
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



}
