package com.poixson.commonjava.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public final class utilsObject {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	private utilsObject() {}


	/**
	 * Cast a collection to list.
	 * @param clss
	 * @param c
	 * @return
	 */
	public static <T> List<T> castList(final Class<? extends T> clss, final Collection<?> c) {
		if(clss == null) throw new NullPointerException("clss cannot be null");
		if(c    == null) throw new NullPointerException("c cannot be null");
		final List<T> result = new ArrayList<T>(c.size());
		for(final Object o : c)
			result.add(clss.cast(o));
		return result;
	}
	@SuppressWarnings("unchecked")
	public static <T> List<T> castList(final Class<? extends T> clss, final Object object) {
		if(clss   == null) throw new NullPointerException("clss cannot be null");
		if(object == null) throw new NullPointerException("object cannot be null");
		try {
			return castList(clss, (Collection<T>) object);
		} catch(Exception ignore) {}
		return null;
	}


}
