package com.poixson.commonjava;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.xLogger.xLog;


public class Failure {

	private static final List<String> messages = new CopyOnWriteArrayList<String>();
	private static volatile boolean failed = false;

	private static final Set<FailureAction> actions = new CopyOnWriteArraySet<FailureAction>();



	public static interface FailureAction {

		public void doFailAction();

	}



	public static void init() {
		// just to prevent gc
		Keeper.add(new Failure());
	}
	private Failure() {}



	public static void fail(final String msg) {
		failed = true;
		if(utils.notEmpty(msg)) {
			xLog.getRoot().fatal(msg);
			messages.add(msg);
		}
		doFailActions();
	}
//	public static void fail() {
//		fail(null);
//	}
	public static void reset() {
		failed = false;
		messages.clear();
	}



	// fail actions
	public static void register(final FailureAction action) {
		actions.add(action);
	}
	// perform actions
	protected static void doFailActions() {
		final FailureAction[] acts = actions.toArray(new FailureAction[0]);
		for(final FailureAction act : acts) {
			act.doFailAction();
			actions.remove(act);
		}
		new Thread() {
			@Override
			public void run() {
				utilsThread.Sleep(150L);
				System.exit(1);
			}
		}.start();
	}



	public static boolean hasFailed() {
		return failed;
	}
	public static String[] getMessages() {
		if(!failed)
			return null;
		return messages.toArray(new String[0]);
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
