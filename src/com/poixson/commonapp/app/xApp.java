package com.poixson.commonapp.app;

import java.io.File;
import java.io.PrintStream;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import com.poixson.commonapp.xLogger.jlineConsole;
import com.poixson.commonjava.Failure;
import com.poixson.commonjava.xVars;
import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.Utils.mvnProps;
import com.poixson.commonjava.Utils.utilsDirFile;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xClock;
import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.Utils.xStartable;
import com.poixson.commonjava.Utils.xThreadPool;
import com.poixson.commonjava.xLogger.xConsole;
import com.poixson.commonjava.xLogger.xLevel;
import com.poixson.commonjava.xLogger.xLog;
import com.poixson.commonjava.xLogger.xNoConsole;
import com.poixson.commonjava.xLogger.formatters.defaultLogFormatter_Color;
import com.poixson.commonjava.xLogger.handlers.logHandlerConsole;


/**
 * Startup sequence
 *   a. initMain()     | internal
 *   b. processArgs()  | abstracted to app
 *   c. init()         | internal
 *   d. initConfig()   | abstracted to app
 *   e. sync clock
 *   f. start thread queue
 *   g. startup(steps 1-8)  | steps abstracted to app
 * Shutdown sequence
 *   a. shutdown()     | internal
 *   b. shutdown(steps 8-1) | steps abstracted to app
 */
public abstract class xApp implements xStartable, Failure.FailureAction {

	private static volatile xApp appInstance = null;
	protected static final Object appLock = new Object();
	private volatile xThreadPool threadPool = null;

	// just to prevent gc
	@SuppressWarnings("unused")
	private static Keeper keeper = null;

	@SuppressWarnings("unused")
	private volatile long startTime = -1;
	protected volatile int initLevel = 0;

	// mvn properties
	protected final mvnProps mvnprops;



	/**
	 * Get the app class instance.
	 */
	public static xApp get() {
		return appInstance;
	}



	// call this from main(args)
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
		xApp.appInstance.Start();
	}
	private static void _AlreadyStarted() {
		log().trace(new UnsupportedOperationException("Cannot redefine singleton instance of xApp; appInstance already set."));
		Failure.fail("Program already started?");
		System.exit(1);
	}
	// new instance
	protected xApp() {
		// mvn properties
		this.mvnprops = mvnProps.get(this.getClass());
		// initialize console and enable colors
		this.initConsole();
	}
	// static
	{
		// just to prevent gc
		keeper = Keeper.get();
		// no console
		if(System.console() == null) {
			System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
		}
	}



	/**
	 * Application startup.
	 */
	@Override
	public boolean Start() {
		synchronized(xApp.appLock) {
			if(this.initLevel != 0)
				_AlreadyStarted();
			this.initLevel = 1;
		}
		// init logger
		log().setLevel(xLevel.ALL);
		if(Failure.hasFailed()) {
			System.out.println("Failure, pre-init!");
			System.exit(1);
		}
		Failure.register(this);
		// load config
		initConfig();
		// load clock
		this.startTime = xClock.get(true).millis();
		// load libraries
		{
			final String libPath = utilsDirFile.mergePaths(".", "lib");
			if((new File(libPath)).isDirectory())
				utilsDirFile.addLibraryPath(libPath);
		}
		// main thread queue
		if(this.threadPool == null)
			this.threadPool = xThreadPool.get();
		// run startup sequence (1-9)
		if(this.initLevel != 1)
			_AlreadyStarted();
		// trigger startup sequence
		log().fine("Startup sequence.. 1..2..");
		getThreadPool().runLater(
			new _StartupRunnable(
				this,
				1
			)
		);
		// start main thread queue
		run();
		// main thread ended
		Failure.fail("@|FG_RED Main process ended! (this shouldn't happen)|@");
		System.exit(1);
		return false;
	}
	@Override
	public void Stop() {
		// trigger shutdown sequence
		log().fine("Shutdown sequence.. 8..7..");
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
			super(app.getName()+"-Startup-"+Integer.toString(step));
			//if(app == null) throw new NullPointerException();
			if(step < 1 || step > 8) throw new UnsupportedOperationException("Unsupported startup step "+Integer.toString(step));
			this.app = app;
			this.step = step;
		}
		@Override
		public void run() {
			if(Failure.hasFailed())
				return;

			switch(this.step) {
			// first step in startup
			case 1: {
				// lock file
				utilsDirFile.lockInstance(this.app.getName()+".lock");
				log().title("Starting "+this.app.getName()+"..");
				break;
			}
			// last step in startup
			case 8: {
				log().title(this.app.getName()+" Ready and Running!");
				break;
			}
			default:
				break;
			}

			// app steps 1-8
			try {
				if(!startup(this.step))
					throw new RuntimeException("Startup failed at step: "+Integer.toString(this.step));
			} catch (Exception e) {
				log().trace(e);
				Failure.fail("Startup failed at step: "+Integer.toString(this.step));
				return;
			}

			// finished startup sequence
			if(this.step >= 8) {
				synchronized(xApp.appLock) {
					this.app.initLevel = 9;
				}
				return;
			}

			// sleep a short moment
			utilsThread.Sleep(5);

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
			super(app.getName()+"-Shutdown-"+Integer.toString(step));
			//if(app == null) throw new NullPointerException();
			if(step < 1 || step > 8) throw new UnsupportedOperationException("Unsupported shutdown step "+Integer.toString(step));
			this.app = app;
			this.step = step;
		}
		@Override
		public void run() {

			switch(this.step) {
			// first step in shutdown
			case 8: {
				log().title("Stopping "+this.app.getName()+"..");
				break;
			}
			// last step in shutdown
			case 1: {
				log().title(this.app.getName()+" Stopped.");
				//TODO: display total time running
				synchronized(xApp.appLock) {
					this.app.initLevel = 0;
				}
				termConsole();
				break;
			}
			default:
				break;
			}

			// app steps 8-1
			try {
				if(!shutdown(this.step))
					throw new RuntimeException();
			} catch (Exception e) {
				log().severe("Shutdown failed at step: "+Integer.toString(this.step));
				log().trace(e);
			}

			// sleep for a moment
			utilsThread.Sleep(50);

			// finished shutdown sequence
			if(this.step <= 1) {
				this.app.initLevel = 0;
				System.out.println();
				System.exit(0);
				return;
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



	@Override
	public boolean isRunning() {
		return this.initLevel != 0;
	}



	protected abstract boolean startup(final int step);
	protected abstract boolean shutdown(final int step);

	protected abstract void initConfig();
	protected abstract void processArgs(final String[] args);



	// mvn properties
	public String getName() {
		return this.mvnprops.name;
	}
	public String getTitle() {
		return this.mvnprops.title;
	}
	public String getFullTitle() {
		return this.mvnprops.full_title;
	}
	public String getVersion() {
		return this.mvnprops.version;
	}
	public String getURL() {
		return this.mvnprops.url;
	}
	public String getOrgName() {
		return this.mvnprops.org_name;
	}
	public String getOrgURL() {
		return this.mvnprops.org_url;
	}
	public String getIssueName() {
		return this.mvnprops.issue_name;
	}
	public String getIssueURL() {
		return this.mvnprops.issue_url;
	}



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
	@Override
	public void doFailAction() {
		this.Stop();
	}



	// initialize console and enable colors
	protected void initConsole() {
		xConsole console = xLog.peekConsole();
		if(console == null || console instanceof xNoConsole) {
			console = new jlineConsole();
			xLog.setConsole(console);
		}
		// enable console color
		log().setFormatter(
			new defaultLogFormatter_Color(),
			logHandlerConsole.class
		);
	}
	// start console prompt
	protected void startConsole() {
		final xConsole console = xLog.peekConsole();
		console.start();
	}



	protected void termConsole() {
		final xConsole console = xLog.peekConsole();
		if(console != null)
			console.stop();
	}



	// ascii header
	protected void displayColors() {
		final PrintStream out = AnsiConsole.out;
		out.println(Ansi.ansi().reset());
		for(final Ansi.Color color : Ansi.Color.values()) {
			final String name = utilsString.padCenter(7, color.name(), ' ');
			out.println(Ansi.ansi()
				.a("   ")
				.fg(color).a(name)
				.a("   ")
				.bold().a("BOLD-"+name)
				.a("   ")
				.boldOff().fg(Ansi.Color.WHITE).bg(color).a(name)
				.reset()
			);
		}
		out.println(Ansi.ansi().reset());
		out.println();
		out.flush();
	}
	public void displayStartupVars() {
		final PrintStream out = AnsiConsole.out;
		out.println();
		out.println(" "+this.mvnprops.full_title);
		out.println(" Running as:  "+System.getProperty("user.name"));
		out.println(" Current dir: "+System.getProperty("user.dir"));
		out.println(" java home:   "+System.getProperty("java.home"));
		out.println(" Terminal:    "+System.getProperty("jline.terminal"));
		if(xVars.get().debug())
			out.println(" Forcing Debug: true");
//		if(utils.notEmpty(args)) {
//			out.println();
//			out.println(utilsString.addStrings(" ", args));
//		}
		out.println();
		out.flush();
	}
	protected void displayLogo() {
		final PrintStream out = AnsiConsole.out;
		final Ansi.Color bgcolor = Ansi.Color.BLACK;
		out.println();
		// line 1
		out.println(Ansi.ansi()
			.a(" ").bg(bgcolor)
			.a("                                ")
			.a("                                 ")
			.reset() );
		// line 2
		out.println(Ansi.ansi()
			.a(" ").bg(bgcolor)
			.fg(Ansi.Color.YELLOW).a("         |`-.__")
			.a("                         ")
			.a("                         ")
			.reset() );
		// line 3
		out.println(Ansi.ansi()
			.a(" ").bg(bgcolor)
			.fg(Ansi.Color.YELLOW).a("         / ' _/")
			.a("                         ")
			.a("                         ")
			.reset() );
		// line 4
		out.println(Ansi.ansi()
			.a(" ").bg(bgcolor)
			.fg(Ansi.Color.RED).a("        ****")
			.fg(Ansi.Color.RED).a("\"         ")
			.a("                     ")
			.a("                      ")
			.reset() );
		// line 5
		out.println(Ansi.ansi()
			.a(" ").bg(bgcolor)
			.fg(Ansi.Color.YELLOW).a("      /    }")
			.a("             ")
			.fg(Ansi.Color.CYAN).bold()
			.a(utilsString.padCenter(20, this.mvnprops.name, ' '))
			.boldOff()
			.a("                    ")
			.reset() );
		// line 6
		out.println(Ansi.ansi()
			.a(" ").bg(bgcolor)
			.fg(Ansi.Color.YELLOW).a("     /    \\")
			.a("              ")
			.fg(Ansi.Color.CYAN)
			.a(utilsString.padCenter(20, this.mvnprops.version, ' '))
			.a("                    ")
			.reset() );
		// line 7
		out.println(Ansi.ansi()
			.a(" ").bg(bgcolor)
			.fg(Ansi.Color.YELLOW).a(" \\ /`    \\\\\\")
			.a("                          ")
			.a("                           ")
			.reset() );
		// line 8
		out.println(Ansi.ansi()
			.a(" ").bg(bgcolor)
			.fg(Ansi.Color.YELLOW).a("  `\\     /_\\\\")
			.a("                          ")
			.a("                          ")
			.reset() );
		// line 9
		out.println(Ansi.ansi()
			.a(" ").bg(bgcolor)
			.fg(Ansi.Color.YELLOW).a("   `~~~~~~``~`")
			.a("                         ")
			.a("                          ")
			.reset() );
		// line 10
		out.println(Ansi.ansi()
			.a(" ").bg(bgcolor)
			.fg(Ansi.Color.GREEN)
			.a("^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/")
			.a("^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^")
			.reset() );
		// line 11
		out.println(Ansi.ansi()
			.a(" ").bg(bgcolor)
			.fg(Ansi.Color.GREEN)
			.a("////////////////////////////////")
			.a("/////////////////////////////////")
			.reset() );
		out.println();
		out.flush();
	}
// 1 |                                                                   |
// 2 |          |`-.__                                                   |
// 3 |          / ' _/                                                   |
// 4 |         ****\"                                                    |
// 5 |       /    }                                                      |
// 6 |      /    \                                                       |
// 7 |  \ /`    \\\                                                      |
// 8 |   `\     /_\\                                                     |
// 9 |    `~~~~~~``~`                                                    |
//10 | ^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^ |
//11 | ///////////////////////////////////////////////////////////////// |
//   0 2 4 6 8 0 2 4 6 8 0 2 4 6 8 0 2 4 6 8 0 2 4 6 8 0 2 4 6 8 0 2 4 6 8
//   0         1         2         3         4         5         6



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



}
