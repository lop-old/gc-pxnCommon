package com.poixson.commonjava.xLogger;

import java.util.List;

import com.poixson.commonjava.Utils.utilsString;


public abstract class xLogPrinting {



	public abstract xLog get(final String name);
	public abstract xLog getWeak();
	public abstract xLog getWeak(final String name);

	public abstract boolean isRoot();
	public abstract List<String> getNameTree();
	public abstract void addHandler(final xLogHandler handler);
	public abstract void setHandler(final xLogHandler handler);



	// publish message
	public abstract void publish(final xLogRecord record);
	public abstract void publish(final String msg);
	public void publish(final xLevel level, final String msg) {
		this.publish(
			new xLogRecord(
				(xLog) this,
				level,
				msg
			)
		);
	}
	public void publish() {
		publish("");
	}



	// title
	public void title(final String msg) {
		if(msg == null)
			this.publish(" @|FG_MAGENTA [[|@ @|FG_CYAN <null>|@ @|FG_MAGENTA ]]|@");
		else
			this.publish(" @|FG_MAGENTA [[|@ @|FG_CYAN "+msg+"|@ @|FG_MAGENTA ]]|@");
	}
	// multi-lined title
	public void title(final String[] msgs) {
		// find max length
		int len = 0;
		for(final String line : msgs)
			if(line.length() > len)
				len = line.length();
		// print lines
		for(final String line : msgs)
			this.publish(" [[ "+line+utilsString.repeat(len - line.length(), " ")+" ]]");
	}
	public void title(final List<String> list) {
		if(list == null) return;
		this.title(list.toArray(new String[0]));
	}



	// exception
	public void trace(final Throwable e) {
		this.publish(
			xLevel.SEVERE,
			utilsString.ExceptionToString(e)
		);
	}



	// stdout
	public void stdout(final String msg) {
		this.publish(xLevel.STDOUT, msg);
	}
	// stderr
	public void stderr(final String msg) {
		this.publish(xLevel.STDERR, msg);
	}



	// finest
	public void finest(final String msg) {
		this.publish(xLevel.FINEST, msg);
	}
	// finer
	public void finer(final String msg) {
		this.publish(xLevel.FINER, msg);
	}
	// fine
	public void fine(final String msg) {
		this.publish(xLevel.FINE, msg);
	}
	// stats
	public void stats(final String msg) {
		this.publish(xLevel.STATS, msg);
	}
	// info
	public void info(final String msg) {
		this.publish(xLevel.INFO, msg);
	}
	// warning
	public void warning(final String msg) {
		this.publish(xLevel.WARNING, msg);
	}
	// severe
	public void severe(final String msg) {
		this.publish(xLevel.SEVERE, msg);
	}
	// fatal
	public void fatal(final String msg) {
		this.publish(xLevel.FATAL, msg);
	}



}
