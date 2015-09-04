package com.poixson.commonjava.Utils;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


public class Keeper {

	private static volatile Keeper instance = null;
	private static final Object instanceLock = new Object();

	private static final Set<Object> holder = new CopyOnWriteArraySet<Object>();



	public static Keeper get() {
		if(instance == null) {
			synchronized(instanceLock) {
				if(instance == null)
					instance = new Keeper();
			}
		}
		return instance;
	}



	public static void add(final Object obj) {
		if(obj == null) throw new NullPointerException("obj argument is required!");
		holder.add(obj);
	}
	public static void remove(final Object obj) {
		if(obj == null) throw new NullPointerException("obj argument is required!");
		holder.remove(obj);
	public static int removeAll(final Class<? extends Object> clss) {
		if(holder.isEmpty())
			return 0;
		int count = 0;
		final String expect = clss.getName();
		final Iterator<Object> it = holder.iterator();
		while(it.hasNext()) {
			final Object obj = it.next();
			final String actual = obj.getClass().getName();
			if(expect.equals(actual)) {
				count++;
				remove(obj);
			}
		}
		return count;
	}
	}



}
