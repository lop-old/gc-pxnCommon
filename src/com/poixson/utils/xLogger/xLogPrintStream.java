package com.poixson.utils.xLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;


public class xLogPrintStream extends PrintStream {

	private final xLog log;



	private static class xLogOutputStream extends OutputStream {

		private volatile xLog   log   = null;
		private volatile xLevel level = null;

		private final StringBuilder buffer = new StringBuilder();

		public xLogOutputStream() {}
		public xLogOutputStream init(final xLog log, final xLevel level) {
			this.log   = log;
			this.level = level;
			return this;
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



	@SuppressWarnings("resource")
	public xLogPrintStream(final xLog log, final xLevel level) {
		super(
			(new xLogOutputStream())
				.init(log, level)
		);
		this.log = log;
	}



	@Override
	public void println() {
		this.log.publish();
	}



}
