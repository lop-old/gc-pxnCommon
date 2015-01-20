package com.poixson.commonjava;

import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.xLogger.xLog;


public class xVars {



	// single instance
	protected static volatile xVars instance = null;
	protected static final Object instanceLock = new Object();

	// get instance
	public static xVars get() {
		if(instance == null) {
			synchronized(instanceLock) {
				if(instance == null)
					instance = new xVars();
			}
		}
		return instance;
	}
	// new instance
	protected xVars() {
		Keeper.add(this);
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
		if(this.debug != null && this.debug.booleanValue() == value) return;
		if(!value) xLog.getRoot().fine("Disabled debug mode");
		this.debug = Boolean.valueOf(value);
		// if(value) xLog.getRoot().fine("Enabled debug mode");
	}



}
