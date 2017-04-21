package com.poixson.utils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import com.poixson.utils.xLogger.xLog;


public final class Failure {
	private Failure() {}

	public static final xTime EXIT_TIMEOUT = xTime.get("300n");

	private static final List<String> messages = new CopyOnWriteArrayList<String>();
	private static volatile boolean failed = false;

	private static final Set<xRunnable> actions = new CopyOnWriteArraySet<xRunnable>();



	public static void init() {
		Keeper.add(new Failure());
	}



	public static void fail() {
		fail(null, null);
	}
	public static void fail(final String msg) {
		fail(msg, null);
	}
	public static void fail(final Exception e) {
		fail(null, e);
	}
	public static void fail(final String msg, final Exception e) {
		failed = true;
		if (Utils.notEmpty(msg) || e != null) {
			final xLog log = xLog.peekRoot();
			if (Utils.notEmpty(msg)) {
				if (log == null) {
					System.out.println(msg);
				} else {
					log.fatal(msg);
				}
				addMessageSilently(msg);
			}
			if (e != null) {
				if (log == null) {
					e.printStackTrace();
				} else {
					log.trace(e);
				}
			}
		}
		doFailActions();
	}
	public static void reset() {
		failed = false;
		messages.clear();
	}



	// fail actions
	public static void register(final xRunnable action) {
		actions.add(action);
	}
	// perform actions
	protected static void doFailActions() {
		final xRunnable[] acts = actions.toArray(new xRunnable[0]);
		for (final xRunnable run : acts) {
			run.run();
			actions.remove(run);
		}
		ExitNow();
	}
	public static void ExitNow() {
		// wait for things to finish
		ThreadUtils.Sleep(EXIT_TIMEOUT.getMS());
		System.exit(1);
	}



	public static boolean hasFailed() {
		return failed;
	}
	public static String[] getMessages() {
		if (!failed) return null;
		return messages.toArray(new String[0]);
	}
	public static void addMessageSilently(final String msg) {
		messages.add(msg);
	}
//	@Override
//	public String toString() {
//		if(!this.failed)
//			return null;
//		synchronized(this.lock) {
//			final String[] msgs = this.getMessages();
//			if(msgs == null)
//				return null;
//			if(msgs.length == 0)
//				return "";
//			return utilsString.addArray(
//				"; ",
//				msgs
//			);
//		}
//	}



}
