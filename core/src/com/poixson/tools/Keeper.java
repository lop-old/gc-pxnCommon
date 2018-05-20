package com.poixson.tools;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import com.poixson.exceptions.RequiredArgumentException;


public class Keeper {

	private static final AtomicReference<Keeper> instance =
			new AtomicReference<Keeper>(null);

	private static final CopyOnWriteArraySet<Object> holder =
			new CopyOnWriteArraySet<Object>();



	public static Keeper get() {
		if (instance.get() != null)
			return instance.get();
		// new instance
		final Keeper keeper = new Keeper();
		if (!instance.compareAndSet(null, keeper))
			return instance.get();
		return keeper;
	}



	public static void add(final Object obj) {
		if (obj == null) throw new RequiredArgumentException("obj");
		holder.add(obj);
	}



	public static void remove(final Object obj) {
		if (obj == null) throw new RequiredArgumentException("obj");
		holder.remove(obj);
	}
	public static void removeAll() {
		holder.clear();
	}
	public static int removeAll(final Class<? extends Object> clss) {
		if (holder.isEmpty())
			return 0;
		int count = 0;
		final String expect = clss.getName();
		final Iterator<Object> it = holder.iterator();
		while (it.hasNext()) {
			final Object obj = it.next();
			final String actual = obj.getClass().getName();
			if (expect.equals(actual)) {
				count++;
				remove(obj);
			}
		}
		return count;
	}



}
