package com.poixson.commonjava;


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


}
