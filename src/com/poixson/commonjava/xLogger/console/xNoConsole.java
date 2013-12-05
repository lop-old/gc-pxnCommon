package com.poixson.commonjava.xLogger.console;

import com.poixson.commonjava.xLogger.xConsole;


public class xNoConsole implements xConsole {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	protected static final Object lock = new Object();
	protected static volatile xConsole console = null;


	public xNoConsole() {
	}


	@Override
	public void start() {
	}
	@Override
	public void stop() {
	}
	@Override
	public void shutdown() {
	}


	@Override
	public void run() {
	}
	@Override
	public void doCommand(String line) {
	}


	@Override
	public void clear() {
	}
	@Override
	public void flush() {
	}
	@Override
	public void print(String msg) {
		System.out.println(msg);
	}
	@Override
	public void redraw() {
	}


	@Override
	public void setPrompt(String prompt) {
	}
	@Override
	public String getPrompt() {
		return null;
	}


}
