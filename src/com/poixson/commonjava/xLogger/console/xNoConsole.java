package com.poixson.commonjava.xLogger.console;

import com.poixson.commonjava.xLogger.xConsole;
import com.poixson.commonjava.xLogger.xLog;


public class xNoConsole implements xConsole {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

//	private static final Object lock = new Object();
	private static final Object printLock = new Object();
//	private static volatile xConsole console = null;


	public xNoConsole() {
	}


	@Override
	public void start() {
		log().finest("Start xNoConsole");
	}
	@Override
	public void stop() {
	}


	@Override
	public void run() {
	}


	@Override
	public void clear() {
	}
	@Override
	public void flush() {
	}
	@Override
	public void print(final String msg) {
		synchronized(printLock) {
			System.out.println(msg);
			System.out.flush();
		}
	}
	@Override
	public void drawPrompt() {
	}


	@Override
	public void setPrompt(final String prompt) {
	}
	@Override
	public String getPrompt() {
		return null;
	}


	// logger
	public static xLog log() {
		return xLog.getRoot();
	}


}
