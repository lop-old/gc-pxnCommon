package com.poixson.utils.xLogger;

import java.io.PrintStream;


public class xLogPrintStream extends PrintStream {

	private final xLog log;



	public xLogPrintStream(final xLog log, final xLevel level) {
		super(
			new xLogOutputStream(log, level)
		);
		this.log = log;
	}



	@Override
	public void println() {
		this.log.publish();
	}



}
