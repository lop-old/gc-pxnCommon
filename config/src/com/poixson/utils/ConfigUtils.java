package com.poixson.utils;

import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.poixson.exceptions.RequiredArgumentException;


public final class ConfigUtils {
	private ConfigUtils() {}



	/**
	 * Load and parse yaml data from an input stream. 
	 * @param in InputStream to read from.
	 * @return Map<String, Object> datamap contents of yml file.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> LoadYamlFromStream(final InputStream in) {
		if (in == null) throw new RequiredArgumentException("in");
		final Yaml yml = new Yaml();
		final Map<String, Object> datamap =
			yml.loadAs(in, Map.class);
		return datamap;
	}



}
