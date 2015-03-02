package com.poixson.commonjava.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public final class utilsObject {
	private utilsObject() {}



	@SuppressWarnings("unchecked")
	public static <T> T cast(final Class<? extends T> clss, final Object object) {
		if(clss == null) throw new NullPointerException();
		if(object == null) return null;
		try {
			if( String.class.equals(clss) && !(object instanceof String) )
				return (T) object.toString();
			return clss.cast(object);
		} catch (Exception ignore) {}
		return null;
	}



	/**
	 * Cast a collection object to a list.
	 * @param clss
	 * @param c
	 * @return
	 */
	public static <T> List<T> castList(final Class<? extends T> clss, final Collection<?> c) {
		if(clss == null) throw new NullPointerException("clss cannot be null");
		if(c    == null) return null;
		try {
			final List<T> result = new ArrayList<T>(c.size());
			for(final Object o : c) {
				try {
					result.add(clss.cast(o));
				} catch (Exception ignore) {}
			}
			return result;
		} catch (Exception ignore) {}
		return null;
	}
	/**
	 * Cast an object to a list.
	 * @param clss
	 * @param c
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> castList(final Class<? extends T> clss, final Object object) {
		if(clss   == null) throw new NullPointerException("clss cannot be null");
		if(object == null) return null;
		try {
			return castList(clss, (Collection<T>) object);
		} catch (Exception ignore) {}
		return null;
	}



	/**
	 * Cast a collection object to a set.
	 * @param clss
	 * @param c
	 * @return
	 */
	public static <T> Set<T> castSet(final Class<? extends T> clss, final Collection<?> c) {
		if(clss == null) throw new NullPointerException("clss cannot be null");
		if(c    == null) return null;
		try {
			final Set<T> result = new HashSet<T>(c.size());
			for(final Object o : c) {
				try {
					result.add(clss.cast(o));
				} catch (Exception ignore) {}
			}
			return result;
		} catch (Exception ignore) {}
		return null;
	}
	/**
	 * Cast an object to a set.
	 * @param clss
	 * @param c
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Set<T> castSet(final Class<? extends T> clss, final Object object) {
		if(clss   == null) throw new NullPointerException("clss cannot be null");
		if(object == null) return null;
		try {
			return castSet(clss, (Collection<T>) object);
		} catch (Exception ignore) {}
		return null;
	}



	/**
	 * Cast a map object to a typed map.
	 * @param keyClss
	 * @param valClss
	 * @param m
	 * @return
	 */
	public static <K, V> Map<K, V> castMap(final Class<? extends K> keyClss,
			final Class<? extends V> valClss, final Map<?, ?> m) {
		if(keyClss == null) throw new NullPointerException("key clss cannot be null");
		if(valClss == null) throw new NullPointerException("val clss cannot be null");
		if(m       == null) return null;
		try {
			final Map<K, V> result = new HashMap<K, V>(m.size());
			for(final Entry<?, ?> entry : m.entrySet()) {
				try {
					result.put(
						cast(keyClss, entry.getKey()),
						cast(valClss, entry.getValue())
					);
				} catch (Exception ignore) {}
			}
			return result;
		} catch (Exception ignore) {}
		return null;
	}
	/**
	 * Cast an object to a typed map.
	 * @param keyClss
	 * @param valClss
	 * @param m
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> castMap(final Class<? extends K> keyClss,
			final Class<? extends V> valClss, final Object object) {
		if(keyClss == null) throw new NullPointerException("key clss cannot be null");
		if(valClss == null) throw new NullPointerException("val clss cannot be null");
		if(object == null) return null;
		try {
			return castMap(keyClss, valClss, (Map<K, V>) object);
		} catch (Exception ignore) {}
		return null;
	}



}
