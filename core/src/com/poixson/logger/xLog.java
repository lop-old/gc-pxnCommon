package com.poixson.logger;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import com.poixson.app.xVars;
import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.formatters.xLogFormatter;
import com.poixson.logger.printers.xLogPrinter;
import com.poixson.logger.records.xLogRecord;
import com.poixson.logger.records.xLogRecord_Msg;
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
	protected volatile SoftReference<String[]> cachedNameTree = null;

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
			if (parent != null)          throw new IllegalArgumentException("Cannot set parent for root logger!");
			if (Utils.notEmpty(logName)) throw new IllegalArgumentException("Cannot set name for root logger!");
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
		if (xVars.isDebug())
			return xLevel.DETAIL;
		return this.level.get();
	}
	public xLevel peekLevel() {
		return this.level.get();
	}
	@Override
	public void setLevel(final xLevel level) {
		this.level.set(level);
	}



	// is level loggable
	public boolean isLoggable(final xLevel level) {
		if (level == null) return true;
		// forced debug mode
		if (xVars.isDebug()) return true;
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
	public boolean notLoggable(final xLevel level) {
		return ! this.isLoggable(level);
	}
	public boolean isDetailLoggable() {
		return this.isLoggable(xLevel.DETAIL);
	}



	// is root logger
	public boolean isRoot() {
		return false;
	}



	public String[] getNameTree() {
		if (this.isRoot())
			return new String[0];
		// cached name tree
		{
			final SoftReference<String[]> soft = this.cachedNameTree;
			if (soft != null) {
				final String[] list = soft.get();
				if (list != null)
					return list;
			}
		}
		// build name tree
		{
			final List<String> list = new ArrayList<String>();
			this.buildNameTree(list);
			final String[] result = list.toArray(new String[0]);
			this.cachedNameTree = new SoftReference<String[]>(result);
			return result;
		}
	}
	private void buildNameTree(final List<String> list) {
		if (this.isRoot())       return;
		if (this.parent == null) return;
		this.parent.buildNameTree(list);
		if (Utils.notEmpty(this.name))
			list.add(this.name);
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



	public int getPrinterCount() {
		return this.printers.size();
	}
	public boolean hasPrinter() {
		return ! this.printers.isEmpty();
	}



	// ------------------------------------------------------------------------------- //
	// publish



	@Override
	public void publish(final xLogRecord record) {
		if (record == null) {
			this.publish(
				(xLogRecord) null
			);
			return;
		}
		final xLevel level = record.getLevel();
		// not loggable
		if ( this.notLoggable(level) )
			return;
		// pass to printers/handlers
		boolean handled = false;
		{
			final xLogPrinter[] printers = this.getPrinters();
			if (Utils.notEmpty(printers)) {
				PRINTER_LOOP:
				for (final xLogPrinter printer : printers) {
					if (printer == null) continue PRINTER_LOOP;
					try {
						if ( printer.notLoggable(level) )
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
			new xLogRecord_Msg(
				this,
				level,
				StringUtils.StringToArray(line),
				args
			)
		);
	}
	@Override
	public void publish(final String[] lines) {
		this.publish(
			new xLogRecord_Msg(
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
			new xLogRecord_Msg(
				this,
				(xLevel)   null,
				StringUtils.StringToArray(line),
				(Object[]) null
			)
		);
	}
	@Override
	public void publish() {
		this.publish(
			new xLogRecord_Msg(
				this,
				(xLevel)   null,
				(String[]) null,
				(Object[]) null
			)
		);
	}



	@Override
	public void flush() {
		final xLogPrinter[] printers = this.getPrinters();
		if (Utils.notEmpty(printers)) {
			for (final xLogPrinter printer : printers) {
				try {
					printer.flush();
				} catch (Exception ignore) {}
			}
		}
	}



	// ------------------------------------------------------------------------------- //
	// publish levels



	// title
	public void title(final String... lines) {
		this.publish(
			new xLogRecord_Msg(
				this,
				xLevel.TITLE,
				lines,
				(Object[]) null
			)
		);
	}
	public void title(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord_Msg(
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
			new xLogRecord_Msg(
				this,
				xLevel.SEVERE,
				StringUtils.StringToArray(str.toString()),
				args
			)
		);
	}



	// stdout
	public void stdout(final String line, final Object... args) {
		this.publish(
			new xLogRecord_Msg(
				this,
				xLevel.STDOUT,
				StringUtils.StringToArray(line),
				args
			)
		);
	}
	public void stdout(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord_Msg(
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
			new xLogRecord_Msg(
				this,
				xLevel.STDERR,
				StringUtils.StringToArray(line),
				args
			)
		);
	}
	public void stderr(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord_Msg(
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
			new xLogRecord_Msg(
				this,
				xLevel.DETAIL,
				StringUtils.StringToArray(line),
				args
			)
		);
	}
	public void detail(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord_Msg(
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
			new xLogRecord_Msg(
				this,
				xLevel.FINEST,
				StringUtils.StringToArray(line),
				args
			)
		);
	}
	public void finest(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord_Msg(
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
			new xLogRecord_Msg(
				this,
				xLevel.FINER,
				StringUtils.StringToArray(line),
				args
			)
		);
	}
	public void finer(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord_Msg(
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
			new xLogRecord_Msg(
				this,
				xLevel.FINE,
				StringUtils.StringToArray(line),
				args
			)
		);
	}
	public void fine(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord_Msg(
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
			new xLogRecord_Msg(
				this,
				xLevel.STATS,
				StringUtils.StringToArray(line),
				args
			)
		);
	}
	public void stats(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord_Msg(
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
			new xLogRecord_Msg(
				this,
				xLevel.INFO,
				StringUtils.StringToArray(line),
				args
			)
		);
	}
	public void info(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord_Msg(
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
			new xLogRecord_Msg(
				this,
				xLevel.WARNING,
				StringUtils.StringToArray(line),
				args
			)
		);
	}
	public void warning(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord_Msg(
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
			new xLogRecord_Msg(
				this,
				xLevel.SEVERE,
				StringUtils.StringToArray(line),
				args
			)
		);
	}
	public void severe(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord_Msg(
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
			new xLogRecord_Msg(
				this,
				xLevel.FATAL,
				StringUtils.StringToArray(line),
				args
			)
		);
	}
	public void fatal(final String[] lines, final Object... args) {
		this.publish(
			new xLogRecord_Msg(
				this,
				xLevel.FATAL,
				lines,
				args
			)
		);
	}



}
