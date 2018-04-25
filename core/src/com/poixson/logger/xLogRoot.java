package com.poixson.logger;

import java.util.concurrent.atomic.AtomicReference;

import com.poixson.app.xVars;
import com.poixson.logger.printers.xLogPrinter;
import com.poixson.logger.printers.xLogPrinter_stdio;
import com.poixson.tools.Keeper;
import com.poixson.utils.Utils;


public class xLogRoot extends xLog {

	public static final xLevel DEFAULT_LEVEL = xLevel.FINEST;

	// root logger
	private static final AtomicReference<xLogRoot> root =
			new AtomicReference<xLogRoot>(null);

	private final AtomicReference<xLogPrinter> defaultPrinter =
			new AtomicReference<xLogPrinter>(null);



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
				Keeper.add(log);
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
		if (xVars.isDebug())
			return xLevel.DETAIL;
		final xLevel level = super.level.get();
		if (level != null)
			return level;
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



	public void clearScreen() {
		final xConsole console = xVars.getConsole();
		if (console != null) {
			console.clearScreen();
		}
	}
	public void clearLine() {
		final xConsole console = xVars.getConsole();
		if (console != null) {
			console.clearLine();
		}
	}
	public void beep() {
		final xConsole console = xVars.getConsole();
		if (console != null) {
			console.beep();
		}
	}



}
