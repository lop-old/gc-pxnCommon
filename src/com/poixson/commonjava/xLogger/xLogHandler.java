package com.poixson.commonjava.xLogger;

import com.poixson.commonjava.xLogger.formatters.defaultLogFormatter;


public abstract class xLogHandler {

	private volatile xLogFormatter formatter = null;
	private volatile xLevel level = null;
	private final Object formatLock = new Object();


	public abstract void publish(final xLogRecord record);
	public abstract void publish(final String msg);
	protected String msgFormat(final xLogRecord record) {
		return getFormatter().formatMsg(record);
	}


	// formatter
	public void setFormatter(final xLogFormatter formatter) {
		synchronized(formatLock) {
			this.formatter = formatter;
		}
	}
	protected xLogFormatter getFormatter() {
		if(formatter == null) {
			synchronized(formatLock) {
				if(formatter == null)
					formatter = new defaultLogFormatter();
			}
		}
		return formatter;
	}


	// log level
	public void setLevel(final xLevel lvl) {
		this.level = lvl;
	}
	public xLevel getLevel() {
		return level;
	}
	// is level loggable
	public boolean isLoggable(final xLevel lvl) {
		return (level != null && level.isLoggable(lvl));
	}


}
