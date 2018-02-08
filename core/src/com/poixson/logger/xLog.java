package com.poixson.logger;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import com.poixson.app.xVars;
import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.formatters.xLogFormatter;
import com.poixson.logger.printers.xLogPrinter;
import com.poixson.tools.Keeper;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class xLog implements xLogPrinter {

	// child-loggers
	private final ConcurrentMap<String, xLog> loggers =
			new ConcurrentHashMap<String, xLog>();

	protected final String name;
	protected final xLog   parent;
	protected final AtomicReference<xLevel> level =
			new AtomicReference<xLevel>(null);

	// handlers
	private final CopyOnWriteArrayList<xLogPrinter> printers =
			new CopyOnWriteArrayList<xLogPrinter>();



	// ------------------------------------------------------------------------------- //



	// get logger
	public static xLog getRoot() {
		return xLogRoot.get();
	}
	public static xLog peekRoot() {
		return xLogRoot.peek();
	}
	public xLog get(final String logName) {
		if (Utils.isEmpty(logName))
			return xLogRoot.get();
		// existing logger instance
		{
			final xLog log = this.loggers.get(logName);
			if (log != null)
				return log;
		}
		// new logger instance
		{
			final xLog log = new xLog(this, logName);
			final xLog existing = this.loggers.putIfAbsent(logName, log);
			if (existing != null)
				return existing;
			Keeper.add(log);
			return log;
		}
	}
	// new instance (weak reference)
	public xLog getWeak(final String logName) {
		if (Utils.isEmpty(logName))
			return this.getWeak();
		return new xLog(this, logName);
	}
	public xLog getWeak() {
		return new xLog(this, this.name);
	}



	protected xLog(final xLog parent, final String logName) {
		// root logger
		if (this.isRoot()) {
			this.parent = null;
			this.name   = null;
		// child logger
		} else {
			if (parent == null) throw new RequiredArgumentException("parent");
			if (Utils.isEmpty(logName)) throw new RequiredArgumentException("logName");
			this.parent = parent;
			this.name  = logName;
		}
	}
	@Override
	public xLog clone() {
		return this.getWeak();
	}



	// ------------------------------------------------------------------------------- //
	// config



	public xLevel getLevel() {
		if (xVars.debug())
			return xLevel.DETAIL;
		return this.level.get();
	}
	public xLevel peekLevel() {
		return this.level.get();
	}
	@Override
	public void setLevel(final xLevel lvl) {
		this.level.set(lvl);
	}



	// is level loggable
	public boolean isLoggable(final xLevel level) {
		if (level == null) return true;
		// forced debug mode
		if (xVars.debug()) return true;
		// check local level
		final xLevel currentLevel = this.getLevel();
		if (currentLevel != null) {
			if (!currentLevel.isLoggable(level))
				return false;
		}
		// check parent level
		if (this.parent != null) {
			final boolean loggable = this.parent.isLoggable(level);
			return loggable;
		}
		// allow by default
		return true;
	}
	public boolean isDetailLoggable() {
		return this.isLoggable(xLevel.DETAIL);
	}



	// is root logger
	public boolean isRoot() {
		return false;
	}



	public String[] getNameTree() {
//TODO: (remember to cache this)
	}



	// ------------------------------------------------------------------------------- //
	// printers/handlers



	public xLogPrinter[] getPrinters() {
		return this.printers
				.toArray(new xLogPrinter[0]);
	}
	public void addPrinter(final xLogPrinter printer) {
		this.printers
			.add(printer);
	}



	// ------------------------------------------------------------------------------- //
	// publish



	@Override
	public void publish(final xLogRecord record) {
		// not loggable
		if ( ! this.isLoggable(record.level) )
			return;
		// pass to printers/handlers
		boolean handled = false;
		final xLogPrinter[] printers = this.getPrinters();
		if (Utils.notEmpty(printers)) {
			PRINTER_LOOP:
			for (final xLogPrinter printer : printers) {
				if (printer == null) continue;
				try {
					if ( ! printer.isLoggable(record.level) )
						continue PRINTER_LOOP;
					handled = true;
					printer.publish(record);
				} catch (Exception e) {
					e.printStackTrace(
						xVars.getOriginalErr()
					);
				}
			} // end PRINTER_LOOP
		}
		// pass to parent
		if (this.parent != null) {
			this.parent
				.publish(record);
			handled = true;
		}
		if ( ! handled ) {
			final RuntimeException e = new RuntimeException("No log handlers found!");
			e.printStackTrace(
				xVars.getOriginalErr()
			);
		}
	}



	public void getPublishLock() {
		throw new UnsupportedOperationException();
	}
	public void releasePublishLock() {
		throw new UnsupportedOperationException();
	}



	public xLogFormatter getFormatter() {
		throw new UnsupportedOperationException();
	}
	public void setFormatter(final xLogFormatter formatter) {
		throw new UnsupportedOperationException();
	}



	public void publish(final xLevel level,
			final String line, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				level,
				new String[] { line },
				args
			)
		);
	}
	@Override
	public void publish(final String[] lines) {
		this.publish(
			new xLogRecord(
				this,
				(xLevel)   null,
				lines,
				(Object[]) null
			)
		);
	}
	@Override
	public void publish(final String line) {
		this.publish(
			new xLogRecord(
				this,
				(xLevel)   null,
				new String[] { line },
				(Object[]) null
			)
		);
	}
	public void publish() {
		this.publish(
			new xLogRecord(
				this,
				(xLevel)   null,
				(String[]) null,
				(Object[]) null
			)
		);
	}



	// ------------------------------------------------------------------------------- //
	// publish levels



	// title
	public void title(final String... lines) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.TITLE,
				lines,
				(Object[]) null
			)
		);
	}
	public void title(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.TITLE,
				lines,
				args
			)
		);
	}



	// trace exception
	public void trace(final Throwable e) {
		this.trace(e, null);
	}
	public void trace(final Throwable e, final String line, final Object... args) {
		final StringBuilder str = new StringBuilder();
		if (Utils.notEmpty(line)) {
			str.append(line)
				.append(" - ");
		}
		str.append(
			StringUtils.ExceptionToString(e)
		);
		this.publish(
			new xLogRecord(
				this,
				xLevel.SEVERE,
				new String[] { str.toString() },
				args
			)
		);
	}



	// stdout
	public void stdout(final String line, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.STDOUT,
				new String[] { line },
				args
			)
		);
	}
	public void stdout(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.STDOUT,
				lines,
				args
			)
		);
	}



	// stderr
	public void stderr(final String line, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.STDERR,
				new String[] { line },
				args
			)
		);
	}
	public void stderr(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.STDERR,
				lines,
				args
			)
		);
	}



	// detail
	public void detail(final String line, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.DETAIL,
				new String[] { line },
				args
			)
		);
	}
	public void detail(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.DETAIL,
				lines,
				args
			)
		);
	}



	// finest
	public void finest(final String line, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.FINEST,
				new String[] { line },
				args
			)
		);
	}
	public void finest(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.FINEST,
				lines,
				args
			)
		);
	}



	// finer
	public void finer(final String line, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.FINER,
				new String[] { line },
				args
			)
		);
	}
	public void finer(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.FINER,
				lines,
				args
			)
		);
	}



	// fine
	public void fine(final String line, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.FINE,
				new String[] { line },
				args
			)
		);
	}
	public void fine(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.FINE,
				lines,
				args
			)
		);
	}



	// stats
	public void stats(final String line, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.STATS,
				new String[] { line },
				args
			)
		);
	}
	public void stats(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.STATS,
				lines,
				args
			)
		);
	}



	// info
	public void info(final String line, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.INFO,
				new String[] { line },
				args
			)
		);
	}
	public void info(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.INFO,
				lines,
				args
			)
		);
	}



	// warning
	public void warning(final String line, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.WARNING,
				new String[] { line },
				args
			)
		);
	}
	public void warning(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.WARNING,
				lines,
				args
			)
		);
	}



	// severe
	public void severe(final String line, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.SEVERE,
				new String[] { line },
				args
			)
		);
	}
	public void severe(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.SEVERE,
				lines,
				args
			)
		);
	}



	// fatal
	public void fatal(final String line, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.FATAL,
				new String[] { line },
				args
			)
		);
	}
	public void fatal(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord(
				this,
				xLevel.FATAL,
				lines,
				args
			)
		);
	}



}
