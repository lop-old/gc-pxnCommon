package com.poixson.commonapp.config;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface xConfigInterface {


	public Object clone();

	public boolean exists(final String path);
	public boolean isFromResource();

	public Object get(final String path) throws xConfigException;
	public Object gt(final String path);

	public Map<String, Object> getDataPath(final String path) throws xConfigException;
	public Map<String, Object> getPath(final String path);

	// string
	public String getString(final String path) throws xConfigException;
	public String getStr(final String path, final String def);
	// boolean
	public Boolean getBoolean(final String path) throws xConfigException;
	public boolean getBool(final String path, final boolean def);
	// integer
	public Integer getInteger(final String path) throws xConfigException;
	public int getInt(final String path, final int def);
	// long
	public Long getLong(final String path) throws xConfigException;
	public long getLng(final String path, final long def);
	// double
	public Double getDouble(final String path) throws xConfigException;
	public double getDbl(final String path, final double def);
	// float
	public Float getFloat(final String path) throws xConfigException;
	public float getFlt(final String path, final float def);

	// list
	public <C> List<C> getList(final Class<? extends C> clss,
			final String path) throws xConfigException;
	public List<String> getStringList(final String path) throws xConfigException;

	// set
	public <T> Set<T> getSet(final Class<? extends T> clss,
			final String path) throws xConfigException;
	public Set<String> getStringSet(final String path) throws xConfigException;

	// map
	public <K, V> Map<K, V> getMap(final Class<? extends K> clssK,
			final Class<? extends V> clssV, final String path) throws xConfigException;
	public Map<String, Object> getStringObjectMap(final String path) throws xConfigException;
	public Map<String, String> getStringMap(final String path) throws xConfigException;

	// config list
	public List<? extends xConfig> getConfigList(final String path,
			final Class<? extends xConfig> clss) throws xConfigException;
//	public List<xConfig> getConfigList(final String path,
//			final Class<? extends xConfig> clss) throws xConfigException;


}
