package com.poixson.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.poixson.exceptions.RequiredArgumentException;


public final class ObjectUtils {
	private ObjectUtils() {}



	@SuppressWarnings("unchecked")
	public static <T> T Cast(final Object object,
			final Class<? extends T> clss) {
		if (clss   == null) throw new RequiredArgumentException("clss");
		if (object == null) return null;
		try {
			if ( String.class.equals(clss) && !(object instanceof String) )
				return (T) object.toString();
			return clss.cast(object);
		} catch (Exception ignore) {}
		return null;
	}



	/**
	 * Cast a collection object to a set.
	 * @param data
	 * @param clss
	 * @return
	 */
	public static <T> Set<T> CastSet(final Collection<?> data,
			final Class<? extends T> clss) {
		if (clss == null) throw new RequiredArgumentException("clss");
		if (data == null) return null;
		try {
			final Set<T> result = new HashSet<T>(data.size());
			for (final Object o : data) {
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
	 * @param data
	 * @param clss
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Set<T> CastSet(final Object data,
			final Class<? extends T> clss) {
		if (clss == null) throw new RequiredArgumentException("clss");
		if (data == null) return null;
		try {
			return CastSet(
					(Collection<T>) data,
					clss
			);
		} catch (Exception ignore) {}
		return null;
	}



	/**
	 * Cast a collection object to a list.
	 * @param data
	 * @param clss
	 * @return
	 */
	public static <T> List<T> CastList(final Collection<?> data,
			final Class<? extends T> clss) {
		if (clss == null) throw new RequiredArgumentException("clss");
		if (data == null) return null;
		try {
			final List<T> result = new ArrayList<T>(data.size());
			for (final Object o : data) {
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
	public static <T> List<T> CastList(final Object data,
			final Class<? extends T> clss) {
		if (clss == null) throw new RequiredArgumentException("clss");
		if (data == null) return null;
		try {
			return CastList(
					(Collection<T>) data,
					clss
			);
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
	public static <K, V> Map<K, V> CastMap(final Map<?, ?> data,
			final Class<? extends K> keyClss, final Class<? extends V> valClss) {
		if (keyClss == null) throw new RequiredArgumentException("keyClss");
		if (valClss == null) throw new RequiredArgumentException("valClss");
		if (data    == null) return null;
		try {
			final Map<K, V> result = new HashMap<K, V>(data.size());
			for (final Entry<?, ?> entry : data.entrySet()) {
				try {
					result.put(
						Cast(
							entry.getKey(),
							keyClss
						),
						Cast(
							entry.getValue(),
							valClss
						)
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
	public static <K, V> Map<K, V> CastMap(final Object data,
			final Class<? extends K> keyClss, final Class<? extends V> valClss) {
		if (keyClss == null) throw new RequiredArgumentException("keyClss");
		if (valClss == null) throw new RequiredArgumentException("valClss");
		if (data    == null) return null;
		try {
			return CastMap(
					(Map<K, V>) data,
					keyClss,
					valClss
			);
		} catch (Exception ignore) {}
		return null;
	}



}
