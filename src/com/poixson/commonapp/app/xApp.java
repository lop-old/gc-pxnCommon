package com.poixson.commonapp.app;

import java.io.File;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsDirFile;
import com.poixson.commonjava.Utils.xClock;
import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.Utils.xThreadPool;
import com.poixson.commonjava.xLogger.xConsole;
import com.poixson.commonjava.xLogger.xLevel;
import com.poixson.commonjava.xLogger.xLog;
import com.poixson.commonjava.xLogger.console.jlineConsole;
import com.poixson.commonjava.xLogger.console.xNoConsole;
import com.poixson.commonjava.xLogger.formatters.defaultLogFormatter_Color;
import com.poixson.commonjava.xLogger.handlers.logHandlerConsole;


/**
 * Startup sequence
 *   a. initMain()     | internal
 *   b. processArgs()  | abstracted to app
 *   c. init()         | internal
 *   d. initConfig()   | abstracted to app
 *   e. startup(step)  | steps 2-7 abstracted to app
 * Shutdown sequence
 *   a. shutdown()     | internal
 *   b. shutdown(step) | steps 7-2 abstracted to app
 */
public abstract class xApp implements Runnable {

	private static volatile xApp appInstance = null;
	protected static final Object appLock = new Object();
	private volatile xThreadPool threadPool = null;

	@SuppressWarnings("unused")
	private volatile long startTime = -1;
	protected volatile int initLevel = 0;


	/**
	 * Get the app class instance.
	 */
	public static xApp get() {
		return appInstance;
	}


	protected static void initMain(final String[] args, final xApp app) {
		// single instance
		if(appInstance != null)
			_AlreadyStarted();
		synchronized(xApp.appLock) {
			if(xApp.appInstance != null)
				_AlreadyStarted();
			xApp.appInstance = app;
		}
		xApp.appInstance.processArgs(args);
		xApp.appInstance.init();
	}
	private static void _AlreadyStarted() {
		fail("Program already started?",
			new UnsupportedOperationException("Cannot redefine singleton instance of xApp; appInstance already set.")
		);
	}
	protected xApp() {
	}


	/**
	 * Application startup.
	 */
	public void init() {
		synchronized(xApp.appLock) {
			if(this.initLevel != 0) _AlreadyStarted();
			this.initLevel = 1;
		}
		// init logger
		log().setLevel(xLevel.ALL);
		// load config
		initConfig();
		// load clock
		this.startTime = xClock.get(true).millis();
		// load libraries
		final String libPath = utilsDirFile.mergePaths(".", "lib");
		if((new File(libPath)).isDirectory())
			utilsDirFile.addLibraryPath(libPath);
		// no console
		if(System.console() == null) {
			System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
		}
		// main thread queue
		if(this.threadPool == null)
			this.threadPool = xThreadPool.get();
		// run startup sequence (1-9)
		if(this.initLevel != 1) _AlreadyStarted();
		// startup sequence
		getThreadPool().runLater(
			new _StartupRunnable(
				this,
				1
			)
		);
		// start main thread queue
		run();
		// main thread ended
		AnsiConsole.out.println();
		AnsiConsole.out.println(Ansi.ansi().fg(Ansi.Color.RED).a("Main process ended! (this shouldn't happen)").reset());
		AnsiConsole.out.println();
		AnsiConsole.out.println();
		System.exit(0);
	}
	public void shutdown() {
		// shutdown sequence
		getThreadPool().runLater(
			new _ShutdownRunnable(
				this,
				8
			)
		);
	}


	/**
	 * Startup sequence.
	 *   0. Stopped
	 *   1. First step in startup
	 *   2-7. Abstracted to app
	 *   8. Last step in startup
	 *   9. Running
	 */
	private class _StartupRunnable extends xRunnable {
		private final xApp app;
		private final int step;
		public _StartupRunnable(final xApp app, final int step) {
			super(getAppName()+"-Startup-"+Integer.toString(step));
			if(app == null) throw new NullPointerException();
			if(step < 1 || step >= 9) throw new UnsupportedOperationException();
			this.app = app;
			this.step = step;
		}
		@Override
		public void run() {
			switch(this.step) {
			// first step in startup
			case 1:
				// lock file
				utilsDirFile.lockInstance(getAppName()+".lock");
				log().title("Starting "+getAppName()+"..");
				break;
			// last step in startup
			case 8:
				log().title(getAppName()+" Ready and Running!");
				synchronized(xApp.appLock) {
					this.app.initLevel = 9;
				}
				return;
			// app steps 2-7
			default:
				try {
					if(startup(this.step))
						break;
				} catch (Exception e) {
					log().trace(e);
				}
				return;
			}
			// queue next step
			synchronized(xApp.appLock) {
				this.app.initLevel = this.step + 1;
				getThreadPool().runLater(
					new _StartupRunnable(
						this.app,
						this.app.initLevel
					)
				);
			}
		}
	}
	/**
	 * Shutdown sequence.
	 *   9. Running
	 *   8. First step in shutdown
	 *   7-2. Abstracted to app
	 *   1. Last step in shutdown
	 *   0. Stopped
	 */
	private class _ShutdownRunnable extends xRunnable {
		private final xApp app;
		private final int step;
		public _ShutdownRunnable(final xApp app, final int step) {
			super(getAppName()+"-Shutdown-"+Integer.toString(step));
			if(app == null) throw new NullPointerException();
			if(step < 1 || step >= 9) throw new UnsupportedOperationException();
			this.app = app;
			this.step = step;
		}
		@Override
		public void run() {
			switch(this.step) {
			// first step in startup
			case 8:
				log().title("Stopping "+getAppName()+"..");
				break;
			// last step in startup
			case 1:
				log().title(getAppName()+" Stopped.");
				//TODO: display total time running
				synchronized(xApp.appLock) {
					this.app.initLevel = 0;
				}
				return;
			// app steps 7-2
			default:
				try {
					shutdown(this.step);
				} catch (Exception e) {
					log().trace(e);
				}
				break;
			}
			// queue next step
			synchronized(xApp.appLock) {
				this.app.initLevel = this.step - 1;
				getThreadPool().runLater(
					new _ShutdownRunnable(
						this.app,
						this.app.initLevel
					)
				);
			}
		}
	}


	protected abstract boolean startup(final int step);
	protected abstract boolean shutdown(final int step);

	protected abstract void initConfig();
	protected abstract void processArgs(final String[] args);

	public abstract String getAppName();
	public abstract String getVersion();


	/**
	 * Start the main thread queue.
	 */
	@Override
	public void run() {
		getThreadPool().run();
	}
	/**
	 * Get the main thread queue.
	 * @return
	 */
	public xThreadPool getThreadPool() {
		return this.threadPool;
	}


	// start console prompt
	protected void initConsole() {
		xConsole console = xLog.peekConsole();
		if(console == null || console instanceof xNoConsole) {
			console = new jlineConsole();
			xLog.setConsole(console);
		}
		console.start();
		// enable console color
		log().setFormatter(
			new defaultLogFormatter_Color(),
			logHandlerConsole.class
		);
	}


	// logger
	private static volatile xLog log = null;
	private static final Object logLock = new Object();
	public static xLog log() {
		if(log == null) {
			synchronized(logLock) {
				if(log == null)
					log = xLog.getRoot();
			}
		}
		return log;
	}


	// fail app startup
	public static void fail(String msg, Exception e) {
		if(utils.notEmpty(msg))
			log().publish(msg);
		if(e != null)
			log().trace(e);
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
