package com.poixson.logger.printers;

import java.lang.ref.SoftReference;

import com.poixson.logger.xLevel;
import com.poixson.logger.xLogRecord;
import com.poixson.logger.formatters.xLogFormatter;
import com.poixson.logger.formatters.xLogFormatter_Default;


public abstract class xLogPrinter {

	private volatile xLogFormatter formatter = null;
	private volatile SoftReference<xLogFormatter> formatterDefault = null;

	private volatile xLevel level = null;



	public xLogPrinter() {
	}



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
		// use custom formatter
		xLogFormatter formatter = this.formatter;
		if (formatter != null) {
			return formatter;
		}
		// use default formatter
		return this.getDefaultFormatter();
	}
	public xLogFormatter getDefaultFormatter() {
		final SoftReference<xLogFormatter> formDef = this.formatterDefault;
		if (formDef != null) {
			return formDef.get();
		}
		final xLogFormatter formatter = new xLogFormatter_Default();
		this.formatterDefault = new SoftReference<xLogFormatter>(
				formatter
		);
		return formatter;
	}



}
