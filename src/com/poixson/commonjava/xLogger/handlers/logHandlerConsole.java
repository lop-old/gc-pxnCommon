package com.poixson.commonjava.xLogger.handlers;

import com.poixson.commonjava.xLogger.xConsole;
import com.poixson.commonjava.xLogger.xLog;
import com.poixson.commonjava.xLogger.xLogHandler;
import com.poixson.commonjava.xLogger.xLogRecord;


public class logHandlerConsole extends xLogHandler {

	private static volatile xConsole console = null;


	public logHandlerConsole() {
		if(console == null)
			console = xLog.getConsole();
	}


	@Override
	public void publish(final xLogRecord record) {
		if(console == null)
			System.out.println(
				msgFormat(record)
			);
		else
			console.print(
				msgFormat(record)
			);
	}


}
