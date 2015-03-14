package com.poixson.commonjava;

import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.xLogger.xLog;


public class xVars {



	private static volatile boolean inited = false;
	public static void init() {
		if(!inited)
			Keeper.add(new xVars());
	}
	private xVars() {
	}



	// debug mode
	private static final boolean DEFAULT_DEBUG = false;
	private static volatile Boolean debug = null;

	public static boolean debug() {
		if(debug == null)
			return DEFAULT_DEBUG;
		return debug.booleanValue();
	}
	public static void debug(final boolean value) {
		if(debug != null && debug.booleanValue() == value) return;
		if(!value) xLog.getRoot().fine("Disabled debug mode");
		debug = Boolean.valueOf(value);
		// if(value) xLog.getRoot().fine("Enabled debug mode");
	}



}
