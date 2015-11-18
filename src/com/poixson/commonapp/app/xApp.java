package com.poixson.commonapp.app;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.fusesource.jansi.AnsiConsole;

import com.poixson.commonapp.app.annotations.xAppStep;
import com.poixson.commonapp.app.annotations.xAppStep.StepType;
import com.poixson.commonjava.Failure;
import com.poixson.commonjava.Utils.LockFile;
import com.poixson.commonjava.Utils.appProps;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.utilsThread;
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
	protected final appProps props;



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
		instance.initConsole();
		// process command line arguments
		final List<String> argsList = new LinkedList<String>();
		argsList.addAll(Arrays.asList(args));
		instance.processArgs(argsList);
		instance.processDefaultArgs(argsList);
		if(utils.notEmpty(argsList)) {
			final StringBuilder str = new StringBuilder();
			for(final String arg : argsList) {
				if(utils.isEmpty(arg))
					continue;
				if(str.length() > 0)
					str.append(" ");
				str.append(arg);
			}
			if(str.length() > 0) {
				System.out.println("Unknown arguments: "+str.toString());
				System.exit(1);
				return;
			}
		}
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
		this.props = appProps.get(this.getClass());
	}



	public xCommandsHandler getCommandsHandler() {
		return null;
	}



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



	// ------------------------------------------------------------------------------- //
	// startup



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



	// garbage collect
	@xAppStep(type=StepType.SHUTDOWN,title="GarbageCollect", priority=1)
	public void __SHUTDOWN_gc() {
		utilsThread.Sleep(250L);
		xScheduler.clearInstance();
		System.gc();
		final xLog log = this.log();
		if(xScheduler.hasLoaded())
			log.warning("xScheduler hasn't fully unloaded!");
		else
			log.finest("xScheduler has been unloaded");

	}



	// ------------------------------------------------------------------------------- //



	// mvn properties
	@Override
	public String getName() {
		return this.props.name;
	}
	@Override
	public String getTitle() {
		return this.props.title;
	}
	@Override
	public String getFullTitle() {
		return this.props.full_title;
	}
	@Override
	public String getVersion() {
		return this.props.version;
	}
	@Override
	public String getCommitHash() {
		final String hash = this.getCommitHashFull();
		if(utils.isEmpty(hash))
			return "N/A";
		return hash.substring(0, 7);
	}
	@Override
	public String getCommitHashFull() {
		return this.props.commitHash;
	}
	@Override
	public String getURL() {
		return this.props.url;
	}
	@Override
	public String getOrgName() {
		return this.props.org_name;
	}
	@Override
	public String getOrgURL() {
		return this.props.org_url;
	}
	@Override
	public String getIssueName() {
		return this.props.issue_name;
	}
	@Override
	public String getIssueURL() {
		return this.props.issue_url;
	}



	// ------------------------------------------------------------------------------- //



	// ascii header
	@Override
	protected void displayLogo() {
		// colors
		final String COLOR_POIXSON_P   = "bold,green";
		final String COLOR_POIXSON_OI  = "bold,blue";
		final String COLOR_POIXSON_X   = "bold,green";
		final String COLOR_POIXSON_SON = "bold,blue";
		final String COLOR_SOFTWARE    = "bold,black";
		final String COLOR_VERSION     = "cyan";
		final String COLOR_GRASS       = "green";
		final String COLOR_DOG         = "yellow";
		final String COLOR_DOG_EYES    = "cyan";
		final String COLOR_DOG_MOUTH   = "red";
		final String COLOR_DOG_COLLAR  = "red";
		final String COLOR_DOG_NOSE    = "bold,black";
		final String COLOR_FROG        = "green";
		final String COLOR_FROG_EYES   = "bold,black";
		final String COLOR_WITCH       = "bold,black";
		final String COLOR_WITCH_EYES  = "red";
		final String COLOR_WITCH_BROOM = "yellow";
		final String COLOR_CAT         = "white";
		final String COLOR_CAT_EYES    = "white";
		final String COLOR_CAT_MOUTH   = "red";
		final String COLOR_CAT_COLLAR  = "blue";
		final String COLOR_CAT_NOSE    = "bold,black";
		// line 1
		final Map<Integer, String> colors1 = new LinkedHashMap<Integer, String>();
		colors1.put(new Integer(38), COLOR_WITCH);
		// line 2
		final Map<Integer, String> colors2 = new LinkedHashMap<Integer, String>();
		colors2.put(new Integer(10), COLOR_DOG);
		colors2.put(new Integer(21), COLOR_POIXSON_P);
		colors2.put(new Integer(22), COLOR_POIXSON_OI);
		colors2.put(new Integer(24), COLOR_POIXSON_X);
		colors2.put(new Integer(25), COLOR_POIXSON_SON);
		colors2.put(new Integer(38), COLOR_WITCH);
		colors2.put(new Integer(40), COLOR_WITCH_EYES);
		colors2.put(new Integer(41), COLOR_WITCH);
		colors2.put(new Integer(51), COLOR_CAT);
		// line 3
		final Map<Integer, String> colors3 = new LinkedHashMap<Integer, String>();
		colors3.put(new Integer(10), COLOR_DOG);
		colors3.put(new Integer(12), COLOR_DOG_EYES);
		colors3.put(new Integer(14), COLOR_DOG_MOUTH);
		colors3.put(new Integer(15), COLOR_DOG_NOSE);
		colors3.put(new Integer(20), COLOR_SOFTWARE);
		colors3.put(new Integer(33), COLOR_WITCH_BROOM);
		colors3.put(new Integer(38), COLOR_WITCH);
		colors3.put(new Integer(50), COLOR_CAT);
		// line 4
		final Map<Integer, String> colors4 = new LinkedHashMap<Integer, String>();
		colors4.put(new Integer(8),  COLOR_DOG);
		colors4.put(new Integer(9),  COLOR_DOG_COLLAR);
		colors4.put(new Integer(13), COLOR_DOG_NOSE);
		colors4.put(new Integer(14), COLOR_DOG_MOUTH);
		colors4.put(new Integer(17), COLOR_VERSION);
		colors4.put(new Integer(33), COLOR_WITCH_BROOM);
		colors4.put(new Integer(37), COLOR_WITCH);
		colors4.put(new Integer(42), COLOR_WITCH_BROOM);
		colors4.put(new Integer(49), COLOR_CAT);
		colors4.put(new Integer(51), COLOR_CAT_EYES);
		colors4.put(new Integer(57), COLOR_CAT);
		// line 5
		final Map<Integer, String> colors5 = new LinkedHashMap<Integer, String>();
		colors5.put(new Integer(7),  COLOR_DOG);
		colors5.put(new Integer(38), COLOR_WITCH);
		colors5.put(new Integer(48), COLOR_CAT);
		colors5.put(new Integer(53), COLOR_CAT_NOSE);
		colors5.put(new Integer(57), COLOR_CAT);
		// line 6
		final Map<Integer, String> colors6 = new LinkedHashMap<Integer, String>();
		colors6.put(new Integer(6),  COLOR_DOG);
		colors6.put(new Integer(25), COLOR_FROG_EYES);
		colors6.put(new Integer(26), COLOR_FROG);
		colors6.put(new Integer(28), COLOR_FROG_EYES);
		colors6.put(new Integer(50), COLOR_CAT);
		colors6.put(new Integer(53), COLOR_CAT_MOUTH);
		colors6.put(new Integer(54), COLOR_CAT);
		// line 7
		final Map<Integer, String> colors7 = new LinkedHashMap<Integer, String>();
		colors7.put(new Integer(2),  COLOR_DOG);
		colors7.put(new Integer(24), COLOR_FROG);
		colors7.put(new Integer(50), COLOR_CAT);
		colors7.put(new Integer(53), COLOR_CAT_COLLAR);
		colors7.put(new Integer(58), COLOR_CAT);
		// line 8
		final Map<Integer, String> colors8 = new LinkedHashMap<Integer, String>();
		colors8.put(new Integer(3),  COLOR_DOG);
		colors8.put(new Integer(23), COLOR_FROG);
		colors8.put(new Integer(49), COLOR_CAT);
		// line 9
		final Map<Integer, String> colors9 = new LinkedHashMap<Integer, String>();
		colors9.put(new Integer(4),  COLOR_DOG);
		colors9.put(new Integer(23), COLOR_FROG);
		colors9.put(new Integer(49), COLOR_CAT);
		// line 10
		final Map<Integer, String> colors10 = new LinkedHashMap<Integer, String>();
		colors10.put(new Integer(1),  COLOR_GRASS);
		colors10.put(new Integer(56), COLOR_CAT);
		colors10.put(new Integer(62), COLOR_GRASS);
		// line 11
		final Map<Integer, String> colors11 = new LinkedHashMap<Integer, String>();
		colors11.put(new Integer(1),  COLOR_GRASS);
		// build art
		final String version = utilsString.padCenter(15, this.props.version, ' ');
		final PrintStream out = AnsiConsole.out;
		out.println();
		this.displayLogoLine(out, colors1, "                                     _/\\_                        "    );
		this.displayLogoLine(out, colors2, "         |`-.__     PoiXson          (('>         _   _          "     );
		this.displayLogoLine(out, colors3, "         / ' _/    Software     _    /^|         /\\\\_/ \\         "  );
		this.displayLogoLine(out, colors4, "       -****\\\"  "+version+" =>--/_\\|m---    / 0  0  \\        "     );
		this.displayLogoLine(out, colors5, "      /    }                         ^^        /_   v   _\\       "    );
		this.displayLogoLine(out, colors6, "     /    \\             @..@                     \\__^___/        "   );
		this.displayLogoLine(out, colors7, " \\ /`    \\\\\\           (----)                    /  0    \\       ");
		this.displayLogoLine(out, colors8, "  `\\     /_\\\\         ( >__< )                  /        \\__     " );
		this.displayLogoLine(out, colors9, "   `~~~~~~``~`        ^^ ~~ ^^                  \\_(_|_)___  \\    "   );
		this.displayLogoLine(out, colors10,"^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^(____//^/^"    );
		this.displayLogoLine(out, colors11,"/////////////////////////////////////////////////////////////////"    );
		out.println();
		out.flush();
	}

//   |        A                B            C             D            |
// 1 |                                     _/\_                        |
// 2 |         |`-.__     PoiXson          (('>         _   _          |
// 3 |         / ' _/    Software     _    /^|         /\\_/ \         |
// 4 |       -****\"  <---version---> =>--/__|m---    / 0  0  \        |
// 5 |      /    }                         ^^        /_   v   _\       |
// 6 |     /    \             @..@                     \__^___/        |
// 7 | \ /`    \\\           (----)                    /  0    \       |
// 8 |  `\     /_\\         ( >__< )                  /        \__     |
// 9 |   `~~~~~~``~`        ^^ ~~ ^^                  \_(_|_)___  \    |
//10 |^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^(____//^/^|
//11 |/////////////////////////////////////////////////////////////////|
//   0 2 4 6 8 0 2 4 6 8 0 2 4 6 8 0 2 4 6 8 0 2 4 6 8 0 2 4 6 8 0 2 4 |
//   0         1         2         3         4         5         6     |



}
