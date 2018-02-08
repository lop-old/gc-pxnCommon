package com.poixson.logger.printers;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.locks.ReentrantLock;

import com.poixson.app.xVars;
import com.poixson.logger.xLogRoot;
import com.poixson.logger.console.xConsole;
import com.poixson.utils.StringUtils;


public class xLogPrinter_stdio extends xLogPrinterBasic {

	protected static final ReentrantLock globalPublishLock = new ReentrantLock(true);

	protected final PrintStream out;



	public xLogPrinter_stdio() {
		super();
		this.out = xVars.getOriginalOut();
		if (this.out == null) throw new NullPointerException();
	}



	// ------------------------------------------------------------------------------- //
	// publish



	@Override
	public void publish(final String line) {
		{
			final xConsole console =
				xLogRoot.get()
					.getConsole();
			if (console != null) {
				if (line == null) {
					console.println();
				} else {
					console.println(line);
				}
				return;
			}
		}
		if (line == null) {
			this.out.println();
		} else {
			this.out.println(
				StringUtils.StripColorTags(
					line
				)
			);
		}
	}



	// ------------------------------------------------------------------------------- //
	// publish lock



	@Override
	public void getPublishLock() throws IOException {
		super.getPublishLock(
			globalPublishLock
		);
	}
	@Override
	public void releasePublishLock() {
		super.releasePublishLock(
			globalPublishLock
		);
	}



}
