package com.poixson.commonjava.Utils;

import java.io.Closeable;
import java.io.IOException;

import com.poixson.commonjava.xLogger.xLog;


public class utils {


	/**
	 * Is string empty.
	 * @param value
	 * @return True if string is null or empty.
	 */
	public static boolean isEmpty(final String value) {
		return (value == null || value.length() == 0);
	}
	/**
	 * Is string populated.
	 * @param value
	 * @return True if string is not null and contains data.
	 */
	public static boolean notEmpty(final String value) {
		return (value != null && value.length() > 0);
	}



	/**
	 * Is array empty
	 * @param array
	 * @return True if array is null or empty.
	 */
	public static boolean isEmpty(final Object[] array) {
		return (array == null || array.length == 0);
	}
	/**
	 * Is array populated.
	 * @param array
	 * @return True if array is not null and contains data.
	 */
	public static boolean notEmpty(final Object[] array) {
		return (array != null && array.length > 0);
	}



	/**
	 * Close safely, ignoring errors.
	 */
	public static void safeClose(Closeable obj) {
		if(obj == null) return;
		try {
			obj.close();
		} catch (IOException ignore) {}
	}



	/**
	 * Current system time ms.
	 * @return
	 */
	public static long getSystemMillis() {
		return System.currentTimeMillis();
	}



	public static void MemoryStats() {
		final int MB = 1024 * 1024;
		final Runtime runtime = Runtime.getRuntime();
		System.out.println("##### Heap utilization statistics [MB] #####");
		System.out.println( "Used Memory:"  + ((runtime.totalMemory() - runtime.freeMemory()) / MB) );
		System.out.println( "Free Memory:"  + (runtime.freeMemory() / MB) );
		System.out.println( "Total Memory:" + (runtime.totalMemory() / MB) );
		System.out.println( "Max Memory:"   + (runtime.maxMemory() / MB) );
	}



	// logger
	public static xLog log() {
		return xLog.getRoot();
	}



}
