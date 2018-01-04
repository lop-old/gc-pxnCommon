package com.poixson.utils.xLogger;

import com.poixson.utils.StringUtils;
import com.poixson.utils.exceptions.RequiredArgumentException;


public class xLogFormatter_Color extends xLogFormatter_Default {



	// level
	@Override
	protected String partLevel(final xLogRecord record) {
		if (record == null) throw RequiredArgumentException.getNew("record");
		final xLevel level = record.level();
		final String color;
		// all, finest, finer, fine
		if (level.isLoggable(xLevel.FINE)) {
			color = "FG_BLACK,BOLD";
			//color = "FG_WHITE,BOLD";
		} else
		// info
		if (level.isLoggable(xLevel.INFO)) {
			color = "FG_CYAN";
			//color = "FG_CYAN,BOLD";
		} else
		// warning
		if (level.isLoggable(xLevel.WARNING)) {
			color = "FG_RED";
		} else
		// severe
		if (level.isLoggable(xLevel.SEVERE)) {
			color = "FG_RED,BOLD";
		} else
		// fatal
		if (level.isLoggable(xLevel.FATAL)) {
			color = "FG_RED,BOLD,UNDERLINE";
		} else
		// stdout
		if (level.isLoggable(xLevel.STDOUT)) {
			color = "FG_GREEN";
		} else
		// stderr
		if (level.isLoggable(xLevel.STDERR)) {
			color = "FG_YELLOW";
		// off
		} else {
			color = "FG_BLACK,BOLD";
		}
		return (new StringBuilder())
			.append("@|FG_BLACK,BOLD [|@@|")
			.append(color)
			.append(" ")
			.append(
				StringUtils.PadCenter(
					7,
					level.toString(),
					' '
				)
			)
			.append("|@@|FG_BLACK,BOLD ]|@")
			.toString();
	}



}
