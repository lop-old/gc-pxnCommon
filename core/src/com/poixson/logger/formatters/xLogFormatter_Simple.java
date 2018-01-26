package com.poixson.logger.formatters;

import com.poixson.logger.xLogRecord;


public class xLogFormatter_Simple extends xLogFormatter_Default {



//TODO: timestamp
	@Override
	protected String partTimestamp(final xLogRecord record) {
		return " ";
	}



}
