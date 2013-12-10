package com.poixson.commonjava.xLogger;

import java.util.List;


public class xLogRecord {

	private final xLog log;
	private final xLevel level;
	private final String msg;


	// new record instance
	public xLogRecord(xLog log, xLevel level, String msg) {
		this.log   = log;
		this.level = level;
		this.msg   = msg;
	}


	// get level
	public xLevel getLevel() {
		return level;
	}
	// java util level type
	public java.util.logging.Level getJavaLevel() {
		if(level == null)
			return null;
		return level.getJavaLevel();
	}


	// get message
	public String getMsg() {
		return msg;
	}


	// [logger] [crumbs]
	public List<String> getNameTree() {
		return log.getNameTree();
	}


}
