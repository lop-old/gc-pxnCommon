package com.poixson.commonjava.xLogger.formatters;

import com.poixson.commonjava.xLogger.xLogRecord;


public class xLogFormatter_Simple extends xLogFormatter_Default {



	// timestamp
	@Override
	protected String partTimestamp(final xLogRecord record) {
		return " ";
	}



}
