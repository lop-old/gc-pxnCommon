/*
package com.poixson.commonjava.xLogger.formatters;

import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.exceptions.RequiredArgumentException;
import com.poixson.commonjava.xLogger.xLevel;
import com.poixson.commonjava.xLogger.xLogRecord;


public class xLogFormatter_Color extends xLogFormatter_Default {



	// level
	@Override
	protected String partLevel(final xLogRecord record) {
		if(record == null) throw new RequiredArgumentException("record");
		final xLevel level = record.level();
		final String color;
		// all, finest, finer, fine
		if(level.isLoggable(xLevel.FINE))
			color = "FG_BLACK,BOLD";
//			color = "FG_WHITE,BOLD";
		// info
		else if(level.isLoggable(xLevel.INFO))
			color = "FG_CYAN";
//			color = "FG_CYAN,BOLD";
		// warning
		else if(level.isLoggable(xLevel.WARNING))
			color = "FG_RED";
		// severe
		else if(level.isLoggable(xLevel.SEVERE))
			color = "FG_RED,BOLD";
		// fatal
		else if(level.isLoggable(xLevel.FATAL))
			color = "FG_RED,BOLD,UNDERLINE";
		// stdout
		else if(level.isLoggable(xLevel.STDOUT))
			color = "FG_GREEN";
		// stderr
		else if(level.isLoggable(xLevel.STDERR))
			color = "FG_YELLOW";
		// off
		else
			color = "FG_BLACK,BOLD";
		return (new StringBuilder())
			.append("@|FG_BLACK,BOLD [|@@|").append(color).append(" ")
			.append(utilsString.padCenter(
				7,
				level.toString(),
				' '
			))
			.append("|@@|FG_BLACK,BOLD ]|@")
			.toString();
	}



}
*/
