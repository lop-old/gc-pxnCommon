package com.poixson.commonjava.xLogger.handlers;

import com.poixson.commonjava.xLogger.xLogHandler;
import com.poixson.commonjava.xLogger.xLogRecord;


public class logHandlerConsole extends xLogHandler {


	@Override
	public void publish(xLogRecord record) {
		System.out.println(
			doFormat(record)
		);
	}


}
