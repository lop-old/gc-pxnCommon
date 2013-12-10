package com.poixson.commonjava.xLogger;

import java.util.List;

import com.poixson.commonjava.Utils.utilsString;


public abstract class xLogPrinting {


	public abstract xLog get(String name);
	public abstract xLog getAnon(String name);
	protected abstract xLog newInstance(String name);

	public abstract boolean isRoot();
	public abstract List<String> getNameTree();
	public abstract void addHandler(xLogHandler handler);


	// publish message
	public abstract void publish(xLogRecord record);
	public void publish(xLevel level, String msg) {
		publish(
			new xLogRecord(
				(xLog) this,
				level,
				msg
			)
		);
	}


	// exception
	public void trace(Exception e) {
		publish(
			xLevel.SEVERE,
			utilsString.ExceptionToString(e)
		);
	}


	// finest
	public void finest(String msg) {
		publish(
			xLevel.FINEST,
			msg
		);
	}
	// finer
	public void finer(String msg) {
		publish(
			xLevel.FINER,
			msg
		);
	}
	// fine
	public void fine(String msg) {
		publish(
			xLevel.FINE,
			msg
		);
	}
	// stats
	public void stats(String msg) {
		publish(
			xLevel.STATS,
			msg
		);
	}
	// info
	public void info(String msg) {
		publish(
			xLevel.INFO,
			msg
		);
	}
	// warning
	public void warning(String msg) {
		publish(
			xLevel.WARNING,
			msg
		);
	}
	// severe
	public void severe(String msg) {
		publish(
			xLevel.SEVERE,
			msg
		);
	}
	// fatal
	public void fatal(String msg) {
		publish(
			xLevel.FATAL,
			msg
		);
	}


	// boolean
	public void finest(Boolean b) {
		finest(utilsString.toString(b));
	}
	public void finer(Boolean b) {
		finer(utilsString.toString(b));
	}
	public void fine(Boolean b) {
		fine(utilsString.toString(b));
	}
	public void debug(Boolean b) {
		debug(utilsString.toString(b));
	}
	public void stats(Boolean b) {
		stats(utilsString.toString(b));
	}
	public void info(Boolean b) {
		info(utilsString.toString(b));
	}
	public void warning(Boolean b) {
		warning(utilsString.toString(b));
	}
	public void severe(Boolean b) {
		severe(utilsString.toString(b));
	}
	public void fatal(Boolean b) {
		fatal(utilsString.toString(b));
	}


}
