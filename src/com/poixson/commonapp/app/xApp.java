package com.poixson.commonapp.app;

import java.io.PrintStream;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import com.poixson.commonapp.app.annotations.xAppStep;
import com.poixson.commonapp.app.annotations.xAppStep.StepType;
import com.poixson.commonjava.Failure;
import com.poixson.commonjava.Utils.LockFile;
import com.poixson.commonjava.Utils.mvnProps;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.exceptions.RequiredArgumentException;
import com.poixson.commonjava.Utils.threads.xThreadPool;
import com.poixson.commonjava.scheduler.xScheduler;
import com.poixson.commonjava.xLogger.xLevel;
import com.poixson.commonjava.xLogger.xLog;
import com.poixson.commonjava.xLogger.commands.xCommandsHandler;


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
public abstract class xApp extends xAppAbstract {

	private static final String ALREADY_STARTED_EXCEPTION = "Illegal app state; this shouldn't happen; cannot start in this state; possibly already started?";
//	private static final String ILLEGAL_STATE_EXCEPTION   = "Illegal app state; cannot continue; this shouldn't happen; Current state: ";

	// mvn properties
	protected final mvnProps mvnprops;



	/**
	 * Get a single instance of the app.
	 */
	public static xApp get() {
		return (xApp) xAppAbstract.get();
	}
	public static xApp peak() {
		return (xApp) xAppAbstract.peak();
	}



	// call this from main(args)
	protected static void initMain(final String[] args,
			final Class<? extends xApp> appClass) {
		if(appClass == null) throw new RequiredArgumentException("appClass");
		// single instance
		if(instance != null) {
			get().log().trace(new RuntimeException(ALREADY_STARTED_EXCEPTION));
			Failure.fail(ALREADY_STARTED_EXCEPTION);
		}
		synchronized(instanceLock) {
			if(instance != null) {
				get().log().trace(new RuntimeException(ALREADY_STARTED_EXCEPTION));
				Failure.fail(ALREADY_STARTED_EXCEPTION);
				return;
			}
			try {
				instance = appClass.newInstance();
			} catch (ReflectiveOperationException e) {
				get().log().trace(e);
				Failure.fail(e.getMessage());
				return;
			}
		}
		// init logger
		xLog.getRoot().setLevel(xLevel.ALL);
		if(Failure.hasFailed()) {
			System.out.println("Failure, pre-init!");
			System.exit(1);
		}
		// no console
		if(System.console() == null)
			System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
		// initialize console and enable colors
		initConsole();
		// process command line arguments
		instance.processArgs(args);
		// handle command-line arguments
		instance.displayStartupVars();
		// app startup
		instance.Start();
		// pass main thread to thread pool
		instance.run();
		// main thread ended
		Failure.fail("@|FG_RED Main process ended! (this shouldn't happen)|@");
		System.exit(1);
	}



	// new instance
	protected xApp() {
		super();
		// mvn properties
		this.mvnprops = mvnProps.get(this.getClass());
	}



	public xCommandsHandler getCommandsHandler() {
		return null;
	}



	// ------------------------------------------------------------------------------- //
	// startup



	@Override
	public void run() {
		// pass main thread to thread pool
		try {
			xThreadPool.getMainPool()
				.run();
		} catch (Exception e) {
			this.log().trace(e);
			Failure.fail("Problem running main thread pool!");
		}
	}



	// lock file
	@xAppStep(type=StepType.STARTUP, title="LockFile", priority=3)
	public void __STARTUP_lockfile() {
		final String filename = this.getName()+".lock";
		if(LockFile.get(filename) == null) {
			Failure.fail("Failed to get lock on file: "+filename);
			return;
		}
	}



	// scheduler
	@xAppStep(type=StepType.STARTUP, title="Scheduler", priority=75)
	public void __STARTUP_scheduler() {
		xScheduler.get()
			.Start();
	}



	// ------------------------------------------------------------------------------- //
	// shutdown



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



	// lock file
	@xAppStep(type=StepType.SHUTDOWN, title="LockFile", priority=3)
	public void __SHUTDOWN_lockfile() {
		final String filename = this.getName()+".lock";
		final LockFile lock = LockFile.peak(filename);
		if(lock != null)
			lock.release();
	}



	// ------------------------------------------------------------------------------- //



	// mvn properties
	@Override
	public String getName() {
		return this.mvnprops.name;
	}
	@Override
	public String getTitle() {
		return this.mvnprops.title;
	}
	@Override
	public String getFullTitle() {
		return this.mvnprops.full_title;
	}
	@Override
	public String getVersion() {
		return this.mvnprops.version;
	}
	@Override
	public String getURL() {
		return this.mvnprops.url;
	}
	@Override
	public String getOrgName() {
		return this.mvnprops.org_name;
	}
	@Override
	public String getOrgURL() {
		return this.mvnprops.org_url;
	}
	@Override
	public String getIssueName() {
		return this.mvnprops.issue_name;
	}
	@Override
	public String getIssueURL() {
		return this.mvnprops.issue_url;
	}



	// ------------------------------------------------------------------------------- //



	// ascii header
	@Override
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



}
