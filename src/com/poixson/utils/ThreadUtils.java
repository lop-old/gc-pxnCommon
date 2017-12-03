package com.poixson.utils;

import java.util.HashSet;
import java.util.Set;

import com.poixson.utils.xLogger.xLog;


public final class ThreadUtils {
	private ThreadUtils() {}
	{ Keeper.add(new ThreadUtils()); }

	public static final String[] ignoreThreadNames = new String[] {
//		"Main-Server-Thread",
//		"Reference Handler",
//		"NonBlockingInputStreamThread",
//		"process reaper",
//		"Signal Dispatcher",
//		"Java2D Disposer",
//		"AWT-EventQueue-0",
//		"AWT-XAWT",
//		"AWT-Shutdown",
//		"Finalizer",
//		"Exit"
	};



	// list running thread names
	public static String[] getThreadNames() {
		return getThreadNames(true);
	}
	public static String[] getThreadNames(final boolean includeDaemon) {
		final Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		if (threadSet.isEmpty())
			return null;
		final Set<String> list = new HashSet<String>();
		for (final Thread thread : threadSet) {
			if (!includeDaemon && thread.isDaemon())
				continue;
			final String name = thread.getName();
			if (Utils.isEmpty(name))
				continue;
			if (!includeDaemon && name.startsWith("main:"))
				continue;
			// check ignore list
			for (final String str : ignoreThreadNames) {
				if (name.equals(str)) {
					continue;
				}
			}
			// add to list
			list.add(thread.getName());
		}
		if (list.isEmpty())
			return null;
		return list.toArray(new String[0]);
	}
	// display threads still running
	public static void DisplayStillRunning() {
		if (!xVars.debug()) return;
		final String[] threadNames = getThreadNames(false);
		// no threads still running
		if (Utils.isEmpty(threadNames)) return;
		// build message
		final StringBuilder msg =
				(new StringBuilder())
					.append("Threads still running:  ")
					.append(threadNames.length);
		for (final String name : threadNames) {
			msg.append("\n  ").append(name);
		}
		xLog.getRoot()
			.publish(msg.toString());
	}



	// sleep thread
	public static void Sleep(final long ms) {
		if (ms < 1) return;
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignore) {}
	}
	public static void Sleep(final String time) {
		Sleep(
			xTime.get(time)
		);
	}
	public static void Sleep(final xTime time) {
		if (time == null) return;
		Sleep(
			time.getMS()
		);
	}



}
