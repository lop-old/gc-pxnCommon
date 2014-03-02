package com.poixson.commonjava.Utils;

import java.io.Closeable;
import java.io.IOException;

import com.poixson.commonjava.xVars;
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


	// logger
	public static xLog log() {
		return xVars.log();
	}


}
