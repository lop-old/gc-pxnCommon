package com.poixson.commonjava.logger;

import org.junit.Assert;

import com.poixson.commonjava.Failure;
import com.poixson.commonjava.xLogger.xLevel;
import com.poixson.commonjava.xLogger.xLog;


public class xLogTest {

	private static volatile xLog log = null;
	private static final Object instanceLock = new Object();



	public static xLog get() {
		if(log == null) {
			synchronized(instanceLock) {
				if(log == null) {
					Assert.assertFalse(Failure.hasFailed());
					log = xLog.getRoot();
					xLog.init();
					xLog.getRoot().setLevel(xLevel.ALL);
					log.publish();
					log.title("Testing pxnCommon");
				}
			}
		}
		Assert.assertFalse(Failure.hasFailed());
		return log;
	}
	public static xLog get(final String name) {
		return get().get(name);
	}



}
