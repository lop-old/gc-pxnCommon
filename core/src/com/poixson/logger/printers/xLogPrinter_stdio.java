package com.poixson.logger.printers;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.SoftReference;
import java.util.concurrent.locks.ReentrantLock;

import com.poixson.app.xVars;
import com.poixson.logger.xLogRoot;
import com.poixson.logger.console.xConsole;


public class xLogPrinter_stdio extends xLogPrinterBasic {

	protected static final ReentrantLock globalPublishLock = new ReentrantLock(true);

	protected final PrintStream out;
	protected SoftReference<xConsole> softConsole = null;



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
			final SoftReference<xConsole> soft = this.softConsole;
			xConsole console = null;
			if (soft == null) {
				console = xLogRoot.get().getConsole();
				this.softConsole = new SoftReference<xConsole>( console );
			} else {
				console = soft.get();
			}
			if (console != null) {
				console.println(line);
				return;
			}
		}
		this.out.println(line);
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
