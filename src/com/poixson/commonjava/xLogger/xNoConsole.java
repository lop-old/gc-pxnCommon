package com.poixson.commonjava.xLogger;

import com.poixson.commonjava.EventListener.xHandler;


public class xNoConsole implements xConsole {

//	private static final Object lock = new Object();
	private static final Object printLock = new Object();
//	private static volatile xConsole console = null;



	public xNoConsole() {
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
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



	@Override
	public void setCommandHandler(final xHandler handler) {
		if(handler != null) throw new UnsupportedOperationException();
	}



	// logger
	public static xLog log() {
		return xLog.getRoot();
	}


}
