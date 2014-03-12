package com.poixson.commonjava.app;

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
	private static final Object appLock = new Object();
	private volatile xThreadPool threadPool = null;

	@SuppressWarnings("unused")
	private volatile long startTime = -1;
	private volatile Integer initLevel = 0;


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
		synchronized(appLock) {
			if(appInstance != null)
				_AlreadyStarted();
			appInstance = app;
		}
		appInstance.processArgs(args);
		appInstance.init();
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
		synchronized(initLevel) {
			if(initLevel != 0) _AlreadyStarted();
			initLevel = 1;
		}
		// init logger
		log().setLevel(xLevel.ALL);
		// load config
		initConfig();
		// load clock
		startTime = xClock.get(true).millis();
		// load libraries
		final String libPath = utilsDirFile.mergePaths(".", "lib");
		if((new File(libPath)).isDirectory())
			utilsDirFile.addLibraryPath(libPath);
		// no console
		if(System.console() == null) {
			System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
		}
		// main thread queue
		threadPool = xThreadPool.get();
		// run startup sequence (1-9)
		if(initLevel != 1) _AlreadyStarted();
		// startup sequence
		threadPool.runLater(new StartupRunnable(1));
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
		threadPool.runLater(new ShutdownRunnable(8));
	}


	/**
	 * Startup sequence.
	 *   0. Stopped
	 *   1. First step in startup
	 *   2-7. Abstracted to app
	 *   8. Last step in startup
	 *   9. Running
	 */
	private class StartupRunnable extends xRunnable {
		private final int step;
		public StartupRunnable(final int step) {
			super(getAppName()+"-Startup-"+Integer.toString(step));
			if(initLevel < 1 || initLevel >= 9) throw new UnsupportedOperationException();
			if(step      < 1 || step      >= 9) throw new UnsupportedOperationException();
			this.step = step;
		}
		@Override
		public void run() {
			switch(step) {
			// first step in startup
			case 1:
				// lock file
				utilsDirFile.lockInstance(getAppName()+".lock");
				log().title("Starting "+getAppName()+"..");
				break;
			// last step in startup
			case 8:
				log().title(getAppName()+" Ready and Running!");
				synchronized(initLevel) {
					initLevel = 9;
				}
				return;
			// app steps 2-7
			default:
				try {
					if(startup(step))
						break;
				} catch (Exception e) {
					log().trace(e);
				}
				return;
			}
			// queue next step
			synchronized(initLevel) {
				initLevel = step + 1;
				threadPool.runLater(new StartupRunnable(initLevel));
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
	private class ShutdownRunnable extends xRunnable {
		private final int step;
		public ShutdownRunnable(final int step) {
			super(getAppName()+"-Shutdown-"+Integer.toString(step));
			if(initLevel < 1 || initLevel >= 9) throw new UnsupportedOperationException();
			if(step      < 1 || step      >= 9) throw new UnsupportedOperationException();
			this.step = step;
		}
		@Override
		public void run() {
			switch(step) {
			// first step in startup
			case 8:
				log().title("Stopping "+getAppName()+"..");
				break;
			// last step in startup
			case 1:
				log().title(getAppName()+" Stopped.");
				//TODO: display total time running
				synchronized(initLevel) {
					initLevel = 0;
				}
				return;
			// app steps 7-2
			default:
				try {
					shutdown(step);
				} catch (Exception e) {
					log().trace(e);
				}
				break;
			}
			// queue next step
			synchronized(initLevel) {
				initLevel = step - 1;
				threadPool.runLater(new ShutdownRunnable(initLevel));
			}
		}
	}


	protected abstract boolean startup(final int step);
	protected abstract boolean shutdown(final int step);

	protected abstract void initConfig();
	protected abstract void processArgs(final String[] args);
	public abstract void processCommand(final String commandStr);

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
		return threadPool;
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


	// debug mode
	private static volatile Boolean globalDebug = null;
	public static void debug(boolean debug) {
		globalDebug = debug;
	}
	public static boolean debug() {
		if(globalDebug == null)
			return false;
		return globalDebug.booleanValue();
	}


}
