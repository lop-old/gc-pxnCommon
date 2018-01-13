package org.slf4j.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import com.poixson.logger.xLevel;
import com.poixson.logger.xLog;


public class slf4jLoggerFactory implements ILoggerFactory {
	public static final String LOG_NAME = "slf4j-wrapper";

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
					getLog()
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



	public static xLog getLog() {
		final xLog log =
			xLog.getRoot()
				.get(LOG_NAME);
		// disable logging if not detail mode
		if (!xLog.getRoot().isLoggable(xLevel.DETAIL)) {
			log.setLevel(xLevel.WARNING);
		}
		return log;
	}



}
