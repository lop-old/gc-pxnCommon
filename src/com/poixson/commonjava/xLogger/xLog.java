package com.poixson.commonjava.xLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.scheduler.ticker.xTickHandler;
import com.poixson.commonjava.scheduler.ticker.xTickPrompt;
import com.poixson.commonjava.xLogger.commands.xCommandsHandler;
import com.poixson.commonjava.xLogger.formatters.defaultLogFormatter;


public class xLog extends xLogPrinting {



// minimal
/*
	// logger
	public static xLog log() {
		return xLog.getRoot();
	}
*/



// local caching
/*
	// logger
	private static volatile xLog _log = null;
	public static xLog log() {
		if(_log == null)
			_log = xLog.log();
		return _log;
	}
*/



// overridable
/*
	// logger
	private volatile xLog _log = null;
	private volatile xLog _log_default = null;
	public xLog log() {
		if(this._log != null)
			return this._log;
		if(this._log_default == null)
			this._log_default = xLog.getRoot();
		return this._log_default;
	}
	public void setLog(final xLog log) {
		this._log = log;
	}
*/



	// ------------------------------------------------------------------------------- //



	// root logger
	protected static volatile xLog root = null;
	protected static final Object lock = new Object();
	public static final xLevel DEFAULT_LEVEL = xLevel.ALL;

	private final String name;
	private final xLog parent;
	private volatile xLevel level = null;
	private static volatile xCommandsHandler commandHandler = null;

	// sub-loggers
	private final ConcurrentMap<String, xLog> loggers = new ConcurrentHashMap<String, xLog>();
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
	private static void initDefaultHandlers() {
		// console handler
		final xLogHandler handler = new logHandlerConsole();
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
			final xLog log = new xLog(logName, this);
			this.loggers.put(logName, log);
			return log;
		}
	}
	// new instance (weak reference)
	@Override
	public xLog getWeak(final String logName) {
		if(utils.isEmpty(logName))
			return new xLog(logName, this.parent);
		return new xLog(logName, this);
	}
	@Override
	public xLog getWeak() {
		return getWeak(null);
	}
	@Override
	public xLog clone() {
		return getWeak();
	}



	// new logger instance
	protected xLog(final String logName, final xLog parentLogger) {
		if(utils.isEmpty(logName) && parentLogger != null)
			throw new NullPointerException("name argument is required!");
		this.name = logName;
		this.parent = parentLogger;
		// new root logger
		if(this.isRoot()) {
			if(this.level == null)
				this.level = DEFAULT_LEVEL;
			Keeper.add(this);
		}
	}



	// is root logger
	@Override
	public boolean isRoot() {
		return (this.name == null && this.parent == null);
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
		if(lvl == null || this.level == null)
			return true;
		// forced debug mode
		if(xVars.debug())
			return true;
		// local logger level
		if(this.level.isLoggable(lvl))
			return true;
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
	}



	// formatter
	public void setFormatter(final xLogFormatter formatter, final Class<?> type) {
		if(formatter == null) throw new NullPointerException("formatter argument is required!");
		for(final xLogHandler handler : this.handlers)
			if(type == null || type.equals(handler.getClass()))
				handler.setFormatter(formatter);
	}
	public void setFormatter(final xLogFormatter formatter) {
		this.setFormatter(formatter, null);
	}



	// [logger] [crumbs]
	// recursive name tree
	private void buildNameTree(final List<String> list) {
		if(this.parent != null) {
			this.parent.buildNameTree(list);
			if(utils.notEmpty(this.name))
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
	@Override
	public void setHandler(final xLogHandler handler) {
		this.handlers.clear();
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



	// ------------------------------------------------------------------------------- //
	// console handler



	private static volatile xConsole consoleHandler = null;

	public static void setConsole(final xConsole console) {
		consoleHandler = console;
		if(consoleHandler != null && commandHandler != null)
			consoleHandler.setCommandHandler(commandHandler);
	}
	public static xConsole getConsole() {
		if(consoleHandler == null)
			consoleHandler = new xNoConsole();
		return consoleHandler;
	}
	public static xConsole peekConsole() {
		return consoleHandler;
	}
	public static void shutdown() {
		// stop prompt ticker
		xTickHandler.get()
			.unregister(xTickPrompt.class);
		// stop console input
		final xConsole console = peekConsole();
		if(console != null)
			console.Stop();
	}



	// set command handler
	public static void setCommandHandler(final xCommandsHandler commandsHandler) {
		if(commandsHandler == null) throw new NullPointerException("commandsHandler argument is required!");
		if(consoleHandler  == null) throw new RuntimeException("consoleHandler not set; command handler not supported.");
		commandHandler = commandsHandler;
		consoleHandler.setCommandHandler(commandsHandler);
	}



}
