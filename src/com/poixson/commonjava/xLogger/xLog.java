package com.poixson.commonjava.xLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.xLogger.console.jlineConsole;
import com.poixson.commonjava.xLogger.console.xNoConsole;
import com.poixson.commonjava.xLogger.formatters.defaultLogFormatter;
import com.poixson.commonjava.xLogger.handlers.CommandHandler;
import com.poixson.commonjava.xLogger.handlers.logHandlerConsole;


public class xLog extends xLogPrinting {



// minimal
/*
	// logger
	public static xLog log() {
		return xLog.getRoot();
	}
== or ==
	// logger
	public static xLog log() {
		return xApp.log();
	}
*/



// local caching
/*
	// logger
	private volatile xLog _log = null;
	private final Object logLock = new Object();
	public xLog log() {
		if(this._log == null) {
			synchronized(this.logLock) {
				if(this._log == null)
					this._log = xLog.getRoot();
			}
		}
		return this._log;
	}
	public void setLog(final xLog log) {
		synchronized(this.logLock) {
			this._log = log;
		}
	}
*/



// full with local caching
/*
	// logger
	private volatile xLog _log = null;
	private final Object logLock = new Object();
	public xLog log() {
		if(this._log == null) {
			synchronized(this.logLock) {
				if(this._log == null)
					this._log = xLog.getRoot();
			}
		}
		return this._log;
	}
	public xLog log(final String name) {
		return log().get(name);
	}
	public void setLog(final xLog log) {
		synchronized(this.logLock) {
			this._log = log;
		}
	}
*/



	// root logger
	protected static volatile xLog root = null;
	protected static final Object lock = new Object();
	public static final xLevel DEFAULT_LEVEL = xLevel.INFO;

	private final String name;
	private final xLog parent;
	private volatile xLevel level = null;
	// sub-loggers
	private final Map<String, xLog> loggers = new ConcurrentHashMap<String, xLog>();
	// handlers
	private final List<xLogHandler> handlers = new CopyOnWriteArrayList<xLogHandler>();



	// default logger initializer
	public static void init() {
		if(root == null) {
			synchronized(lock) {
				if(root == null) {
					root = new xLog(null, null);
					initDefaultHandlers();
				}
			}
		}
	}
	// default log handlers
	protected static void initDefaultHandlers() {
		// console handler
		xLogHandler handler = new logHandlerConsole();
		handler.setFormatter(new defaultLogFormatter());
		root.addHandler(handler);
	}



	// get root logger
	public static xLog getRoot() {
		if(root == null)
			init();
		return root;
	}
	// get named logger
	public static xLog getRoot(final String name) {
		return getRoot().get(name);
	}
	// get sub-logger
	@Override
	public xLog get(final String logName) {
		if(utils.isEmpty(logName))
			return this;
		{
			final xLog log = this.loggers.get(logName);
			if(log != null)
				return log;
		}
		// new logger
		synchronized(this.loggers) {
			if(this.loggers.containsKey(logName))
				return this.loggers.get(logName);
			final xLog log =new xLog(logName, this);
			this.loggers.put(logName, log);
			return log;
		}
	}
	// new anonymous instance
	@Override
	public xLog getAnon(final String logName) {
		if(utils.isEmpty(logName))
			return new xLog(logName, this.parent);
		return new xLog(logName, this);
	}
	@Override
	public xLog getAnon() {
		return getAnon(null);
	}
	@Override
	public xLog clone() {
		return getAnon();
	}



	// new logger instance
	protected xLog(final String logName, final xLog parentLogger) {
		if(utils.isEmpty(logName) && parentLogger != null)
			throw new NullPointerException("name cannot be null");
		this.name = logName;
		this.parent = parentLogger;
		// root logger
		if(parentLogger == null) {
			Keeper.add(this);
		}
	}



	// is root logger
	@Override
	public boolean isRoot() {
		return (this.parent == null);
	}



	// log level
	public void setLevel(final xLevel lvl) {
		this.level = lvl;
		// handlers
		for(final xLogHandler handler : this.handlers)
			handler.setLevel(lvl);
	}
	public xLevel getLevel() {
		return this.level;
	}
	// is level loggable
	public boolean isLoggable(final xLevel lvl) {
		// forced debug mode
		if(xVars.get().debug())
			return true;
		// local logger level
		if(this.level != null && !this.level.isLoggable(lvl))
			return false;
//		// handlers
//		for(xLogHandler handler : handlers)
//			if(handler.isLoggable(lvl))
//				return true;
//		// parents
//		if(parent != null)
//			if(parent.isLoggable(lvl))
//				return true;
//		// default level at root
//		if(parent == null && level == null)
//			return DEFAULT_LEVEL.isLoggable(lvl);
		// default to all
		return true;
	}



	// formatter
	public void setFormatter(final xLogFormatter formatter, final Class<?> type) {
		if(formatter == null) throw new NullPointerException("formatter cannot be null");
		if(type      == null) throw new NullPointerException("handler type cannot be null");
		for(final xLogHandler handler : this.handlers)
			if(handler.getClass().equals(type))
				handler.setFormatter(formatter);
	}



	// [logger] [crumbs]
	// recursive name tree
	private void buildNameTree(final List<String> list) {
		if(this.parent != null) {
			this.parent.buildNameTree(list);
			list.add(this.name);
		}
	}
	@Override
	public List<String> getNameTree() {
		return getNameTree(this);
	}
	public static List<String> getNameTree(final xLog log) {
		final List<String> list = new ArrayList<String>();
		log.buildNameTree(list);
		return list;
	}



	// log handlers
	@Override
	public void addHandler(final xLogHandler handler) {
		this.handlers.add(handler);
	}
	// publish record to handlers
	@Override
	public void publish(final xLogRecord record) {
		final xLevel lvl = record.level();
		if(!isLoggable(lvl))
			return;
		if(this.parent != null)
			this.parent.publish(record);
		if(!this.handlers.isEmpty()) {
			for(final xLogHandler handler : this.handlers) {
				if(handler.isLoggable(lvl))
					handler.publish(record);
			}
		}
	}
	// publish string to handlers
	@Override
	public void publish(final String msg) {
		if(msg == null) publish("");
		if(this.parent != null)
			this.parent.publish(msg);
		if(!this.handlers.isEmpty()) {
			for(final xLogHandler handler : this.handlers)
				handler.publish(msg);
		}
	}



	// ### console handler



	private static volatile xConsole consoleHandler = null;
	private static volatile CommandHandler commandHandler = null;
	private static final Object consoleLock = new Object();


	public static void setConsole(final xConsole console) {
		synchronized(consoleLock) {
			xLog.consoleHandler = console;
		}
		if(commandHandler != null)
			setCommandHandler(commandHandler);
	}
	public static xConsole getConsole() {
		if(consoleHandler == null) {
			synchronized(consoleLock) {
				if(consoleHandler == null)
					consoleHandler = new xNoConsole();
			}
		}
		return consoleHandler;
	}
	public static xConsole peekConsole() {
		return consoleHandler;
	}
	public static void shutdown() {
		if(peekConsole() != null)
			peekConsole().stop();
	}
	public static void setCommandHandler(final CommandHandler handler) {
		synchronized(consoleLock) {
			if(consoleHandler == null) return;
			commandHandler = handler;
			if(!(consoleHandler instanceof jlineConsole)) return;
			((jlineConsole) consoleHandler).setCommandHandler(handler);
		}
	}



}
