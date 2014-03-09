package com.poixson.commonjava.app;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsDirFile;
import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.Utils.xThreadPool;


public abstract class xApp implements Runnable {

	private static volatile xApp appInstance = null;
	private static final Object appLock = new Object();

	private volatile xThreadPool threadPool = null;


	/**
	 * Get the app class instance.
	 */
	public static xApp get() {
		return appInstance;
	}
	protected static void Init(final AppFactory factory) {
		// single instance
		if(appInstance != null)
			_AlreadyStarted();
		synchronized(appLock) {
			if(appInstance != null)
				_AlreadyStarted();
			appInstance = factory.create();
		}
	}
	private static void _AlreadyStarted() {
		fail("Program already started?",
			new UnsupportedOperationException("Cannot redefine singleton instance of xApp; appInstance already set.")
		);
	}
	protected static interface AppFactory {
		public xApp create();
	}
	protected xApp() {

	}


	public abstract String getAppName();
	public abstract String getVersion();


	protected void ProcessArgs(final String[] args) {
	}
	/**
	 * Application startup.
	 */
	protected void StartApp() {
		// load libraries
		utilsDirFile.addLibraryPath(
			utilsDirFile.mergePaths(
				".",
				"resources"
			)
		);
		// no console
		if(System.console() == null) {
			System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
		}
		// start main thread queue
		threadPool = xThreadPool.get();
		threadPool.runLater(new xRunnable("Server-Startup") {
			@Override
			public void run() {
				get().start();
			}
		});
		threadPool.run();
		// main thread ended
		System.out.println("Main process ended (this shouldn't happen!)");
		System.out.println();
		System.out.println();
		System.exit(0);
	}
	protected abstract void start();
	protected abstract void stop();



	/**
	 * Start the main thread queue.
	 */
	@Override
	public void run() {
		getThreadPool().run();
	}
	public xThreadPool getThreadPool() {
		return threadPool;
	}


	// fail app startup
	public static void fail(String msg, Exception e) {
		if(utils.notEmpty(msg))
			System.out.println(msg);
		if(e != null)
			e.printStackTrace();
		System.exit(1);
	}
	public static void fail(String msg) {
		fail(msg, null);
	}
	public static void fail(Exception e) {
		fail(null, e);
	}
	public static void fail() {
		fail(null, null);
	}


}
