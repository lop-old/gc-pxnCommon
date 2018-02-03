package com.poixson.logger;

import java.util.concurrent.atomic.AtomicReference;

import com.poixson.app.xVars;
import com.poixson.logger.console.xConsole;
import com.poixson.logger.formatters.xLogFormatter;
import com.poixson.logger.formatters.xLogFormatter_Detailed;
import com.poixson.logger.printers.xLogPrinter;
import com.poixson.logger.printers.xLogPrinter_stdio;
import com.poixson.utils.Utils;


public class xLogRoot extends xLog {

	public static final xLevel DEFAULT_LEVEL = xLevel.FINEST;

	// root logger
	private static final AtomicReference<xLogRoot> root =
			new AtomicReference<xLogRoot>(null);

	private final AtomicReference<xLogPrinter> defaultPrinter =
			new AtomicReference<xLogPrinter>(null);
	private final AtomicReference<xLogFormatter> defaultFormatter =
			new AtomicReference<xLogFormatter>(null);

	private final AtomicReference<xConsole> console =
			new AtomicReference<xConsole>(null);



	// get root logger
	public static xLogRoot get() {
		// existing root logger
		{
			final xLogRoot log = root.get();
			if (log != null)
				return log;
		}
		// new root logger instance
		{
			final xLogRoot log = new xLogRoot();
			if ( ! root.compareAndSet(null, log) )
				return root.get();
			// init root logger
			{
				log.setLevel(DEFAULT_LEVEL);
			}
			return log;
		}
	}
	public static xLogRoot peek() {
		return root.get();
	}



	protected xLogRoot() {
		super(null, null);
	}



	// ------------------------------------------------------------------------------- //
	// config



	@Override
	public xLevel getLevel() {
		if (xVars.debug())
			return xLevel.DETAIL;
		final xLevel lvl = super.level.get();
		if (lvl != null)
			return lvl;
		return DEFAULT_LEVEL;
	}

	@Override
	public boolean isRoot() {
		return true;
	}



	// ------------------------------------------------------------------------------- //
	// printer handlers



	public xLogPrinter[] getPrinters() {
		final xLogPrinter[] printers =
			super.getPrinters();
		if (Utils.notEmpty(printers))
			return printers;
		return
			new xLogPrinter[] {
				getDefaultPrinter()
			};
	}
	public xLogPrinter getDefaultPrinter() {
		// existing instance
		{
			final xLogPrinter printer = this.defaultPrinter.get();
			if (printer != null)
				return printer;
		}
		{
			final xLogPrinter printer = this.newDefaultPrinter();
			if ( ! this.defaultPrinter.compareAndSet(null, printer) )
				return this.defaultPrinter.get();
			return printer;
		}
	}
	protected xLogPrinter newDefaultPrinter() {
		return new xLogPrinter_stdio();
	}



	// ------------------------------------------------------------------------------- //
	// printer formatter



	public xLogFormatter getDefaultFormatter() {
		// existing instance
		{
			final xLogFormatter formatter = this.defaultFormatter.get();
			if (formatter != null)
				return formatter;
		}
		{
			final xLogFormatter formatter = new xLogFormatter_Detailed();
			if ( ! this.defaultFormatter.compareAndSet(null, formatter) )
				return this.defaultFormatter.get();
			return formatter;
		}
	}



	// ------------------------------------------------------------------------------- //
	// printer formatter



	public xConsole getConsole() {
		return this.console.get();
	}
	public void setConsole(final xConsole console) {
		this.console.set(console);
	}



}
