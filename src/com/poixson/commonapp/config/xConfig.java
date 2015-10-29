package com.poixson.commonapp.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsObject;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.byRef.StringRef;
import com.poixson.commonjava.xLogger.xLog;


public class xConfig extends xConfigLoader implements xConfigInterface {

	protected final Map<String, Object> datamap;

	protected volatile boolean loadedFromResource = false;



	public xConfig(final Map<String, Object> datamap) {
		if(datamap == null) throw new NullPointerException("datamap argument is required!");
		this.datamap = datamap;
	}
	@Override
	public Object clone() {
		return new xConfig(this.datamap);
	}



	// path exists
	@Override
	public boolean exists(final String path) {
		return this.datamap.containsKey(path);
	}
	// get object
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
	public Object gt(final String path) {
		try {
			return this.get(path);
		} catch (xConfigException ignore) {}
		return null;
	}
	// get path
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
	@Override
	public Map<String, Object> getPath(final String path) {
		try {
			return this.getDataPath(path);
		} catch (xConfigException ignoire) {}
		return null;
	}



	public boolean isFromResource() {
		return this.loadedFromResource;
	}



	// get string
	@Override
	public String getString(final String path) throws xConfigException {
		try {
			return (String) this.get(path);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	@Override
	public String getStr(final String path, final String def) {
		try {
			final String value = this.getString(path);
			if(utils.notEmpty(value))
				return value;
		} catch (xConfigException ignore) {}
		return def;
	}
	// get boolean



	@Override
	public Boolean getBoolean(final String path) throws xConfigException {
		try {
			return (Boolean) this.get(path);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	@Override
	public boolean getBool(final String path, final boolean def) {
		try {
			final Boolean value = this.getBoolean(path);
			if(value != null)
				return value.booleanValue();
		} catch (xConfigException ignore) {}
		return def;
	}
	// get integer



	@Override
	public Integer getInteger(final String path) throws xConfigException {
		try {
			return (Integer) this.get(path);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	@Override
	public int getInt(final String path, final int def) {
		try {
			final Integer value = this.getInteger(path);
			if(value != null)
				return value.intValue();
		} catch (xConfigException ignore) {}
		return def;
	}
	// get long



	@Override
	public Long getLong(final String path) throws xConfigException {
		try {
			return (Long) this.get(path);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	@Override
	public long getLng(final String path, final long def) {
		try {
			final Long value = this.getLong(path);
			if(value != null)
				return value.longValue();
		} catch (xConfigException ignore) {}
		return def;
	}
	// get double



	@Override
	public Double getDouble(final String path) throws xConfigException {
		try {
			return (Double) this.get(path);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	@Override
	public double getDbl(final String path, final double def) {
		try {
			final Double value = this.getDouble(path);
			if(value != null)
				return value.doubleValue();
		} catch (xConfigException ignore) {}
		return def;
	}
	// get float



	@Override
	public Float getFloat(final String path) throws xConfigException {
		try {
			return (Float) this.get(path);
		} catch (Exception e) {
			throw new xConfigException(e, path);
		}
	}
	@Override
	public float getFlt(final String path, final float def) {
		try {
			final Float value = this.getFloat(path);
			if(value != null)
				return value.floatValue();
		} catch (xConfigException ignore) {}
		return def;
	}



	// get list
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
	// get string list
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



	// get set
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
	// get string list
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



	// get map
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
	// get string:object map
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
	// get string:string map
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



	// logger
	private volatile xLog _log = null;
	private xLog _log_default  = null;
	public xLog log() {
		final xLog log = this._log;
		if(log != null)
			return log;
		if(this._log_default == null)
			this._log_default = xLog.getRoot(LOG_NAME);
		return this._log_default;
	}
	public void setLog(final xLog log) {
		this._log = log;
	}



}
