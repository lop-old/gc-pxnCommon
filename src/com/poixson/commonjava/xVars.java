package com.poixson.commonjava;

import com.poixson.commonjava.xLogger.xLog;


public final class xVars {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	private xVars() {}


	// debug mode
	private static volatile Boolean globalDebug = null;
	public static void debug(boolean debug) {
		globalDebug = debug;
	}
	public static boolean debug() {
		if(globalDebug == null)
			return false;
		return globalDebug.booleanValue();
	}


	// global logger
	private static volatile xLog _log = null;
	private static final Object logLock = new Object();
	public static xLog getLog() {
		if(_log == null) {
			synchronized(logLock) {
				if(_log == null)
					_log = xLog.getRoot();
			}
		}
		return _log;
	}
	public static xLog getLog(String name) {
		return getLog().get(name);
	}
	public static void setLog(xLog log) {
		synchronized(logLock) {
			_log = log;
		}
	}


}
