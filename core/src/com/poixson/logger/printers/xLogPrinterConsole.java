package com.poixson.logger.printers;

import com.poixson.logger.xLog;
import com.poixson.logger.xLogRecord;
import com.poixson.logger.console.xConsole;


public class xLogPrinterConsole extends xLogPrinter {



	public xLogPrinterConsole() {
		super();
	}



	@Override
	public void publish(final xLogRecord record) {
		publish(
			msgFormat(record)
		);
	}
	@Override
	public void publish(final String msg) {
		final xConsole console = xLog.getConsoleHandler();
		if (console == null) {
			System.out.println(msg);
		} else {
			console.print(msg);
		}
	}



}
