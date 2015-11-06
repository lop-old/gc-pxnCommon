package com.poixson.commonapp.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsObject;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.byRef.StringRef;
import com.poixson.commonjava.Utils.exceptions.RequiredArgumentException;


public class xConfig extends xConfigLoader implements xConfigInterface {

	protected final Map<String, Object> datamap;

	protected volatile boolean loadedFromResource = false;



	/**
	 * Stores the datamap Map object for this xConfig instance.
	 * @param datamap The Map instance to use
	 */
	public xConfig(final Map<String, Object> datamap)
			throws xConfigException {
		if(datamap == null) throw new RequiredArgumentException("datamap");
		this.datamap = datamap;
	}
	/**
	 * Clones a xConfig instance to a new instance.
	 * @return xConfig instance
	 */
	@Override
	public xConfig clone() {
		try {
			return new xConfig(this.datamap);
		} catch (xConfigException ignore) {}
		return null;
	}



	/**
	 * Check if path exists in loaded config.
	 * @return true if path exists, false if not found
	 */
	@Override
	public boolean exists(final String path) {
		return this.datamap.containsKey(path);
	}
	/**
	 * @return true if config data was loaded from a jar resource,
	 *         false if config file found in filesystem
	 */
	@Override
	public boolean isFromResource() {
		return this.loadedFromResource;
	}



	/**
	 * Get an object from the stored data.
	 * @return Object
	 * @throws xConfigException
	 */
	@Override
	public Object get(final String path) throws xConfigException {
		if(utils.isEmpty(path))
			return null;
		try {
			return this.datamap.get(path);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	/**
	 * Get an object from the stored data.
	 * @return Object or null on failure
	 */
	@Override
	public Object gt(final String path) {
		try {
			return this.get(path);
		} catch (xConfigException ignore) {}
		return null;
	}



	/**
	 * Get an object from the stored data by path.
	 * @return Object
	 * @throws xConfigException
	 */
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
	/**
	 * Gets an object from the stored data by path.
	 * @return Object or null on failure
	 */
	@Override
	public Map<String, Object> getPath(final String path) {
		try {
			return this.getDataPath(path);
		} catch (xConfigException ignoire) {}
		return null;
	}



	// ------------------------------------------------------------------------------- //
	// primitive types



	/**
	 * Get a string value from the stored data.
	 * @return String value from config
	 * @throws xConfigException
	 */
	@Override
	public String getString(final String path) throws xConfigException {
		try {
			return (String) this.get(path);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	/**
	 * Get a string value from the stored data.
	 * @return String value from config or null on failure
	 */
	@Override
	public String getStr(final String path, final String def) {
		try {
			final String value = this.getString(path);
			if(utils.notEmpty(value))
				return value;
		} catch (xConfigException ignore) {}
		return def;
	}



	/**
	 * Get a boolean value from the stored data.
	 * @return Boolean value from config
	 * @throws xConfigException
	 */
	@Override
	public Boolean getBoolean(final String path) throws xConfigException {
		try {
			return (Boolean) this.get(path);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	/**
	 * Get a boolean value from the stored data.
	 * @return boolean value from config or null on failure
	 */
	@Override
	public boolean getBool(final String path, final boolean def) {
		try {
			final Boolean value = this.getBoolean(path);
			if(value != null)
				return value.booleanValue();
		} catch (xConfigException ignore) {}
		return def;
	}



	/**
	 * Get an integer value from the stored data.
	 * @return Integer value from config
	 * @throws xConfigException
	 */
	@Override
	public Integer getInteger(final String path) throws xConfigException {
		try {
			return (Integer) this.get(path);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	/**
	 * Get an int value from the stored data.
	 * @return int value from config or null on failure
	 */
	@Override
	public int getInt(final String path, final int def) {
		try {
			final Integer value = this.getInteger(path);
			if(value != null)
				return value.intValue();
		} catch (xConfigException ignore) {}
		return def;
	}



	/**
	 * Get a long value from the stored data.
	 * @return Long value from config
	 * @throws xConfigException
	 */
	@Override
	public Long getLong(final String path) throws xConfigException {
		try {
			return (Long) this.get(path);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	/**
	 * Get a long value from the stored data.
	 * @return long value from config or null on failure
	 */
	@Override
	public long getLng(final String path, final long def) {
		try {
			final Long value = this.getLong(path);
			if(value != null)
				return value.longValue();
		} catch (xConfigException ignore) {}
		return def;
	}



	/**
	 * Get a double value from the stored data.
	 * @return Double value from config
	 * @throws xConfigException
	 */
	@Override
	public Double getDouble(final String path) throws xConfigException {
		try {
			return (Double) this.get(path);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	/**
	 * Get a double value from the stored data.
	 * @return double value from config or null on failure
	 */
	@Override
	public double getDbl(final String path, final double def) {
		try {
			final Double value = this.getDouble(path);
			if(value != null)
				return value.doubleValue();
		} catch (xConfigException ignore) {}
		return def;
	}



	/**
	 * Get a float value from the stored data.
	 * @return Float value from config
	 * @throws xConfigException
	 */
	@Override
	public Float getFloat(final String path) throws xConfigException {
		try {
			return (Float) this.get(path);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	/**
	 * Get a float value from the stored data.
	 * @return float value from config or null on failure
	 */
	@Override
	public float getFlt(final String path, final float def) {
		try {
			final Float value = this.getFloat(path);
			if(value != null)
				return value.floatValue();
		} catch (xConfigException ignore) {}
		return def;
	}



	// ------------------------------------------------------------------------------- //
	// set/list/map



	/**
	 * Get a set of data from the stored data.
	 * @return Set<?>
	 * @throws xConfigException
	 */
	@Override
	public <C> Set<C> getSet(final Class<? extends C> clss,
			final String path) throws xConfigException {
		try {
			return utilsObject.castSet(
				clss,
				this.get(path)
			);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	/**
	 * Get string set from the stored data.
	 * @return Set<String>
	 * @throws xConfigException
	 */
	@Override
	public Set<String> getStringSet(final String path)
			throws xConfigException {
		try {
			return this.getSet(
				String.class,
				path
			);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}



	/**
	 * Get a list of data from the stored data.
	 * @return List<?>
	 * @throws xConfigException
	 */
	@Override
	public <C> List<C> getList(final Class<? extends C> clss,
			final String path) throws xConfigException {
		try {
			return utilsObject.castList(
				clss,
				this.get(path)
			);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	/**
	 * Get a string list from the stored data.
	 * @return List<String>
	 * @throws xConfigException
	 */
	@Override
	public List<String> getStringList(final String path)
			throws xConfigException {
		try {
			return this.getList(
				String.class,
				path
			);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}



	/**
	 * Get a map of data from the stored data.
	 * @return Map<?, ?>
	 * @throws xConfigException
	 */
	@Override
	public <K, V> Map<K, V> getMap(final Class<? extends K> clssK,
			final Class<? extends V> clssV, final String path)
			throws xConfigException {
		try {
			return utilsObject.castMap(
				clssK,
				clssV,
				this.getPath(path)
			);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	/**
	 * Get a map of data from the stored data.
	 * @return Map<String, Object>
	 * @throws xConfigException
	 */
	@Override
	public Map<String, Object> getStringObjectMap(final String path)
			throws xConfigException {
		try {
			return this.getMap(
				String.class,
				Object.class,
				path
			);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	/**
	 * Get a map of strings from the stored data.
	 * @return Map<String, String>
	 * @throws xConfigException
	 */
	@Override
	public Map<String, String> getStringMap(final String path)
			throws xConfigException {
		try {
			return this.getMap(
				String.class,
				String.class,
				path
			);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}



//	public List<HashMap<String, Object>> getKeyList(String path) {
//		try {
//			@SuppressWarnings("unchecked")
//			List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) get(path);
//			return list;
//		} catch (Exception e) {
//			this.log().trace(e);
//		}
//		return null;
//	}
//	public List<pxnConfig> getConfigList(String path) {
//		List<HashMap<String, Object>> map = getKeyList(path);
//		if(map == null) return null;
//		List<pxnConfig> list = new ArrayList<pxnConfig>();
//		for(HashMap<String, Object> d : map)
//			list.add(new pxnConfig(d));
//		return list;
//	}



	/**
	 * Get a list of config objects from the stored data.
	 * @return List<xConfig>
	 * @throws xConfigException
	 */
	@Override
	public List<xConfig> getConfigList(final String path,
			final Class<? extends xConfig> clss)
			throws xConfigException {
		try {
			final List<Object> datalist = this.getList(
					Object.class,
					path
			);
			if(datalist == null) {
				this.log().fine("Config list not found: "+path);
				return null;
			}
			// get config class constructor
			final Constructor<? extends xConfig> construct;
			try {
				construct = clss.getConstructor(Map.class);
			} catch (NoSuchMethodException e) {
				throw new xConfigException(path, e);
			} catch (SecurityException e) {
				throw new xConfigException(path, e);
			}
			if(construct == null)
				throw new xConfigException("Failed to get <Map> constructor for class: "+clss.getName());
			final List<xConfig> configList = new ArrayList<xConfig>();
			final Iterator<Object> it = datalist.iterator();
			while(it.hasNext()) {
				final Map<String, Object> datamap = utilsObject.castMap(
						String.class,
						Object.class,
						it.next()
				);
				if(datamap == null)
					throw new xConfigException("Failed to load config entry: "+path);
				final xConfig cfg;
				try {
					cfg = (xConfig) construct.newInstance(datamap);
				} catch (InstantiationException e) {
					throw new xConfigException(path, e);
				} catch (IllegalAccessException e) {
					throw new xConfigException(path, e);
				} catch (IllegalArgumentException e) {
					throw new xConfigException(path, e);
				} catch (InvocationTargetException e) {
					throw new xConfigException(path, e);
				}
				configList.add(cfg);
			}
			return configList;
		} catch (Exception e) {
			throw new xConfigException(path, e);
		}
	}



	public static void Dump(final Object obj) {
		Dump(obj, 0, false);
	}
	public void dump() {
		Dump(this.datamap, 0, false);
	}
	private static void Dump(final Object obj, final int indent, boolean noIndent) {
		// null
		if(obj == null) {
			System.out.println(DumpIndent(indent, noIndent)+"[NULL]");
			return;
		}
		// xConfig object
		if(obj instanceof xConfig) {
			((xConfig) obj).dump();
			return;
		}
		// set/list
		if(obj instanceof Collection) {
			@SuppressWarnings("rawtypes")
			final Collection c = (Collection) obj;
			System.out.println();
			System.out.println(DumpIndent(indent, false)+"[SET/LIST]");
			for(final Object o : c) {
				Dump(o, indent+1, false);
			}
			return;
		}
		// map
		if(obj instanceof Map) {
			final Map<String, Object> map = utilsObject.castMap(
					String.class,
					Object.class,
					obj
			);
//			System.out.println();
			System.out.println(DumpIndent(indent, noIndent)+"[MAP]");
			for(final Entry<String, Object> entry : map.entrySet()) {
				System.out.print(DumpIndent(indent+1, false)+entry.getKey()+" = ");
				Dump(entry.getValue(), indent+2, true);
			}
			return;
		}
		// other
		System.out.println(DumpIndent(indent, noIndent)+obj.toString());
	}
//	private void dump(final int indent, final boolean noIndent) {
//		if(indent == 0) {
//			System.out.println();
//			System.out.println("DUMP:");
//		}
//		for(final Entry<String, Object> entry : this.datamap.entrySet()) {
//			final String key = entry.getKey();
//			final Object val = entry.getValue();
//			System.out.print(DumpIndent(indent+1, false)+key+" = ");
//			Dump(val, indent+2, false);
//		}
//		if(indent == 0) {
//			System.out.println();
//		}
//	}
	private static String DumpIndent(final int indent, final boolean noIndent) {
		if(noIndent)
			return "";
		return utilsString.repeat(indent, "  ");
	}



}
