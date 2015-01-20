package com.poixson.commonapp.app;

import java.io.PrintStream;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import com.poixson.commonapp.xLogger.jlineConsole;
import com.poixson.commonjava.Failure;
import com.poixson.commonjava.xVars;
import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.Utils.mvnProps;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsDirFile;
import com.poixson.commonjava.Utils.utilsNumbers;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xClock;
import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.Utils.xStartable;
import com.poixson.commonjava.Utils.xThreadPool;
import com.poixson.commonjava.scheduler.xScheduler;
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
	private static final Keeper keeper = Keeper.get();

	public enum APP_STATE {
		STARTUP,
		RUNNING,
		STOPPING,
		STOPPED
	}
	protected volatile APP_STATE state = APP_STATE.STOPPED;

	@SuppressWarnings("unused")
	private volatile long startTime = -1;
	protected volatile int initLevel = 0;

	// mvn properties
	protected final mvnProps mvnprops;

	protected static final String ALREADY_STARTED_EXCEPTION = "Illegal app state; this shouldn't happen; cannot start in this state; possibly already started?";
	protected static final String ILLEGAL_STATE_EXCEPTION   = "Illegal app state; cannot continue; this shouldn't happen; Current state: ";



	/**
	 * Get the app class instance.
	 */
	public static xApp get() {
		return appInstance;
	}



	// call this from main(args)
	protected static void initMain(final String[] args, final xApp app) {
		// single instance
		if(appInstance != null) {
			log().trace(new RuntimeException(ALREADY_STARTED_EXCEPTION));
			Failure.fail(ALREADY_STARTED_EXCEPTION);
		}
		synchronized(xApp.appLock) {
			if(xApp.appInstance != null) {
				log().trace(new RuntimeException(ALREADY_STARTED_EXCEPTION));
				Failure.fail(ALREADY_STARTED_EXCEPTION);
			}
			xApp.appInstance = app;
		}
		// process command line arguments
		xApp.appInstance.processArgs(args);
		// initialize app for startup
		xApp.appInstance.Start();
		// start main thread queue
		xApp.appInstance.run();
		// main thread ended
		Failure.fail("@|FG_RED Main process ended! (this shouldn't happen)|@");
		System.exit(1);
	}



	// new instance
	protected xApp() {
		// mvn properties
		this.mvnprops = mvnProps.get(this.getClass());
		// initialize console and enable colors
		this.initConsole();
	}
	{
		// no console
		if(System.console() == null) {
			System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
		}
	}



	/**
	 * Application startup.
	 */
	@Override
	public void Start() {
		synchronized(this.state) {
			if(this.initLevel != 0) {
				log().trace(new RuntimeException(ILLEGAL_STATE_EXCEPTION+this.state.toString()));
				Failure.fail(ILLEGAL_STATE_EXCEPTION+this.state.toString());
			}
			if(!APP_STATE.STOPPED.equals(this.state)) {
				log().trace(new RuntimeException(ILLEGAL_STATE_EXCEPTION+this.state.toString()));
				Failure.fail(ILLEGAL_STATE_EXCEPTION+this.state.toString());
			}
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
		this.initConfig();
		if(Failure.hasFailed()) return;
		// load clock
		this.startTime = xClock.get(true).millis();
//TODO: does this work?
//		// load libraries
//		{
//			final String libPath = utilsDirFile.mergePaths(".", "lib");
//			if((new File(libPath)).isDirectory())
//				utilsDirFile.addLibraryPath(libPath);
//		}
		// ready for startup sequence
		if(this.initLevel != 1) {
			log().trace(new RuntimeException(ALREADY_STARTED_EXCEPTION));
			Failure.fail(ALREADY_STARTED_EXCEPTION);
		}
		if(!APP_STATE.STOPPED.equals(this.state)) {
			log().trace(new RuntimeException(ILLEGAL_STATE_EXCEPTION+this.state.toString()));
			Failure.fail(ILLEGAL_STATE_EXCEPTION+this.state.toString());
		}
		// run startup sequence (1-9)
		log().fine("Startup sequence.. 1..2..");
		this.getThreadPool().runLater(
			new _StartupRunnable(
				this,
				1
			)
		);
		// ready to start
	}
	@Override
	public void Stop() {
		// already stopping
		if(APP_STATE.STOPPING.equals(this.state))
			return;
		final xThreadPool pool = this.getThreadPool();
		if(pool == null) {
			log().severe("Failed to get thread pool for proper shutdown");
			xThreadPool.Exit();
		}
		// trigger shutdown sequence
		log().fine("Shutdown sequence.. 8..7..");
		pool.runLater(
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
			if(!utilsNumbers.isMinMax(step, 1, 8)) throw new UnsupportedOperationException("Unsupported startup step "+Integer.toString(step));
			this.app = app;
			this.step = step;
		}
		@Override
		public void run() {
			if(Failure.hasFailed())
				return;
			log().finest("[ "+Integer.toString(this.step)+" ] STARTUP");

			switch(this.step) {
			// first step in startup
			case 1: {
				synchronized(this.app.state) {
					if(!APP_STATE.STOPPED.equals(this.app.state)) {
						log().trace(new RuntimeException(ILLEGAL_STATE_EXCEPTION+this.app.state.toString()));
						Failure.fail(ILLEGAL_STATE_EXCEPTION+this.app.state.toString());
					}
					this.app.state = APP_STATE.STARTUP;
				}
				// lock file
				final String filename = this.app.getName()+".lock";
				if(!utilsDirFile.lockInstance(filename)) {
					Failure.fail("Failed to get lock on file: "+filename);
					return;
				}
				log().title("Starting "+this.app.getName()+"..");
				break;
			}
			case 2:
			case 3:
				break;
			// start scheduler
			case 4: {
				xScheduler.get().Start();
				break;
			}
			case 5:
			case 6:
			case 7:
				break;
			// last step in startup
			case 8: {
				this.app.state = APP_STATE.RUNNING;
				log().title(this.app.getName()+" Ready and Running!");
//				if(xVars.get().debug())
//					utils.MemoryStats();
				break;
			}
			default:
				throw new RuntimeException("Unknown startup step: "+Integer.toString(this.step));
			}

			// app steps 1-8
			try {
				if(!this.app.StartupStep(this.step))
					throw new RuntimeException("Startup failed at step: "+Integer.toString(this.step));
			} catch (Exception e) {
				log().trace(e);
				Failure.fail("Startup failed at step: "+Integer.toString(this.step));
				return;
			}

			// finished startup sequence
			if(this.step >= 8) {
				synchronized(this.app.state) {
					this.app.initLevel = 9;
				}
				return;
			}

			// sleep a short moment
			utilsThread.Sleep(5);

			// queue next step
			synchronized(this.app.state) {
				this.app.initLevel = this.step + 1;
				this.app.getThreadPool().runLater(
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
	 *   7. Stop scheduler
	 *   6-2. Abstracted to app
	 *   1. Last step in shutdown
	 *   0. Stopped
	 */
	private class _ShutdownRunnable extends xRunnable {
		private final xApp app;
		private final int step;
		public _ShutdownRunnable(final xApp app, final int step) {
			super(app.getName()+"-Shutdown-"+Integer.toString(step));
			//if(app == null) throw new NullPointerException();
			if(!utilsNumbers.isMinMax(step, 1, 8)) throw new UnsupportedOperationException("Unsupported shutdown step "+Integer.toString(step));
			this.app = app;
			this.step = step;
		}
		@Override
		public void run() {
			log().finest("[ "+Integer.toString(this.step)+" ] SHUTDOWN");

			switch(this.step) {
			// first step in shutdown
			case 8: {
				if(!APP_STATE.RUNNING.equals(this.app.state) && !APP_STATE.STARTUP.equals(this.app.state)) {
					log().trace(new RuntimeException(ILLEGAL_STATE_EXCEPTION+this.app.state.toString()));
					Failure.fail(ILLEGAL_STATE_EXCEPTION+this.app.state.toString());
				}
				log().title("Stopping "+this.app.getName()+"..");
				break;
			}
			// stop scheduler
			case 7: {
				xScheduler.get().Stop();
				xThreadPool.ShutdownAll();
				break;
			}
			case 6:
			case 5:
			case 4:
			case 3:
			case 2:
				break;
			// last step in shutdown
			case 1: {
				log().title(this.app.getName()+" Stopped.");
				//TODO: display total time running
				synchronized(this.app.state) {
					this.app.initLevel = 0;
					this.app.state = APP_STATE.STOPPED;
				}
				this.app.termConsole();
				break;
			}
			default:
				throw new RuntimeException("Unknown shutdown step: "+Integer.toString(this.step));
			}

			// app steps 8-1
			try {
				if(!this.app.ShutdownStep(this.step))
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
				xThreadPool.Exit();
				return;
			}

			// queue next step
			synchronized(this.app.state) {
				this.app.initLevel = this.step - 1;
				this.app.getThreadPool().runLater(
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



	protected abstract boolean StartupStep(final int step);
	protected abstract boolean ShutdownStep(final int step);

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
		this.getThreadPool().run();
	}
	/**
	 * Get the main thread queue.
	 * @return
	 */
	public xThreadPool getThreadPool() {
		if(this.threadPool == null)
			this.threadPool = xThreadPool.get();
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
			if(!utils.isJLineAvailable())
				Failure.fail("jline library not found");
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
		console.Start();
	}



	protected void termConsole() {
		final xConsole console = xLog.peekConsole();
		if(console != null)
			console.Stop();
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
	public static xLog log() {
		if(log == null)
			log = xLog.getRoot();
		return log;
	}



}
