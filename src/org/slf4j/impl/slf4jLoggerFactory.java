package org.slf4j.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import com.poixson.utils.xLogger.xLog;


public class slf4jLoggerFactory implements ILoggerFactory {

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
//			final org.apache.commons.logging.Log apacheLogger =
//					LogFactory.getLog(name);
//			final Logger newLogger = new slf4jLoggerAdapter(apacheLogger, name);
			final xLog log = xLog.getRoot().get("slf4j-wrapper");
			final Logger newlogger = new slf4jLoggerAdapter(name, log);
			final Logger existing = this.loggers.putIfAbsent(name, newlogger);
			return (
				existing == null
				? newlogger
				: existing
			);
		}
	}



}
