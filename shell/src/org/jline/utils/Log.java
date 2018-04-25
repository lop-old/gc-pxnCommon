package org.jline.utils;

import java.util.function.Supplier;

import com.poixson.logger.xLogRoot;


public final class Log {

	public static final boolean enableLogging = false;



	public static void trace(final Object... messages) {
		if (enableLogging) {
			xLogRoot.get()
				.trace(null, "", messages);
		}
	}
	public static void trace(Supplier<String> supplier) {
		if (enableLogging) {
			xLogRoot.get()
				.trace(null, supplier.get());
		}
	}



	public static void debug(final Object... messages) {
		if (enableLogging) {
			xLogRoot.get()
				.fine("", messages);
		}
	}
	public static void debug(Supplier<String> supplier) {
		if (enableLogging) {
			xLogRoot.get()
				.fine(supplier.get());
		}
	}



	public static void info(final Object... messages) {
		if (enableLogging) {
			xLogRoot.get()
				.info("", messages);
		}
	}



	public static void warn(final Object... messages) {
		if (enableLogging) {
			xLogRoot.get()
				.warning("", messages);
		}
	}



	public static void error(final Object... messages) {
		if (enableLogging) {
			xLogRoot.get()
				.severe("", messages);
		}
	}



	public static boolean isDebugEnabled() {
		if (!enableLogging)
			return false;
		return xLogRoot.get().isDetailLoggable();
	}



}
