package com.poixson.utils.xLogger;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poixson.utils.Keeper;
import com.poixson.utils.Utils;
import com.poixson.utils.xVars;
import com.poixson.utils.exceptions.RequiredArgumentException;

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



// ------------------------------------------------------------------------------- //

public class xLog extends xLogPrinting {

	// root logger
	protected static volatile xLog root = null;
	protected static final Object lock = new Object();
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
		if (root == null)
			init();
		return root;
	}
	public static xLog peekRoot() {
		return root;
	}
	// is root logger
	@Override
	public boolean isRoot() {
		return (this == root);
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
		synchronized(this.loggers) {
			if (this.loggers.containsKey(logName)) {
				return this.loggers.get(logName);
			}
			final xLog log = new xLog(logName, this);
			this.loggers.put(logName, log);
			return log;
		}
	}
	// new instance (weak reference)
	@Override
	public xLog getWeak(final String logName) {
		if (Utils.isEmpty(logName)) {
			return new xLog(logName, this.parent);
		}
		return new xLog(logName, this);
	}
	@Override
	public xLog getWeak() {
		return getWeak(null);
	}



	// default logger initializer
	public static void init() {
		if (root == null) {
			synchronized(lock) {
				if (root == null) {
					root = new xLog(null, null);
//TODO:
//					initDefaultHandlers();
				}
			}
		}
	}
//TODO:
//	// default log handlers
//	private static void initDefaultHandlers() {
//		// console handler
//		final xLogHandler handler = new logHandlerConsole();
//		handler.setFormatter(new xLogFormatter_Default());
//		root.addHandler(handler);
//	}



	// new logger instance
	protected xLog(final String logName, final xLog parentLogger) {
		if (Utils.isEmpty(logName) && parentLogger != null)
			throw new RequiredArgumentException("name");
		this.name   = logName;
		this.parent = parentLogger;
		// new root logger
		if (this.isRoot()) {
			if (this.level == null) {
				this.level = DEFAULT_LEVEL;
			}
			Keeper.add(this);
		}
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
		for (final xLogHandler handler : this.handlers) {
			handler.setLevel(lvl);
		}
	}
	@Override
	public xLevel getLevel() {
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
		for (final xLogHandler handler : this.handlers) {
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
			for (final xLogHandler handler : this.handlers) {
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
			publish("");
		}
		if (this.parent != null) {
			this.parent.publish(msg);
		}
		// publish to handlers
		for (final xLogHandler handler : this.handlers) {
			handler.publish(msg);
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
//			final xTickHandler tickHandler = xTickHandler.peak();
//			if (tickHandler != null) {
//				tickHandler.unregisterType(xTickPrompt.class);
//			}
		}
		// stop console input
		{
			final xConsole console = peekConsole();
			if (console != null) {
				console.Stop();
			}
		}
	}



}
