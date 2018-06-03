package org.slf4j.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import com.poixson.logger.xLevel;
import com.poixson.logger.xLog;
import com.poixson.logger.xLogRoot;
import com.poixson.utils.Utils;


public class slf4jLoggerFactory implements ILoggerFactory {
	public static final String LOG_NAME = "slf4j";

	private final ConcurrentMap<String, Logger> loggers =
			new ConcurrentHashMap<String, Logger>();



	@Override
	public Logger getLogger(final String name) {
		// use existing logger instance
		{
			final Logger logger = this.loggers.get(name);
			if (logger != null)
				return logger;
		}
		// new logger instance
		{
			// wrap the logger
			final Logger newlogger =
				new slf4jLoggerAdapter(
					name,
					getLog(name)
				);
			// cache wrapped logger
			final Logger existing =
				this.loggers.putIfAbsent(
					name,
					newlogger
				);
			return (
				existing == null
				? newlogger
				: existing
			);
		}
	}



	public static xLog getLog(final String name) {
		final xLog log;
		if (Utils.isEmpty(name)) {
			log = xLogRoot.get()
					.get(LOG_NAME);
		} else
		if (name.startsWith("org.xeustechnologies.jcl.")) {
			log = xLogRoot.get()
					.get("jcl");
		} else {
			log = xLogRoot.get()
					.get(LOG_NAME).get(name);
		}
		// disable logging if not detail mode
		if ( ! xLogRoot.get().isDetailLoggable() ) {
			log.setLevel(xLevel.WARNING);
		}
		return log;
	}



}
