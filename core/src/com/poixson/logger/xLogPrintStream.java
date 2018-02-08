package com.poixson.logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;


// xLog print stream
public class xLogPrintStream extends PrintStream {

	private final xLog log;



	public xLogPrintStream() {
		this(null, null);
	}
	public xLogPrintStream(final xLog log) {
		this(log, null);
	}
	public xLogPrintStream(final xLevel printLevel) {
		this(null, printLevel);
	}
	public xLogPrintStream(final xLog log, final xLevel printLevel) {
		super(
			new xLogOutputStream(log, printLevel)
		);
		this.log = log;
	}



	@Override
	public void println() {
		if (this.log != null)
			this.log.publish();
	}



	public xLog getLog() {
		return this.log;
	}



	@Override
	public void flush() {
	}



	// ------------------------------------------------------------------------------- //
	// output stream class



	//not thread safe - variables not volatile, assuming only used in one thread
	public static class xLogOutputStream extends OutputStream {

		private final xLog   log;
		private final xLevel level;

		private StringBuilder buffer;



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
			super();
			this.log   = outputLog;
			this.level = printLevel;
			this.buffer = (
				outputLog == null
				? null
				: new StringBuilder()
			);
		}



		@Override
		public void write(final int b) throws IOException {
			if (this.log == null)
				return;
			if (b == '\r')
				return;
			// flush buffer
			if (b == '\n') {
				this.flush();
				return;
			}
			// append to buffer
			this.buffer
				.append( (char) b );
		}
		@Override
		public void write(final byte b[]) throws IOException {
			write(b, 0, b.length);
		}
		@Override
		public void write(final byte bytes[], final int off,
				final int len) throws IOException {
			if (bytes == null) throw new NullPointerException();
			if (off < 0)                      throw new IndexOutOfBoundsException();
			if (off > bytes.length)           throw new IndexOutOfBoundsException();
			if (len < 0)                      throw new IndexOutOfBoundsException();
			if ( (off + len) > bytes.length ) throw new IndexOutOfBoundsException();
			if ( (off + len) < 0)             throw new IndexOutOfBoundsException();
			if (this.log == null) return;
			if (len == 0)         return;
			for (int i=0; i<len; i++) {
				final byte b = bytes[ off+i ];
				if (b == '\r')
					continue;
				// flush buffer
				if (b == '\n') {
					this.flush();
					continue;
				}
				// append to buffer
				this.buffer
					.append( (char) b );
			}
		}



		@Override
		public void flush() throws IOException {
			this.log
				.publish(
					this.level,
					this.buffer.toString()
				);
			// reset buffer
			if (this.buffer.length() > 40) {
				this.buffer = new StringBuilder();
			} else {
				this.buffer.setLength(0);
			}
		}



	}



}
