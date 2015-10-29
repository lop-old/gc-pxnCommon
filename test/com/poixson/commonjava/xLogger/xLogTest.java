package com.poixson.commonjava.xLogger;

import org.junit.Assert;

import com.poixson.commonjava.Failure;
import com.poixson.commonjava.xVars;


public class xLogTest {

	private static volatile xLog log = null;
	private static final Object instanceLock = new Object();



	public static xLog get() {
		if(log == null) {
			synchronized(instanceLock) {
				if(log == null) {
					assertHasntFailed();
					xVars.debug(true);
					log = xLog.getRoot();
					assertHasntFailed();
					log.setLevel(xLevel.ALL);
				}
			}
		}
		assertHasntFailed();
		return log;
	}
	public static xLog get(final String name) {
		return get().get(name);
	}



	public static void testStart(final String name) {
		get().publish();
		get().title(
				(new StringBuilder())
				.append(name)
				.append(" is testing..")
				.toString()
		);
	}
	public static void testPassed(final String name) {
		get().title(
				(new StringBuilder())
				.append(name)
				.append(" passed!")
				.toString()
		);
	}
	public static void title(final String msg) {
		get().title(msg);
	}
	public static void publish(final String msg) {
		get().publish(
				(new StringBuilder())
				.append("*** ")
				.append(msg)
				.toString()
		);
	}
	public static void trace(final Throwable e) {
		get().trace(e);
	}



	public static void assertHasntFailed() {
		Assert.assertFalse(
			"Failed!",
			Failure.hasFailed()
		);
	}



}
