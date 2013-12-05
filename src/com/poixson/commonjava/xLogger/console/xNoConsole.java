package com.poixson.commonjava.xLogger.console;

import com.poixson.commonjava.xLogger.xConsole;


public class xNoConsole implements xConsole {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();

	public static void set() {
		xConsole.set(new xNoConsole());
	}


		if(xConsole.console != null) {
			xConsole.shutdown();
			xConsole.console = null;
		}
		xConsole.reader = null;
		xConsole.jlineEnabled = false;
		//xConsole.thread = null;
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
		System.out.println(
			renderAnsi(
				msg
			)
		);
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
