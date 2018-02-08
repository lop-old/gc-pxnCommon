package com.poixson.logger;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.tools.xClock;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class xLogRecord {

	public final xLog     log;
	public final xLevel   level;
	public final long     timestamp;
	public final String[] linesRaw;
	public final int      lineCount;
	public final Object[] args;



	// new record instance
	public xLogRecord(final xLog log, final xLevel level,
			final String[] lines, Object[] args) {
		if (log == null) throw new RequiredArgumentException("log");
		this.timestamp = xClock.get(false).millis();
		this.log   = log;
		this.level = level;
		this.args  = args;
		if (lines == null || lines.length == 0) {
			this.linesRaw = null;
		} else
		if (lines.length == 1 && lines[0] != null
		&& lines[0].contains("\n")) {
			this.linesRaw =
				lines[0]
					.replace("\r", "")
					.split("\n");
		} else {
			this.linesRaw = lines;
		}
		this.lineCount = (
			this.linesRaw == null
			? 0
			: this.linesRaw.length
		);
	}



	// message lines
	public int getLongestLine() {
		return StringUtils.FindLongestLine(this.lines);
	}
	public boolean isEmpty() {
		return Utils.isEmpty(this.linesRaw);
	}
	public boolean notEmpty() {
		return Utils.notEmpty(this.linesRaw);
	}



	// level
	public String getLevelStr() {
		return (
			this.level == null
			? "<null>"
			: this.level.toString()
		);
	}
	// java util level type
	public java.util.logging.Level getJavaLevel() {
		if (this.level == null)
			return null;
		return this.level.getJavaLevel();
	}



	// [logger] [crumbs]
	public String[] getNameTree() {
		return this.log
				.getNameTree();
	}



}
