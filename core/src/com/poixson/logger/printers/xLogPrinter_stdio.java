package com.poixson.logger.printers;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.locks.ReentrantLock;

import com.poixson.app.xVars;
import com.poixson.logger.xLogRoot;
import com.poixson.logger.console.xConsole;
import com.poixson.logger.formatters.xLogFormatter;
import com.poixson.logger.formatters.xLogFormatter_Detailed;
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



	// ------------------------------------------------------------------------------- //
	// default formatter



	@Override
	public xLogFormatter getDefaultFormatter() {
		// existing instance
		{
			final xLogFormatter formatter = super.defaultFormatter.get();
			if (formatter != null)
				return formatter;
		}
		// enable console color if possible
		if (xVars.isColorEnabled()) {
			try {
				final Class<?> clss =
					Class.forName(
						"com.poixson.logger.formatters.xLogFormatter_Color"
					);
				if (clss == null) throw new ClassNotFoundException();
				final xLogFormatter formatter =
					(xLogFormatter) clss.newInstance();
				if ( ! super.defaultFormatter.compareAndSet(null, formatter) )
					return super.defaultFormatter.get();
				return formatter;
			} catch (ClassNotFoundException ignore) {
			} catch (InstantiationException ignore) {
			} catch (IllegalAccessException ignore) {
			}
		}
		// new default formatter
		{
			final xLogFormatter formatter =
				new xLogFormatter_Detailed();
			if ( ! super.defaultFormatter.compareAndSet(null, formatter) )
				return super.defaultFormatter.get();
			return formatter;
		}
	}



}
