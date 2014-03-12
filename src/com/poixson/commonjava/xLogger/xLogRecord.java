package com.poixson.commonjava.xLogger;

import java.util.List;

import com.poixson.commonjava.Utils.xClock;


public class xLogRecord {

	private final xLog log;
	private final xLevel level;
	private final long timestamp;
	private final String msg;


	// new record instance
	public xLogRecord(final xLog log, final xLevel level, final String msg) {
		this.timestamp = xClock.get(false).millis();
		this.log   = log;
		this.level = level;
		this.msg   = msg;
	}


	// get level
	public xLevel level() {
		return level;
	}
	// java util level type
	public java.util.logging.Level javaLevel() {
		if(level == null)
			return null;
		return level.getJavaLevel();
	}


	// get timestamp
	public long timestamp() {
		return timestamp;
	}


	// get message
	public String msg() {
		return msg;
	}


	// [logger] [crumbs]
	public List<String> getNameTree() {
		return log.getNameTree();
	}


}
