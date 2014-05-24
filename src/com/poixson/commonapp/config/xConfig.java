package com.poixson.commonapp.config;

import java.util.List;
import java.util.Map;

import com.poixson.commonjava.Utils.utilsObject;


public class xConfig {

	protected final Map<String, Object> data;


	public xConfig(final Map<String, Object> data) {
		if(data == null) throw new NullPointerException();
		this.data = data;
	}
//	@Override
//	public Object clone() throws CloneNotSupportedException {
//		throw new CloneNotSupportedException();
//	}


	// path exists
	public boolean exists(final String path) {
		return this.data.containsKey(path);
	}
	// get object
	public Object get(final String path) {
		if(path == null || path.isEmpty())
			return null;
		try {
			return this.data.get(path);
		} catch (Exception ignore) {}
		return null;
	}
	// get path
	@SuppressWarnings("unchecked")
	public Map<String, Object> getPath(final String path) {
		try {
			return (Map<String, Object>) get(path);
		} catch (Exception ignore) {}
		return null;
	}


	// get string
	public String getString(final String path) {
		try {
			return (String) get(path);
		} catch (Exception ignore) {}
		return null;
	}
	// get boolean
	public Boolean getBoolean(final String path) {
		try {
			return (Boolean) get(path);
		} catch (Exception ignore) {}
		return null;
	}
	// get integer
	public Integer getInteger(final String path) {
		try {
			return (Integer) get(path);
		} catch (Exception ignore) {}
		return null;
	}
	// get long
	public Long getLong(final String path) {
		try {
			return (Long) get(path);
		} catch (Exception ignore) {}
		return null;
	}
	// get double
	public Double getDouble(final String path) {
		try {
			return (Double) get(path);
		} catch (Exception ignore) {}
		return null;
	}
	// get float
	public Float getFloat(final String path) {
		try {
			return (Float) get(path);
		} catch (Exception ignore) {}
		return null;
	}


	// get list
	public <T> List<T> getList(final Class<? extends T> clss, final String path) {
		try {
			return utilsObject.castList(clss, get(path));
		} catch (Exception ignore) {}
		return null;
	}
	// get string list
	public List<String> getStringList(final String path) {
		try {
			return getList(String.class, path);
		} catch (Exception ignore) {}
		return null;
	}


//	public List<HashMap<String, Object>> getKeyList(String path) {
//		try {
//			@SuppressWarnings("unchecked")
//			List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) get(path);
//			return list;
//		} catch (Exception ignore) {}
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


}
