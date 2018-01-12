package com.poixson.logger;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import com.poixson.app.xVars;
import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.tools.Keeper;
import com.poixson.utils.FileUtils;
import com.poixson.utils.Utils;


// ------------------------------------------------------------------------------- //



/* minimal

	// logger
	public static xLog log() {
		return xLog.getRoot();
	}
*/



/* soft-cache

	// logger
	private volatile SoftReference<xLog> _log = null;
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null)
				return log;
		}
		final xLog log = xLog.getRoot();
		this._log = new SoftReference<xLog>(log);
		return log;
	}
*/



/* named soft-cache

	// logger
	private volatile SoftReference<xLog> _log = null;
	private volatile String _className = null;
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null)
				return log;
		}
		if (this._className == null) {
			this._className =
				ReflectUtils.getClassName(
					this.getClass()
				);
		}
		final xLog log =
			xLog.getRoot()
				.get(this._className);
		this._log = new SoftReference<xLog>(log);
		return log;
	}
*/



/* cached detail logging

	// cached log level
	private volatile SoftReference<Boolean> _detail = null;
	public boolean isDetailedLogging() {
		if (this._detail != null) {
			final Boolean detail = this._detail.get();
			if (detail != null)
				return detail.booleanValue();
		}
		final boolean detail =
			this.log()
				.isLoggable(xLevel.DETAIL);
		this._detail = new SoftReference<Boolean>(Boolean.valueOf(detail));
		return detail;
	}
*/



/* settable logger

	// logger
	private volatile xLog _log_override = null;
	public xLog log() {
		// use override logger
		{
			final xLog log = this._log_override;
			if (log != null)
				return log;
		}
		// use default logger
		return this.logSoft();
	}
	public pxnSerial setLog(final xLog log) {
		this._log_override = log;
		return this;
	}

	private volatile SoftReference<xLog> _log_soft = null;
	private volatile String _className = null;
	private xLog logSoft() {
		if (this._log_soft != null) {
			final xLog log = this._log_soft.get();
			if (log != null)
				return log;
		}
		if (this._className == null) {
			this._className =
				ReflectUtils.getClassName(
					this.getClass()
				);
		}
		final xLog log =
			xLog.getRoot()
				.get(this._className);
		this._log_soft = new SoftReference<xLog>(log);
		return log;
	}
*/



// ------------------------------------------------------------------------------- //

public class xLog extends xLogPrinting {

	// root logger
	protected static final AtomicReference<xLog> root = new AtomicReference<xLog>(null);
	public static final xLevel DEFAULT_LEVEL = xLevel.FINEST;

	// logger instance
	protected final String name;
	protected final xLog parent;
	protected volatile xLevel level = null;

	// sub-loggers
	private final ConcurrentMap<String, xLog> loggers = new ConcurrentHashMap<String, xLog>();
	// handlers
	private final List<xLogHandler> handlers = new CopyOnWriteArrayList<xLogHandler>();



	// get root logger
	public static xLog getRoot() {
		if (root.get() != null)
			return root.get();
		final xLog log = new xLog(null, null);
		log.setLevel(DEFAULT_LEVEL);
//TODO:
//		initDefaultHandlers();
//		// default log handlers
//		private static void initDefaultHandlers() {
//			// console handler
//			final xLogHandler handler = new logHandlerConsole();
//			handler.setFormatter(new xLogFormatter_Default());
//			root.addHandler(handler);
//		}
		if (!root.compareAndSet(null, log)) {
			return root.get();
		}
		if (FileUtils.SearchLocalFile(xVars.SEARCH_DEBUG_FILES)) {
			xVars.debug(true);
		}
		Keeper.add(log);
		return log;
	}
	public static xLog peekRoot() {
		return root.get();
	}
	// is root logger
	@Override
	public boolean isRoot() {
		final xLog log = root.get();
		if (log == null)
			return false;
		return this.equals(log);
	}



	// get logger
	@Override
	public xLog get(final String logName) {
		if (Utils.isEmpty(logName)) {
			return this;
		}
		// existing logger instance
		{
			final xLog log = this.loggers.get(logName);
			if (log != null) {
				return log;
			}
		}
		// new logger instance
		{
			final xLog log = new xLog(logName, this);
			final xLog existing =
				this.loggers.putIfAbsent(
					logName,
					log
				);
			return (
				existing == null
				? log
				: existing
			);
		}
	}
	// new instance (weak reference)
	@Override
	public xLog getWeak(final String logName) {
		if (Utils.isEmpty(logName))
			throw new RequiredArgumentException("logName");
		return new xLog(logName, this);
	}
	@Override
	public xLog getWeak() {
		return getWeak(null);
	}



	// new logger instance
	protected xLog(final String logName, final xLog parentLogger) {
		if (parentLogger != null) {
			if (Utils.isEmpty(logName)) throw new RequiredArgumentException("name");
		}
		this.name   = logName;
		this.parent = parentLogger;
	}
	@Override
	public xLog clone() {
		return getWeak();
	}



	public static PrintStream getOriginalOut() {
		return xVars.getOriginalOut();
	}
	public static PrintStream getOriginalErr() {
		return xVars.getOriginalErr();
	}
	public static InputStream getOriginalIn() {
		return xVars.getOriginalIn();
	}



	public static xCommandHandler getCommandHandler() {
		return
			getConsole()
				.getCommandHandler();
	}



	// log level
	@Override
	public void setLevel(final xLevel lvl) {
		this.level = lvl;
		// handlers
		final Iterator<xLogHandler> it = this.handlers.iterator();
		while (it.hasNext()) {
			it.next()
				.setLevel(lvl);
		}
	}
	@Override
	public xLevel getLevel() {
		return this.level;
	}
	@Override
	public xLevel peekLevel() {
		return this.level;
	}
	// is level loggable
	@Override
	public boolean isLoggable(final xLevel lvl) {
		if (lvl == null)
			return true;
		// forced debug mode
		if (xVars.debug()) {
			return true;
		}
		// local level
		final xLevel thisLevel;
		{
			final xLevel thisLvl = this.level;
			thisLevel = (
				thisLvl == null
				? DEFAULT_LEVEL
				: thisLvl
			);
		}
		if (thisLevel.isLoggable(lvl))
			return true;
		return false;
	}



	// formatter
	public void setFormatter(final xLogFormatter formatter, final Class<?> handlerType) {
		if (formatter == null) throw new RequiredArgumentException("formatter");
		final Iterator<xLogHandler> it = this.handlers.iterator();
		while (it.hasNext()) {
			final xLogHandler handler = it.next();
			if (handlerType == null) {
				handler.setFormatter(formatter);
			} else
			if (handlerType.equals(handler.getClass())) {
				handler.setFormatter(formatter);
			}
		}
	}
	public void setFormatter(final xLogFormatter formatter) {
		this.setFormatter(formatter, null);
	}



	// log handlers
	@Override
	public void addHandler(final xLogHandler handler) {
		this.handlers.add(handler);
	}
	@Override
	public void setHandler(final xLogHandler handler) {
		this.handlers.clear();
		this.handlers.add(handler);
	}



	// [logger] [crumbs]
	// recursive name tree
	private void buildNameTree(final List<String> list) {
		if (this.parent != null) {
			this.parent.buildNameTree(list);
			if (Utils.notEmpty(this.name)) {
				list.add(this.name);
			}
		}
	}
	@Override
	public List<String> getNameTree() {
		final List<String> list = new ArrayList<String>();
		this.buildNameTree(list);
		return list;
//		return getNameTree(this);
	}
//TODO:
//	public static List<String> getNameTree(final xLog log) {
//		final List<String> list = new ArrayList<String>();
//		log.buildNameTree(list);
//		return list;
//	}
//	// default level at root
//	if (parent == null && level == null) {
//		return DEFAULT_LEVEL.isLoggable(lvl);
//	}



	// publish record to handlers
	@Override
	public void publish(final xLogRecord record) {
		final xLevel lvl = record.level();
		if (xLevel.TITLE.equals(lvl)) {
			this.title(record.msg());
			return;
		}
		if (!this.isLoggable(lvl)) return;
		if (this.parent != null) {
			this.parent.publish(record);
		}
		if (this.handlers.isEmpty()) {
			if (this.isRoot()) {
				getOriginalOut()
					.println(
						record.msg()
					);
			}
		} else {
			final Iterator<xLogHandler> it = this.handlers.iterator();
			while (it.hasNext()) {
				final xLogHandler handler = it.next();
				if (handler.isLoggable(lvl)) {
					handler.publish(record);
				}
			}
		}
	}
	// publish string to handlers
	@Override
	public void publish(final String msg) {
		if (msg == null) {
			this.publish("");
			return;
		}
		// give msg to parent
		if (this.parent != null) {
			this.parent.publish(msg);
		}
		// publish to handlers
		if (!this.handlers.isEmpty()) {
			final Iterator<xLogHandler> it = this.handlers.iterator();
			while (it.hasNext()) {
				it.next()
					.publish(msg);
			}
		}
	}



	// ------------------------------------------------------------------------------- //
	// console handler



	private static volatile xConsole consoleHandler = null;

	public static void setConsole(final xConsole console) {
		consoleHandler = console;
	}
	public static xConsole getConsole() {
		if (consoleHandler == null) {
			consoleHandler = new xNoConsole();
		}
		return consoleHandler;
	}
	public static xConsole peekConsole() {
		return consoleHandler;
	}



	public static void Shutdown() {
		// stop prompt ticker
		{
//TODO:
//			final xTickHandler tickHandler = xTickHandler.peek();
//			if (tickHandler != null) {
//				tickHandler.unregisterType(xTickPrompt.class);
//			}
		}
		// stop console input
		{
			final xConsole console = peekConsole();
			if (console != null) {
				console.stop();
			}
		}
	}



}
