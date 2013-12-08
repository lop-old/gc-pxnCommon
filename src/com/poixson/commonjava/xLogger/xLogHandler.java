package com.poixson.commonjava.xLogger;

import com.poixson.commonjava.xLogger.formatters.defaultLogFormatter;


public abstract class xLogHandler {

	private volatile xLogFormatter formatter = null;
	private volatile xLevel level = null;
	private final Object formatLock = new Object();


	public abstract void publish(xLogRecord record);
	protected String msgFormat(xLogRecord record) {
		return getFormatter().formatMsg(record);
	}


	// formatter
	public void setFormatter(xLogFormatter formatter) {
		synchronized(formatLock) {
			this.formatter = formatter;
		}
	}
	protected xLogFormatter getFormatter() {
		if(formatter != null)
			return formatter;
		synchronized(formatLock) {
			if(formatter == null)
				formatter = new defaultLogFormatter();
		}
		return formatter;
	}


	// log level
	public void setLevel(xLevel lvl) {
		this.level = lvl;
	}
	// is level loggable
	public boolean isLoggable(xLevel lvl) {
		if(level != null && level.isLoggable(lvl))
			return true;
		return false;
	}


}
