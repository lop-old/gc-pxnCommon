package com.poixson.utils.remapped;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;


public abstract class OutputStreamLineRemapper extends OutputStream {

	private final StringBuilder buf = new StringBuilder();



	public static PrintStream toPrintStream(final OutputStream outstream) {
		return new PrintStream(outstream, false);
	}
	public OutputStreamLineRemapper() {
		super();
	}



	public abstract void line(final String line);



	@Override
	public void write(final int b) throws IOException {
		if (b == '\r')
			return;
		if (b == '\n') {
			this.line(
				this.buf.toString()
			);
			this.buf.setLength(0);
			return;
		}
		this.buf.append(
			Character.toChars(b)
		);
	}



}
