package com.poixson.utils.xLogger;

import java.io.IOException;
import java.io.OutputStream;


//not thread safe - variables not volatile, assuming only used in one thread
public class xLogOutputStream extends OutputStream {

	private final xLog   log;
	private final xLevel printLevel;

	private StringBuilder buffer = new StringBuilder();



	public xLogOutputStream() {
		this(null, null);
	}
	public xLogOutputStream(final xLog outputLog) {
		this(outputLog, null);
	}
	public xLogOutputStream(final xLevel printLevel) {
		this(null, printLevel);
	}
	public xLogOutputStream(final xLog outputLog, final xLevel printLevel) {
		this.log = (
			outputLog == null
			? xLog.getRoot()
			: outputLog
		);
		this.printLevel = printLevel;
	}



	@Override
	public void write(final int b) throws IOException {
		if (b == '\r')
			return;
		// flush buffer
		if (b == '\n') {
			this.log
				.publish(
					this.printLevel,
					this.buffer.toString()
				);
			// reset buffer
			if (this.buffer.length() > 40) {
				this.buffer = new StringBuilder();
			} else {
				this.buffer.setLength(0);
			}
			return;
		}
		// append to buffer
		this.buffer.append(
			(char) b
		);
	}



}
