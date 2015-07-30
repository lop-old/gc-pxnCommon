package com.poixson.commonapp.app;

import java.io.PrintStream;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import com.poixson.commonapp.app.annotations.xAppStep;
import com.poixson.commonapp.app.annotations.xAppStep.StepType;
import com.poixson.commonapp.xLogger.jlineConsole;
import com.poixson.commonjava.Failure;
import com.poixson.commonjava.xVars;
import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.Utils.mvnProps;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsProc;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.xClock;
import com.poixson.commonjava.Utils.xStartable;
import com.poixson.commonjava.Utils.threads.xThreadPool;
import com.poixson.commonjava.scheduler.xScheduler;
import com.poixson.commonjava.scheduler.ticker.xTicker;
import com.poixson.commonjava.xLogger.xConsole;
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
public abstract class xApp implements xStartable {

	private static volatile xApp appInstance = null;
	private static final Object appLock = new Object();

	// just to prevent gc
	@SuppressWarnings("unused")
	private static final Keeper keeper = Keeper.get();

	private volatile long startTime = -1;

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
	protected static void initMain(final String[] args, final Class<? extends xApp> appClass) {
		if(appClass == null) throw new NullPointerException();
		// single instance
		if(appInstance != null) {
			log().trace(new RuntimeException(ALREADY_STARTED_EXCEPTION));
			Failure.fail(ALREADY_STARTED_EXCEPTION);
		}
		xVars.init();
		synchronized(appLock) {
			if(appInstance != null) {
				log().trace(new RuntimeException(ALREADY_STARTED_EXCEPTION));
				Failure.fail(ALREADY_STARTED_EXCEPTION);
				return;
			}
			try {
				appInstance = appClass.newInstance();
			} catch (ReflectiveOperationException e) {
				Failure.fail(e.getMessage());
				log().trace(e);
				return;
			}
		}
		// no console
		if(System.console() == null)
			System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
		// initialize console and enable colors
		initConsole();
		// process command line arguments
		appInstance.processArgs(args);
		// handle command-line arguments
		appInstance.displayStartupVars();
		// app startup
		appInstance.Start();
		// pass main thread to thread pool
		appInstance.run();
		// main thread ended
		Failure.fail("@|FG_RED Main process ended! (this shouldn't happen)|@");
		System.exit(1);
	}



	// new instance
	protected xApp() {
		// mvn properties
		this.mvnprops = mvnProps.get(this.getClass());
	}



	protected abstract void processArgs(final String[] args);



	@Override
	public void Start() {
		xAppManager.get()
			.Start();
	}
	@Override
	public void Stop() {
		xAppManager.get()
			.Stop();
	}
	// Start the main thread queue
	@Override
	public void run() {
		xAppManager.get()
			.run();
	}



	// ------------------------------------------------------------------------------- //
	// startup



	// lock file
	@xAppStep(type=StepType.STARTUP, title="LockFile", priority=3)
	public void __STARTUP_lockfile() {
		final String filename = this.getName()+".lock";
		if(!utilsProc.lockInstance(filename)) {
			Failure.fail("Failed to get lock on file: "+filename);
			return;
		}
	}



	// ensure not root
	@xAppStep(type=StepType.STARTUP, title="RootCheck", priority=5)
	public void __STARTUP_rootcheck() {
		final String user = System.getProperty("user.name");
		if("root".equals(user))
			log().warning("It is recommended to run as a non-root user");
		else
		if("administrator".equalsIgnoreCase(user) || "admin".equalsIgnoreCase(user))
			log().warning("It is recommended to run as a non-administrator user");
	}



	// clock
	@xAppStep(type=StepType.STARTUP, title="Clock", priority=11)
	public void __STARTUP_clock() {
		this.startTime = xClock.get(true).millis();
	}



	// scheduler
	@xAppStep(type=StepType.STARTUP, title="Scheduler", priority=75)
	public void __STARTUP_scheduler() {
		xScheduler.get()
			.Start();
	}



	// tick scheduler
	@xAppStep(type=StepType.STARTUP, title="Ticker", priority=80)
	public void __STARTUP_ticker() {
		xTicker.get()
			.Start();
	}



	// ------------------------------------------------------------------------------- //
	// shutdown



	// total time running
	@xAppStep(type=StepType.SHUTDOWN, title="TimeRunning", priority=100)
	public void __SHUTDOWN_timerunning() {
//TODO: display total time running
	}



	// stop ticker
	@xAppStep(type=StepType.SHUTDOWN, title="Ticker", priority=80)
	public void __SHUTDOWN_ticker() {
		xTicker.get()
			.Stop();
	}



	// stop scheduler
	@xAppStep(type=StepType.SHUTDOWN, title="Scheduler", priority=75)
	public void __SHUTDOWN_scheduler() {
		xScheduler.get()
			.Stop();
	}



	// stop thread pools
	@xAppStep(type=StepType.SHUTDOWN, title="ThreadPools", priority=70)
	public void __SHUTDOWN_threadpools() {
		xThreadPool
			.ShutdownAll();
	}



	// stop console input
	@xAppStep(type=StepType.SHUTDOWN, title="Console", priority=32)
	public void __SHUTDOWN_console() {
		xLog.shutdown();
	}



	// ------------------------------------------------------------------------------- //



	@Override
	public boolean isRunning() {
		return xAppManager.get()
				.isRunning();
	}



	public long getUptime() {
		if(this.startTime == -1)
			return 0;
		return xClock.get(true).millis() - this.startTime;
	}
	public String getUptimeString() {
//TODO:
		return "<UPTIME>";
	}



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



	// initialize console and enable colors
	protected static void initConsole() {
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
//		out.println(" Terminal:    "+System.getProperty("jline.terminal"));
		out.println(" Pid: "+utilsProc.getPid());
		if(xVars.debug())
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
