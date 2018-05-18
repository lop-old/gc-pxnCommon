package com.poixson.app;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import com.poixson.logger.xLog;
import com.poixson.tools.Keeper;
import com.poixson.tools.xTime;
import com.poixson.utils.StringUtils;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.Utils;


public final class Failure {
	private Failure() {}
	static { Keeper.add(new Failure()); }

	public static final xTime EXIT_TIMEOUT = xTime.getNew("300n");

	private static final List<String> messages = new CopyOnWriteArrayList<String>();
	private static volatile boolean failed = false;

	private static final CopyOnWriteArraySet<Runnable> actions =
			new CopyOnWriteArraySet<Runnable>();



	public static void fail() {
		fail( (Throwable)null, (String)null );
	}
	public static void fail(final String msg, final Object... args) {
		fail( (Throwable)null, msg, args);
	}
	public static void fail(final Throwable e) {
		fail( e, (String)null );
	}
	public static void fail(final Throwable e, final String msg, final Object... args) {
		failed = true;
		final xLog log = xLog.peekRoot();
		if (Utils.notEmpty(msg)) {
			final String str = StringUtils.ReplaceTags(msg, args);
			if (log == null) {
				xVars.getOriginalErr()
					.println(str);
			} else {
				log.fatal(str);
			}
			addMessageSilently(str);
		}
		if (e != null) {
			if (log == null) {
				e.printStackTrace();
			} else {
				log.trace(e);
			}
		}
		doFailActions();
	}
	public static void reset() {
		failed = false;
		messages.clear();
	}



	// fail actions
	public static void register(final Runnable action) {
		actions.add(action);
		if (failed) {
			doFailActions();
		}
	}
	// perform actions
	protected static void doFailActions() {
		failed = true;
		while (!actions.isEmpty()) {
			final Iterator<Runnable> it = actions.iterator();
			while (it.hasNext()) {
				final Runnable run = it.next();
				it.remove();
				run.run();
			}
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
	@Override
	public String toString() {
		if (!failed)
			return null;
		final String[] msgs = getMessages();
		if (msgs == null)     return null;
		if (msgs.length == 0) return "";
		return StringUtils.MergeStrings(
			"; ",
			msgs
		);
	}



}
