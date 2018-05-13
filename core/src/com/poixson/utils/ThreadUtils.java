package com.poixson.utils;

import java.util.HashSet;
import java.util.Set;

import com.poixson.app.xVars;
import com.poixson.logger.xLogRoot;
import com.poixson.tools.Keeper;
import com.poixson.tools.xTime;


public final class ThreadUtils {
	private ThreadUtils() {}
	{ Keeper.add(new ThreadUtils()); }

	public static final String[] ignoreThreadNames = new String[] {
		"main-w1",
		"DestroyJavaVM"
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
		THREAD_LOOP:
		for (final Thread thread : threadSet) {
			if (!includeDaemon && thread.isDaemon())
				continue THREAD_LOOP;
			final String name = thread.getName();
			if (Utils.isEmpty(name))
				continue THREAD_LOOP;
			if (!includeDaemon && name.startsWith("main:"))
				continue THREAD_LOOP;
			// check ignore list
			//IGNORE_LOOP:
			for (final String str : ignoreThreadNames) {
				if (name.equals(str)) {
					continue THREAD_LOOP;
				}
			} // end IGNORE_LOOP
			// add to list
			list.add(thread.getName());
		} // end THREAD_LOOP
		if (list.isEmpty())
			return null;
		return list.toArray(new String[0]);
	}
	// display threads still running
	public static void DisplayStillRunning() {
		if (xVars.notDebug()) return;
		final String[] threadNames = getThreadNames(false);
		// no threads still running
		if (Utils.isEmpty(threadNames)) return;
		// build message
		final StringBuilder msg = new StringBuilder();
		msg.append("Threads still running: [")
			.append(threadNames.length)
			.append(']');
		boolean hasDestroyJavaVM = false;
		for (final String name : threadNames) {
			if ("DestroyJavaVM".equals(name))
				hasDestroyJavaVM = true;
			msg.append("\n  ").append(name);
		}
		if (hasDestroyJavaVM) {
			msg.append("\n\nShould use xApp.waitUntilClosed() when main() is finished.\n");
		}
		xLogRoot.get()
			.publish(
				msg.toString()
			);
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
			xTime.getNew(time)
		);
	}
	public static void Sleep(final xTime time) {
		if (time == null) return;
		Sleep(
			time.getMS()
		);
	}



}
