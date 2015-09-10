package com.poixson.commonjava.xLogger;

import com.poixson.commonjava.xLogger.commands.xCommandsHandler;


public class xNoConsole implements xConsole {

	private static final Object printLock = new Object();



	public xNoConsole() {
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}



	@Override
	public void Start() {
		log().finest("Start xNoConsole");
	}
	@Override
	public void Stop() {
	}
	@Override
	public boolean isRunning() {
		return false;
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
		String str = msg;
		// strip color tags
		while(true) {
			final int posA = str.indexOf("@|");
			if(posA == -1) break;
			final int posB = str.indexOf(' ', posA);
			final int posC = str.indexOf("|@", posB);
			if(posB == -1) break;
			if(posC == -1) break;
			// strip out color tags
			final StringBuilder tmp = new StringBuilder();
			tmp.append(str.substring(0, posA));
			tmp.append(str.substring(posB+1, posC));
			tmp.append(str.substring(posC+2));
			str = tmp.toString();
		}
		synchronized(printLock) {
			System.out.println(str);
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
	public void setCommandHandler(final xCommandsHandler handler) {
	}



	// logger
	public static xLog log() {
		return xLog.getRoot();
	}



}
