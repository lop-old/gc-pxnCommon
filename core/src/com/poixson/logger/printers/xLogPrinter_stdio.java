package com.poixson.logger.printers;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.locks.ReentrantLock;

import com.poixson.app.xVars;
import com.poixson.logger.xConsole;
import com.poixson.logger.formatters.xLogFormatter;
import com.poixson.logger.formatters.xLogFormatter_Detailed;
import com.poixson.utils.ShellUtils;
import com.poixson.utils.Utils;


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
	protected void doPublish(final String[] lines) throws IOException {
		this.getPublishLock();
		try {
			// blank line
			if (Utils.isEmpty(lines)) {
				this.doPublish( (String)null );
			} else
			// single line
			if (lines.length == 1) {
				this.doPublish(lines[0]);
			// multiple lines
			} else {
				for (final String line : lines) {
					this.doPublish(line);
				}
			}
		} finally {
			this.releasePublishLock();
		}
	}
	protected void doPublish(final String line) {
		final xConsole console = xVars.getConsole();
		// print to shell
		if (console == null) {
			if (line == null) {
				this.out.println();
			} else {
				this.out.println(
					ShellUtils.RenderAnsi(
						line
					)
				);
			}
			this.out.flush();
		// print to console handler
		} else {
			console.doPublish(
				ShellUtils.RenderAnsi(
					line
				)
			);
		}
	}



	@Override
	public void flush() {
		final xConsole console = xVars.getConsole();
		if (console != null) {
			console.doFlush();
		} else {
			this.out.flush();
		}
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
