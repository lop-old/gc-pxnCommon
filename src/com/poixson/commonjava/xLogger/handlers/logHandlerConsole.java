package com.poixson.commonjava.xLogger.handlers;

import com.poixson.commonjava.xLogger.xConsole;
import com.poixson.commonjava.xLogger.xLog;
import com.poixson.commonjava.xLogger.xLogHandler;
import com.poixson.commonjava.xLogger.xLogRecord;


public class logHandlerConsole extends xLogHandler {


	public logHandlerConsole() {
	}


	@Override
	public void publish(final xLogRecord record) {
		publish(msgFormat(record));
	}
	@Override
	public void publish(final String msg) {
		final xConsole console = xLog.getConsole();
		if(console == null) {
			System.out.println(msg);
		} else {
			console.print(msg);
		}
	}


}
