package com.poixson.commonjava.Utils;

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
		if(obj == null) throw new NullPointerException();
		holder.add(obj);
	}
	public static void remove(final Object obj) {
		if(obj == null) throw new NullPointerException();
		holder.remove(obj);
	}



}
