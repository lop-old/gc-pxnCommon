package com.poixson.commonjava.xLogger;


public class xNoConsole extends xConsole {


	public static void set() {
		xConsole.set(new xNoConsole());
	}


	protected xNoConsole() {
		if(xConsole.console != null) {
			xConsole.shutdown();
			xConsole.console = null;
		}
		xConsole.reader = null;
		xConsole.jlineEnabled = false;
		//xConsole.thread = null;
	}


	@Override
	public void start() {
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


}
