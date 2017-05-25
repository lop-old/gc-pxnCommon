package com.poixson.utils.xLogger;

import java.io.IOException;
import java.io.OutputStream;


//not thread safe - variables not volatile, assuming only used in one thread
public class xLogOutputStream extends OutputStream {

	private final xLog   log;
	private final xLevel level;

	private StringBuilder buffer = new StringBuilder();



	public xLogOutputStream(final xLog log, final xLevel level) {
		this.log   = log;
		this.level = level;
	}



	@Override
	public void write(final int b) throws IOException {
		// flush buffer
		if (b == '\n') {
			xLog log = this.log;
			if (log == null) {
				log = xLog.getRoot();
			}
			synchronized(this) {
				log.publish(
					this.level,
					this.buffer.toString()
				);
				// reset buffer
				this.buffer = new StringBuilder();
			}
			return;
		}
		// append to buffer
		this.buffer.append(
			(char) b
		);
	}



}
