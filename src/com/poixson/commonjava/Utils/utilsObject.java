package com.poixson.commonjava.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


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
	 * Cast a collection to list.
	 * @param clss
	 * @param c
	 * @return
	 */
	public static <T> List<T> castList(final Class<? extends T> clss, final Collection<?> c) {
		if(clss == null) throw new NullPointerException("clss cannot be null");
		if(c    == null) return null;
		final List<T> result = new ArrayList<T>(c.size());
		try {
			for(final Object o : c)
				result.add(clss.cast(o));
		} catch (Exception ignore) {
			return null;
		}
		return result;
	}
	@SuppressWarnings("unchecked")
	public static <T> List<T> castList(final Class<? extends T> clss, final Object object) {
		if(clss   == null) throw new NullPointerException("clss cannot be null");
		if(object == null) return null;
		try {
			return castList(clss, (Collection<T>) object);
		} catch (Exception ignore) {}
		return null;
	}



}
