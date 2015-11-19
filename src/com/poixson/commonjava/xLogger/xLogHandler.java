package com.poixson.commonjava.xLogger;

import com.poixson.commonjava.xLogger.formatters.xLogFormatter_Default;



public abstract class xLogHandler {

	private volatile xLogFormatterInterface formatter = null;
	private volatile xLevel level = null;
	private final Object formatLock = new Object();



	public abstract void publish(final xLogRecord record);
	public abstract void publish(final String msg);
	protected String msgFormat(final xLogRecord record) {
		return getFormatter().formatMsg(record);
	}



	// formatter
	public void setFormatter(final xLogFormatterInterface formatter) {
		synchronized(this.formatLock) {
			this.formatter = formatter;
		}
	}
	protected xLogFormatterInterface getFormatter() {
		if(this.formatter == null) {
			synchronized(this.formatLock) {
				if(this.formatter == null)
					this.formatter = new xLogFormatter_Default();
			}
		}
		return this.formatter;
	}



	// log level
	public void setLevel(final xLevel lvl) {
		this.level = lvl;
	}
	public xLevel getLevel() {
		return this.level;
	}
	// is level loggable
	public boolean isLoggable(final xLevel lvl) {
		if(lvl == null || this.level == null)
			return true;
		return this.level.isLoggable(lvl);
	}



}
