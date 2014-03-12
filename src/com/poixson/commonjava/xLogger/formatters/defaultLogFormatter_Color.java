package com.poixson.commonjava.xLogger.formatters;

import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.xLogger.xLevel;
import com.poixson.commonjava.xLogger.xLogRecord;


public class defaultLogFormatter_Color extends defaultLogFormatter {


	// level
	@Override
	protected String partLevel(final xLogRecord record) {
		final xLevel level = record.level();
		final String color;
		// all, finest, finer, fine
		if(level.isLoggable(xLevel.FINE))
			color = "FG_WHITE,BOLD";
		// info
		else if(level.isLoggable(xLevel.INFO))
			color = "FG_CYAN,BOLD";
		// warning
		else if(level.isLoggable(xLevel.WARNING))
			color = "FG_RED";
		// severe
		else if(level.isLoggable(xLevel.SEVERE))
			color = "FG_RED,BOLD";
		// fatal
		else if(level.isLoggable(xLevel.FATAL))
			color = "FG_RED,BOLD,UNDERLINE";
		// off
		else
			color = "FG_BLACK,BOLD";
		return (new StringBuilder())
			.append("@|FG_BLACK,BOLD [|@@|").append(color).append(" ")
			.append(utilsString.padCenter(
				7,
				level.toString(),
				" "
			))
			.append("|@@|FG_BLACK,BOLD ]|@")
			.toString();
	}


}
