package com.poixson.utils.xLogger;

import java.lang.ref.SoftReference;


public abstract class xLogHandler {

	private volatile xLogFormatter formatter = null;
	private volatile SoftReference<xLogFormatter> formatterDefault = null;

	private volatile xLevel level = null;



	public abstract void publish(final xLogRecord record);
	public abstract void publish(final String msg);



	// log level
	public void setLevel(final xLevel lvl) {
		this.level = lvl;
	}
	public xLevel getLevel() {
		return this.level;
	}
	// is level loggable
	public boolean isLoggable(final xLevel lvl) {
		if (lvl == null || this.level == null)
			return true;
		return this.level.isLoggable(lvl);
	}



	protected String msgFormat(final xLogRecord record) {
		return getFormatter()
				.formatMsg(record);
	}



	// formatter
	public void setFormatter(final xLogFormatter formatter) {
		this.formatter = formatter;
	}
	protected xLogFormatter getFormatter() {
		xLogFormatter formatter = this.formatter;
		if (formatter != null) {
			return formatter;
		}
		// use default formatter
		formatter = this.formatterDefault.get();
		if (formatter == null) {
			formatter = new xLogFormatter_Default();
			this.formatterDefault = new SoftReference<xLogFormatter>(
					formatter
			);
		}
		return formatter;
	}



}
