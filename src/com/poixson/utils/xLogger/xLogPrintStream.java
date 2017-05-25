package com.poixson.utils.xLogger;

import java.io.PrintStream;


public class xLogPrintStream extends PrintStream {

	private final xLog log;



	public xLogPrintStream() {
		this(null, null);
	}
	public xLogPrintStream(final xLog outputLog) {
		this(outputLog, null);
	}
	public xLogPrintStream(final xLevel printLevel) {
		this(null, printLevel);
	}
	public xLogPrintStream(final xLog outputLog, final xLevel printLevel) {
		super(
			new xLogOutputStream(outputLog, printLevel)
		);
		this.log = (
			outputLog == null
			? xLog.getRoot()
			: outputLog
		);
	}



	@Override
	public void println() {
		this.log.publish();
	}



}
