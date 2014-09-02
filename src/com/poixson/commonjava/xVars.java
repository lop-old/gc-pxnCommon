package com.poixson.commonjava;

import com.poixson.commonjava.pxdb.dbManager;

public class xVars {


	// singleton instance
	private static volatile xVars instance = null;
	protected static final Object instanceLock = new Object();
	// just to prevent gc
	@SuppressWarnings("unused")
	private final dbManager dbman;



	public static xVars get() {
		if(instance == null) {
			synchronized(instanceLock) {
				if(instance == null)
					instance = new xVars();
			}
		}
		return instance;
	}
	// new instance of holder
	protected xVars() {
		// clone vars
		if(instance != null) {
			this.debug = instance.debug;
		// new instance
		} else {
		}
		// just to prevent gc
		this.dbman = dbManager.get();
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}



	// debug mode
	private static final boolean DEFAULT_DEBUG = false;
	private volatile Boolean debug = null;
	public boolean debug() {
		if(this.debug == null)
			return DEFAULT_DEBUG;
		return this.debug.booleanValue();
	}
	public void debug(final boolean value) {
		this.debug = new Boolean(value);
	}



}
