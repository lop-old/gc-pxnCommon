package com.poixson.commonjava.xLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.app.xApp;
import com.poixson.commonjava.xLogger.console.xNoConsole;
import com.poixson.commonjava.xLogger.formatters.defaultLogFormatter;
import com.poixson.commonjava.xLogger.handlers.logHandlerConsole;


public class xLog extends xLogPrinting {


// minimal
/*
	// logger
	public static xLog log() {
		return xLog.log();
	}
*/

// local caching
/*
	// logger
	private volatile xLog _log = null;
	private final Object logLock = new Object();
	public xLog log() {
		if(_log == null) {
			synchronized(logLock) {
				if(_log == null)
					_log = xLog.log();
			}
		}
		return _log;
	}
	public void setLog(xLog log) {
		synchronized(logLock) {
			_log = log;
		}
	}
*/

// full with local caching
/*
	// logger
	private volatile xLog _log = null;
	private final Object logLock = new Object();
	public xLog log() {
		if(_log == null) {
			synchronized(logLock) {
				if(_log == null)
					_log = xLog.log();
			}
		}
		return _log;
	}
	public xLog log(String name) {
		return log().get(name);
	}
	public void setLog(xLog log) {
		synchronized(logLock) {
			_log = log;
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
	public xLog get(final String name) {
		if(utils.isEmpty(name))
			return this;
		{
			final xLog log = loggers.get(name);
			if(log != null)
				return log;
		}
		// new logger
		synchronized(loggers) {
			if(loggers.containsKey(name))
				return loggers.get(name);
			final xLog log =new xLog(name, this);
			loggers.put(name, log);
			return log;
		}
	}
	// new anonymous instance
	@Override
	public xLog getAnon(final String name) {
		if(utils.isEmpty(name))
			return new xLog(name, parent);
		return new xLog(name, this);
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
	protected xLog(final String name, final xLog parent) {
		if(utils.isEmpty(name) && parent != null)
			throw new NullPointerException("name cannot be null");
		this.name = name;
		this.parent = parent;
	}


	// is root logger
	@Override
	public boolean isRoot() {
		return (parent == null);
	}


	// log level
	public void setLevel(final xLevel lvl) {
		this.level = lvl;
		// handlers
		for(final xLogHandler handler : handlers)
			handler.setLevel(lvl);
	}
	// is level loggable
	public boolean isLoggable(final xLevel lvl) {
		// forced debug mode
		if(xApp.debug())
			return true;
		// local logger level
		if(level != null && !level.isLoggable(lvl))
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
		for(final xLogHandler handler : handlers)
			if(handler.getClass().equals(type))
				handler.setFormatter(formatter);
	}


	// [logger] [crumbs]
	// recursive name tree
	private void buildNameTree(final List<String> list) {
		if(parent != null) {
			parent.buildNameTree(list);
			list.add(name);
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
	// publish record
	@Override
	public void publish(final xLogRecord record) {
		final xLevel lvl = record.level();
		if(!isLoggable(lvl))
			return;
		if(parent != null)
			parent.publish(record);
		if(!handlers.isEmpty()) {
			for(final xLogHandler handler : this.handlers) {
				if(handler.isLoggable(lvl))
					handler.publish(record);
			}
		}
	}
	@Override
	public void publish(final String msg) {
		if(parent != null)
			parent.publish(msg);
		if(!handlers.isEmpty()) {
			for(final xLogHandler handler : this.handlers)
				handler.publish(msg);
		}
	}


	// ### console handler


	private static volatile xConsole consoleHandler = null;
	private static final Object consoleLock = new Object();


	public static void setConsole(final xConsole console) {
		synchronized(consoleLock) {
			xLog.consoleHandler = console;
		}
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
			peekConsole().shutdown();
	}


}
