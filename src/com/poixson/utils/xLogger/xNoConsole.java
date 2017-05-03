package com.poixson.utils.xLogger;

import java.lang.ref.SoftReference;


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
	public void run() {
	}
	@Override
	public boolean isRunning() {
		return false;
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
		while (true) {
			final int posA = str.indexOf("@|");
			if (posA == -1) break;
			final int posB = str.indexOf(' ', posA);
			final int posC = str.indexOf("|@", posB);
			if (posB == -1) break;
			if (posC == -1) break;
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
	public String getPrompt() {
		return null;
	}
	@Override
	public void setPrompt(final String prompt) {
	}
	@Override
	public void drawPrompt() {
	}



	@Override
	public void setCommandHandler(final xCommandHandler handler) {
	}



	// logger
	private volatile SoftReference<xLog> _log = null;
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null) {
				return log;
			}
		}
		final xLog log = xLog.getRoot();
		this._log = new SoftReference<xLog>(log);
		return log;
	}



}
