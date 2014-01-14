package com.poixson.commonjava.Utils;

import java.util.HashSet;
import java.util.Set;


public final class utilsThread {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	private utilsThread() {}


	// list running thread names
	public static String[] getThreadNames() {
		final Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		if(threadSet.isEmpty()) return null;
		final Set<String> list = new HashSet<String>();
		for(final Thread thread : threadSet) {
			switch(thread.getName()) {
//			case "Main-Server-Thread":
			case "Reference Handler":
			case "NonBlockingInputStreamThread":
			case "process reaper":
			case "Signal Dispatcher":
			case "Java2D Disposer":
			case "AWT-EventQueue-0":
			case "AWT-XAWT":
			case "AWT-Shutdown":
			case "Finalizer":
			case "Exit":
				continue;
			default:
			}
			// add to list
			list.add(thread.getName());
		}
		if(list.isEmpty()) return null;
		return list.toArray(new String[0]);
	}


	// sleep thread
	public static void Sleep(final long ms) {
		if(ms < 1) return;
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignore) {}
	}
	public static void Sleep(final xTime time) {
		if(time == null) return;
		Sleep(time.getMS());
	}


}
