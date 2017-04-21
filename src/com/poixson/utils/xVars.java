package com.poixson.utils;

import java.io.InputStream;
import java.io.PrintStream;

import com.poixson.utils.xLogger.xLog;


public class xVars {

	private static final boolean DEFAULT_DEBUG = false;



	private static volatile boolean inited = false;
	public static void init() {
		if (!inited) {
			Keeper.add(new xVars());
		}
	}
	private xVars() {
	}



	// debug mode
	private static volatile Boolean debug = null;
	private static final Object debugLock = new Object();

	public static boolean debug() {
		final Boolean bool = debug;
		if (bool == null) {
			return DEFAULT_DEBUG;
		}
		return bool.booleanValue();
	}
	public static void debug(final boolean value) {
		synchronized(debugLock) {
			// check existing value
			final Boolean bool = debug;
			if (bool != null) {
				if (bool.booleanValue() == value) {
					return;
				}
			}
			// change debug state
			if (!value) {
				xLog.getRoot().fine("Disabled debug mode");
			}
			debug = Boolean.valueOf(value);
			if (value) {
				xLog.getRoot().fine("Enabled debug mode");
			}
		}
	}



}
