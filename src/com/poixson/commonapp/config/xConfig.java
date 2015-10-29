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
	public boolean exists(final String path) {
		return this.datamap.containsKey(path);
	}
	// get object
	public Object get(final String path) {
		if(utils.isEmpty(path))
			return null;
		try {
			return this.datamap.get(path);
		} catch (Exception e) {
			this.log().trace(e);
		}
		return null;
	}
	// get path
	@SuppressWarnings("unchecked")
	public Map<String, Object> getPath(final String path) {
		try {
			if(path.contains(".")) {
				Map<String, Object> map = this.datamap;
				final StringRef str = new StringRef(path);
				while(str.notEmpty()) {
					final String part = utilsString.getNextPart(".", str);
					try {
						map = (Map<String, Object>) map.get(part);
					} catch (Exception e) {
						this.log().trace(e);
						return null;
					}
				}
				return map;
			}
			return (Map<String, Object>) this.get(path);
		} catch (Exception e) {
			this.log().trace(e);
		}
		return null;
	}



	public boolean isFromResource() {
		return this.loadedFromResource;
	}



	// get string
	public String getString(final String path) {
		try {
			return (String) this.get(path);
		} catch (Exception e) {
			this.log().trace(e);
		}
		return null;
	}
	public String getStr(final String path, final String def) {
		final String value = this.getString(path);
		if(utils.isEmpty(value))
			return def;
		return value;
	}
	// get boolean
	public Boolean getBoolean(final String path) {
		try {
			return (Boolean) this.get(path);
		} catch (Exception e) {
			this.log().trace(e);
		}
		return null;
	}
	public boolean getBool(final String path, final boolean def) {
		final Boolean value = this.getBoolean(path);
		if(value == null)
			return def;
		return value.booleanValue();
	}
	// get integer
	public Integer getInteger(final String path) {
		try {
			return (Integer) this.get(path);
		} catch (Exception e) {
			this.log().trace(e);
		}
		return null;
	}
	public int getInt(final String path, final int def) {
		final Integer value = this.getInteger(path);
		if(value == null)
			return def;
		return value.intValue();
	}
	// get long
	public Long getLong(final String path) {
		try {
			return (Long) this.get(path);
		} catch (Exception e) {
			this.log().trace(e);
		}
		return null;
	}
	public long getLng(final String path, final long def) {
		final Long value = this.getLong(path);
		if(value == null)
			return def;
		return value.longValue();
	}
	// get double
	public Double getDouble(final String path) {
		try {
			return (Double) this.get(path);
		} catch (Exception e) {
			this.log().trace(e);
		}
		return null;
	}
	public double getDbl(final String path, final double def) {
		final Double value = this.getDouble(path);
		if(value == null)
			return def;
		return value.doubleValue();
	}
	// get float
	public Float getFloat(final String path) {
		try {
			return (Float) this.get(path);
		} catch (Exception e) {
			this.log().trace(e);
		}
		return null;
	}
	public float getFlt(final String path, final float def) {
		final Float value = this.getFloat(path);
		if(value == null)
			return def;
		return value.floatValue();
	}



	// get list
	public <T> List<T> getList(final Class<? extends T> clss, final String path) {
		try {
			return utilsObject.castList(
				clss,
				this.get(path)
			);
		} catch (Exception e) {
			this.log().trace(e);
		}
		return null;
	}
	// get string list
	public List<String> getStringList(final String path) {
		try {
			return this.getList(
				String.class,
				path
			);
		} catch (Exception e) {
			this.log().trace(e);
		}
		return null;
	}



	// get set
	public <T> Set<T> getSet(final Class<? extends T> clss, final String path) {
		try {
			return utilsObject.castSet(
				clss,
				this.get(path)
			);
		} catch (Exception e) {
			this.log().trace(e);
		}
		return null;
	}
	// get string list
	public Set<String> getStringSet(final String path) {
		try {
			return this.getSet(
				String.class,
				path
			);
		} catch (Exception e) {
			this.log().trace(e);
		}
		return null;
	}



	// get map
	public <K, V> Map<K, V> getMap(final Class<? extends K> clssK,
			final Class<? extends V> clssV, final String path) {
		try {
			return utilsObject.castMap(
				clssK,
				clssV,
				this.getPath(path)
			);
		} catch (Exception e) {
			this.log().trace(e);
		}
		return null;
	}
	// get string:object map
	public Map<String, Object> getStringObjectMap(final String path) {
		try {
			return this.getMap(
				String.class,
				Object.class,
				path
			);
		} catch (Exception e) {
			this.log().trace(e);
		}
		return null;
	}
	// get string:string map
	public Map<String, String> getStringMap(final String path) {
		try {
			return this.getMap(
				String.class,
				String.class,
				path
			);
		} catch (Exception e) {
			this.log().trace(e);
		}
		return null;
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
