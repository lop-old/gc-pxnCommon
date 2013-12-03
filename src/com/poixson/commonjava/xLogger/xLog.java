package com.poixson.commonjava.xLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poixson.commonjava.xLogger.formatters.defaultLogFormatter;
import com.poixson.commonjava.xLogger.handlers.logHandlerConsole;


public class xLog extends xLogPrinting {

	// root logger
	protected static volatile xLog root = null;
	protected static final Object lock = new Object();

	private final String name;
	private final xLog parent;
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
		if(parent != null)
			parent.publish(record);
		if(!handlers.isEmpty())
			for(xLogHandler handler : this.handlers)
				handler.publish(record);
	}


}
