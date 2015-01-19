package com.poixson.commonjava.xLogger;

import com.poixson.commonjava.Failure;


public class xLogTest {

	private static volatile xLog log = null;
	private static final Object instanceLock = new Object();



	public static xLog get() {
		if(log == null) {
			synchronized(instanceLock) {
				if(log == null) {
					if(Failure.hasFailed()) return null;
					log = xLog.getRoot();
					xLog.init();
					if(Failure.hasFailed()) return null;
					xLog.getRoot().setLevel(xLevel.ALL);
					log.publish();
					log.title("Testing pxnCommon");
				}
			}
		}
		if(Failure.hasFailed()) return null;
		return log;
	}
	public static xLog get(final String name) {
		return get().get(name);
	}



}
