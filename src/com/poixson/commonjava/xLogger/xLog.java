package com.poixson.commonjava.xLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.xLogger.console.xNoConsole;
import com.poixson.commonjava.xLogger.formatters.defaultLogFormatter;
import com.poixson.commonjava.xLogger.handlers.logHandlerConsole;


public class xLog extends xLogPrinting {


// minimal
/*
	// logger
	public static xLog log() {
		return xVars.log();
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
					_log = xVars.log();
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
					_log = xVars.log();
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
		if(root != null) return;
		synchronized(lock) {
			if(root != null) return;
			root = new xLog(null, null);
			initDefaultHandlers();
		}
	}
	// default log handlers
	protected static void initDefaultHandlers() {
		// console
		xLogHandler console = new logHandlerConsole();
		console.setFormatter(
			new defaultLogFormatter()
		);
		root.addHandler(console);
	}


	// get root logger
	public static xLog getRoot() {
		if(root == null)
			init();
		return root;
	}
	// get named logger
	public static xLog getLog(String name) {
		return getRoot().get(name);
	}
	// get sub-logger
	@Override
	public xLog get(String name) {
		if(name == null || name.isEmpty())
			return this;
		if(loggers.containsKey(name))
			return loggers.get(name);
		// new logger
		synchronized(loggers) {
			if(loggers.containsKey(name))
				return loggers.get(name);
			xLog log = this.newInstance(name);
			loggers.put(name, log);
			return log;
		}
	}
	@Override
	public xLog getAnon(String name) {
		if(name == null || name.isEmpty())
			return this;
		return new xLog(name, this);
	}
	// new logger instance
	protected xLog(String name, xLog parent) {
		if( (name == null || name.isEmpty()) && parent != null)
			throw new NullPointerException("name cannot be null");
		this.name = name;
		this.parent = parent;
	}
	@Override
	protected xLog newInstance(String name) {
		return new xLog(name, this);
	}
	// no clone
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}


	// is root logger
	@Override
	public boolean isRoot() {
		return (parent == null);
	}


	// log level
	public void setLevel(xLevel lvl) {
		this.level = lvl;
		// handlers
		for(xLogHandler handler : handlers)
			handler.setLevel(lvl);
	}
	// is level loggable
	public boolean isLoggable(xLevel lvl) {
		// forced debug mode
		if(xVars.debug())
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


	// [logger] [crumbs]
	// recursive name tree
	private void buildNameTree(List<String> list) {
		if(parent != null) {
			parent.buildNameTree(list);
			list.add(name);
		}
	}
	@Override
	public List<String> getNameTree() {
		return getNameTree(this);
	}
	public static List<String> getNameTree(xLog log) {
		List<String> list = new ArrayList<String>();
		log.buildNameTree(list);
		return list;
	}


	// log handlers
	@Override
	public void addHandler(xLogHandler handler) {
		this.handlers.add(handler);
	}
	// publish record
	@Override
	public void publish(xLogRecord record) {
		xLevel lvl = record.getLevel();
		if(!isLoggable(lvl))
			return;
		if(parent != null)
			parent.publish(record);
		if(!handlers.isEmpty()) {
			for(xLogHandler handler : this.handlers) {
				if(handler.isLoggable(lvl))
					handler.publish(record);
			}
		}
	}


	// ### console handler


	private static volatile xConsole _console = null;
	private static final Object consoleLock = new Object();


	public static void setConsole(xConsole console) {
		synchronized(consoleLock) {
			xLog._console = console;
		}
	}
	public static xConsole getConsole() {
		if(_console == null) {
			synchronized(consoleLock) {
				if(_console == null)
					_console = new xNoConsole();
			}
		}
		return _console;
	}
	public static xConsole peekConsole() {
		return _console;
	}
	public static void shutdown() {
		if(peekConsole() != null)
			peekConsole().shutdown();
	}


}
