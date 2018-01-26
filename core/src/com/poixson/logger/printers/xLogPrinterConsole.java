package com.poixson.logger;


public class xLogHandlerConsole extends xLogHandler {



	public xLogHandlerConsole() {
	}



	@Override
	public void publish(final xLogRecord record) {
		publish(
			msgFormat(record)
		);
	}
	@Override
	public void publish(final String msg) {
		final xConsole console = xLog.getConsole();
		if (console == null) {
			System.out.println(msg);
		} else {
			console.print(msg);
		}
	}



}
