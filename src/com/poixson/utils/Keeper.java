package com.poixson.utils;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.Utils.exceptions.RequiredArgumentException;
import com.poixson.commonjava.xLogger.xLog;


public class Keeper {
	private static final String LOG_NAME = "KEEPER";
	private static final boolean DEBUG_EXTRA = false;

	private static volatile Keeper instance = null;
	private static final Object instanceLock = new Object();

	private static final Set<Object> holder = new CopyOnWriteArraySet<Object>();



	public static Keeper get() {
		if(instance == null) {
			synchronized(instanceLock) {
				if(instance == null)
					instance = new Keeper();
			}
			utils.init();
		}
		return instance;
	}



	public static void add(final Object obj) {
		if(obj == null) throw new RequiredArgumentException("obj");
		holder.add(obj);
		if(DEBUG_EXTRA())
			finest("Added: "+obj.getClass().getName());
	}
	public static void remove(final Object obj) {
		if(obj == null) throw new RequiredArgumentException("obj");
		holder.remove(obj);
		if(DEBUG_EXTRA())
			finest("Removed: "+obj.getClass().getName());
	}
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



	// logger
	private static volatile xLog _log = null;
	private static xLog log() {
		if(!DEBUG_EXTRA()) return null;
		if(_log == null)
			_log = xLog.getRoot(LOG_NAME);
		return _log;
	}
	private static void finest(final String msg) {
		(new Thread() {
			private volatile String msg = null;
			public Thread finest(final String msg) {
				this.msg = msg;
				return this;
			}
			@Override
			public void run() {
				log().finest(this.msg);
			}
		}).finest(msg)
		.start();
	}
	private static boolean DEBUG_EXTRA() {
		if(DEBUG_EXTRA)
			return xVars.debug();
		return false;
	}



}
