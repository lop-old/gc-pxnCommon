package com.poixson.commonjava.xLogger.handlers;

import com.poixson.commonjava.xLogger.xConsole;
import com.poixson.commonjava.xLogger.xLogHandler;
import com.poixson.commonjava.xLogger.xLogRecord;


public class logHandlerConsole extends xLogHandler {

	private static volatile xConsole console = null;


	public logHandlerConsole() {
		if(console == null)
			console = xConsole.get();
	}


	@Override
	public void publish(xLogRecord record) {
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
