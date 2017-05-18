package com.poixson.utils.xConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.poixson.utils.ObjectUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.exceptions.RequiredArgumentException;


public abstract class xConfigValues {

	protected final Map<String, Object> datamap;



	public xConfigValues(final Map<String, Object> datamap) {
		if (datamap == null) throw new RequiredArgumentException("datamap");
		this.datamap = new ConcurrentHashMap<String, Object>();
		this.datamap.putAll(datamap);
	}



	public boolean exists(final String key) {
		return this.datamap.containsKey(key);
	}



	// ------------------------------------------------------------------------------- //
	// data getters



	public Object get(final String key) {
		if (Utils.isEmpty(key))
			return null;
		return this.datamap.get(key);
	}
	public Object gt(final String key) {
		try {
			return this.get(key);
		} catch (Exception ignore) {}
		return null;
	}



//TODO: is this useful?
/*
	/ **
	 * Get an object from the stored data.
	 * @return Object
	 * @throws xConfigException
	 * /
	public Object get(final String path) throws xConfigException {
		if(utils.isEmpty(path))
			return null;
		try {
			return this.datamap.get(path);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	/ **
	 * Get an object from the stored data.
	 * @return Object or null on failure.
	 * /
	public Object gt(final String path) {
		try {
			return this.get(path);
		} catch (xConfigException ignore) {}
		return null;
	}



//	// get path recursively
//	@SuppressWarnings("unchecked")
//	protected Map<String, Object> getByPath(final String path) {
//		try {
//			if (path.contains(".")) {
//				Map<String, Object> map = this.datamap;
//				final StringRef str = new StringRef(path);
//				while(str.notEmpty()) {
//					final String part = StringUtils.getNextPart(".", str);
//					map = (Map<String, Object>) map.get(part);
//				}
//				return map;
//			}
//			return (Map<String, Object>) this.get(path);
//		} catch (Exception e) {
//			throw new xConfigException(e, path);
//		}
//	}
//	public Map<String, Object> getDatPth(final String path) {
//		try {
//			return this.getDataPath(path);
//		} catch (xConfigException ignore) {}
//		return null;
//	}



	/ **
	 * Get an object from the stored data by path.
	 * @return Object
	 * @throws xConfigException
	 * /
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getDataPath(final String path)
			throws xConfigException {
		try {
			if(path.contains(".")) {
				Map<String, Object> map = this.datamap;
				final StringRef str = new StringRef(path);
				while(str.notEmpty()) {
					final String part = utilsString.getNextPart(".", str);
					map = (Map<String, Object>) map.get(part);
				}
				return map;
			}
			return (Map<String, Object>) this.get(path);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	/ **
	 * Gets an object from the stored data by path.
	 * @return Object or null on failure
	 * /
	@Override
	public Map<String, Object> getPath(final String path) {
		try {
			return this.getDataPath(path);
		} catch (xConfigException ignoire) {}
		return null;
	}
*/



	// ------------------------------------------------------------------------------- //
	// primitives



	// string
	public String getString(final String key) {
		return (String) this.get(key);
	}
	public String getStr(final String key, final String defVal) {
		return this.getStr(
			key,
			defVal,
			false
		);
	}
	public String getStr(final String key, final String defVal, final boolean strict) {
		try {
			final String value = this.getString(key);
			if (strict) {
				if (value != null)
					return value;
			} else {
				if (Utils.notEmpty(value))
					return value;
			}
		} catch (Exception ignore) {}
		return defVal;
	}



	// boolean
	public Boolean getBoolean(final String key) {
		return (Boolean) this.get(key);
	}
	public boolean getBool(final String key, final boolean defVal) {
		try {
			final Boolean value = this.getBoolean(key);
			if (value != null)
				return value.booleanValue();
		} catch (Exception ignore) {}
		return defVal;
	}



	// integer
	public Integer getInteger(final String key) {
		return (Integer) this.get(key);
	}
	public int getInt(final String key, final int defVal) {
		try {
			final Integer value = this.getInteger(key);
			if (value != null)
				return value.intValue();
		} catch (Exception ignore) {}
		return defVal;
	}



	// long
	public Long getLong(final String key) {
		return (Long) this.get(key);
	}
	public long getLng(final String key, final long defVal) {
		try {
			final Long value = this.getLong(key);
			if (value != null)
				return value.longValue();
		} catch (Exception ignore) {}
		return defVal;
	}



	// double
	public Double getDouble(final String key) {
		return (Double) this.get(key);
	}
	public double getDbl(final String key, final double defVal) {
		try {
			final Double value = this.getDouble(key);
			if (value != null)
				return value.doubleValue();
		} catch (Exception ignore) {}
		return defVal;
	}



	// float
	public Float getFloat(final String key) {
		return (Float) this.get(key);
	}
	public float getFlt(final String key, final float defVal) {
		try {
			final Float value = this.getFloat(key);
			if (value != null)
				return value.floatValue();
		} catch (Exception ignore) {}
		return defVal;
	}



	// ------------------------------------------------------------------------------- //
	// set/list/map data getters



	// set
	public <C> Set<C> getSet(final String key, final Class<? extends C> clss) {
		return ObjectUtils.castSet(
				this.get(key),
				clss
		);
	}
	public Set<String> getStringSet(final String key) {
		return this.getSet(
				key,
				String.class
		);
	}



	// list
	public <C> List<C> getList(final String key, final Class<? extends C> clss) {
		return ObjectUtils.castList(
				this.get(key),
				clss
		);
	}
	public List<String> getStringList(final String key) {
		return this.getList(
				key,
				String.class
		);
	}



	// map
	public <K, V> Map<K, V> getMap(final String key,
			final Class<? extends K> clssK, final Class<? extends V> clssV) {
		return ObjectUtils.castMap(
				this.get(key),
				clssK,
				clssV
		);
	}
	public Map<String, Object> getStringObjectMap(final String key) {
		return this.getMap(
				key,
				String.class,
				Object.class
		);
	}
	public Map<String, String> getStringMap(final String key) {
		return this.getMap(
				key,
				String.class,
				String.class
		);
	}



	public List<? extends xConfig> getConfigList(final String key,
			final Class<? extends xConfig> clss) {
		final List<Object> datalist =
			this.getList(
				key,
				Object.class
			);
		if (datalist == null)
			return null;
		final List<xConfig> output = new ArrayList<xConfig>();
		final Iterator<Object> it = datalist.iterator();
		while (it.hasNext()) {
			final Object o = it.next();
			final Map<String, Object> datamap =
				ObjectUtils.castMap(
					o,
					String.class,
					Object.class
				);
			if (datamap == null)
				throw new RuntimeException("Failed to get Map constructor for class: "+clss.getName());
			final xConfig cfg =
				xConfigFactory
					.newConfigInstance(
						clss,
						datamap
					);
			output.add(cfg);
		}
		return output;
	}



	//TODO: remove this
//	// config list
//	public List<? extends xConfig> getConfigList(final String path,
//			final Class<? extends xConfig> clss) throws xConfigException;
//	public List<xConfig> getConfigList(final String path,
//			final Class<? extends xConfig> clss) throws xConfigException;



}
