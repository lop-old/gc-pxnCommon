package com.poixson.app;

import java.io.PrintStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.fusesource.jansi.Ansi;

import com.poixson.app.steps.xAppStep;
import com.poixson.app.steps.xAppStep.StepType;
import com.poixson.app.steps.xAppStepDAO;
import com.poixson.utils.AppProps;
import com.poixson.utils.Failure;
import com.poixson.utils.HangCatcher;
import com.poixson.utils.Keeper;
import com.poixson.utils.LockFile;
import com.poixson.utils.StringUtils;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.xClock;
import com.poixson.utils.xStartable;
import com.poixson.utils.xTime;
import com.poixson.utils.xVars;
import com.poixson.utils.xLogger.xLevel;
import com.poixson.utils.xLogger.xLog;
import com.poixson.utils.xLogger.xLogFormatter_Color;
import com.poixson.utils.xLogger.xLogHandlerConsole;
import com.poixson.utils.xLogger.xLogPrintStream;
import com.poixson.utils.xThreadPool.xThreadPool;
import com.poixson.utils.xThreadPool.xThreadPoolFactory;


/*
 * Startup sequence
 *   10  prevent root
 *   50  load main configs
 *   60  sync clock
 *   80  display logo
 *   90  lock file
 *  100  start thread pools
 *  150  start scheduler
 * Shutdown sequence
 *  150  stop scheduler
 *  100  stop thread pools
 *   60  display uptime
 *   30  stop console input
 *   20  release lock file
 *   10  final garpage collect
 */
public abstract class xApp implements xStartable {
	private static final String APP_ALREADY_STARTED_EXCEPTION    = "Cannot init app, already inited!";
	private static final String APP_ALREADY_STOPPING_EXCEPTION   = "Cannot start app, already stopping!";
	private static final String APP_INVALID_STATE_EXCEPTION      = "Invalid state, cannot start: ";
	private static final String APP_INCONSISTENT_STATE_EXCEPTION = "Failed to start, inconsistent state!";
	private static final String APP_INCONSISTENT_STOP_EXCEPTION  = "Failed to stop, inconsistent state!";

	// app instance
	protected static volatile xApp instance = null;
	protected static final Object instanceLock = new Object();

	// state
	protected final AtomicInteger step = new AtomicInteger(0);
	protected static final int STEP_OFF   = 0;
	protected static final int STEP_START = 1;
	protected static final int STEP_STOP  = Integer.MIN_VALUE;
	protected static final int STEP_RUN   = Integer.MAX_VALUE;

	protected volatile xTime startTime = null;

	// mvn properties
	protected final AppProps props;

	// just to prevent gc
	@SuppressWarnings("unused")
	private static final Keeper keeper = Keeper.get();



	public static xApp get() {
		return instance;
	}
	public static xApp peak() {
		return instance;
	}



	public xApp() {
		if (instance != null) {
			xLog.getRoot()
				.trace(new RuntimeException(APP_ALREADY_STARTED_EXCEPTION));
			Failure.fail(
				APP_ALREADY_STARTED_EXCEPTION,
				new RuntimeException(APP_ALREADY_STARTED_EXCEPTION)
			);
		}
		synchronized(instanceLock) {
			if (instance != null) {
				xLog.getRoot()
					.trace(new RuntimeException(APP_ALREADY_STARTED_EXCEPTION));
				Failure.fail(
					APP_ALREADY_STARTED_EXCEPTION,
					new RuntimeException(APP_ALREADY_STARTED_EXCEPTION)
				);
			}
			instance = this;
		}
		this.props = new AppProps(this.getClass());
	}



	@Override
	public void Start() {
		// already starting or running
		if (this.isRunning() || this.isStarting()) {
			return;
		}
		synchronized(instanceLock) {
			if (this.isRunning() || this.isStarting()) {
				return;
			}
			// already stopping
			if (this.isStopping()) {
				this.log().warning(APP_ALREADY_STOPPING_EXCEPTION);
				return;
			}
			// set starting state
			if (!this.step.compareAndSet(STEP_OFF, STEP_START)) {
				this.log().warning(APP_INVALID_STATE_EXCEPTION+this.step.get());
				return;
			}
		}

		// init logger
		{
			final xLog log = xLog.getRoot();
			log.setLevel(xLevel.ALL);
			if (Failure.hasFailed()) {
				xVars.getOriginalOut()
					.println("Failure, pre-init!");
				System.exit(1);
			}
			// initialize console and enable colors
			if (System.console() != null) {
				if (!Utils.isJLineAvailable()) {
					Failure.fail("jline library not found");
				}
//TODO: detect when no console color is supported
				// enable console color
				xLog.getRoot()
					.setFormatter(
						new xLogFormatter_Color(),
						xLogHandlerConsole.class
					);
			}
		}
//		// process command line arguments
//		final List<String> argsList = new LinkedList<String>();
//		argsList.addAll(Arrays.asList(args));
//		instance.processArgs(argsList);
//		instance.processDefaultArgs(argsList);
//		if (utils.notEmpty(argsList)) {
//			final StringBuilder str = new StringBuilder();
//			for (final String arg : argsList) {
//				if (utils.isEmpty(arg)) continue;
//				if (str.length() > 0)
//					str.append(" ");
//				str.append(arg);
//			}
//			if (str.length() > 0) {
//				xVars.getOriginalOut()
//					.println("Unknown arguments: "+str.toString());
//				System.exit(1);
//				return;
//			}
//		}

//		// handle command-line arguments
//		instance.displayStartupVars();
//		// main thread ended
//		Failure.fail("@|FG_RED Main process ended! (this shouldn't happen)|@");
//		System.exit(1);

		this.log().publish();
		this.log().title(
			(new StringBuilder())
				.append("Starting ")
				.append(this.getTitle())
				.append("..")
				.toString()
		);
		// prepare startup steps
		final Map<Integer, List<xAppStepDAO>> orderedSteps =
				getSteps(StepType.STARTUP);
		final int highestStep = findHighestPriorityStep(orderedSteps);
		// hang catcher
		final HangCatcher hangCatcher = new HangCatcher(
			"10s",
			"100n"
		);
		hangCatcher.Start();
		// startup loop
		final PrintStream out = xVars.getOriginalOut();
		while (true) {
			if (!this.isStarting()) {
				Failure.fail(APP_INCONSISTENT_STATE_EXCEPTION,
						new RuntimeException(APP_INCONSISTENT_STATE_EXCEPTION));
			}
			// invoke step
			final int stepInt = this.step.get();
			final List<xAppStepDAO> lst = orderedSteps.get( new Integer(stepInt) );
			if (lst != null) {
//				this.log().fine(
//					(new StringBuilder())
//						.append("Startup Step ")
//						.append(stepInt)
//						.append(" ..")
//						.toString()
//				);
				boolean hasInvoked = false;
				for (final xAppStepDAO dao : lst) {
					try {
						dao.invoke();
						hasInvoked = true;
					} catch (ReflectiveOperationException e) {
						Failure.fail("Failed to invoke startup step!", e);
					} catch (RuntimeException e) {
						Failure.fail("Failed to invoke startup step!", e);
					}
				}
				// finished step
				if (hasInvoked) {
					out.flush();
					// sleep a short bit
					if (xVars.debug()) {
						ThreadUtils.Sleep(20L);
					}
				}
			}
			// finished starting
			if (stepInt >= highestStep) {
				break;
			}
			this.step.incrementAndGet();
		}
		hangCatcher.Stop();
		if (!this.isStarting()) {
			Failure.fail(APP_INCONSISTENT_STATE_EXCEPTION,
					new RuntimeException(APP_INCONSISTENT_STATE_EXCEPTION));
		}
		// finished starting
		this.step.set(STEP_RUN);
	}
	@Override
	public void Stop() {
		// already stopping or stopped
		if (this.isStopped())  return;
		if (this.isStopping()) return;
		synchronized(instanceLock) {
			if (this.isStopped())  return;
			if (this.isStopping()) return;
			// set stopping state
			this.step.set(STEP_STOP);
		}
		this.log().title(
			new String[] {
				(new StringBuilder())
					.append("Stopping ")
					.append(this.getTitle())
					.append("..")
					.toString(),
				(new StringBuilder())
					.append("Uptime: ")
					.append(this.getUptimeString())
					.toString()
			}
		);
		// prepare shutdown steps
		final Map<Integer, List<xAppStepDAO>> orderedSteps =
				getSteps(StepType.SHUTDOWN);
		final int highestStep = findHighestPriorityStep(orderedSteps);
		this.step.set( 0 - highestStep );
		// hang catcher
		final HangCatcher hangCatcher = new HangCatcher(
			"10s",
			"100n"
		);
		hangCatcher.Start();
		// shutdown loop
		final PrintStream out = xVars.getOriginalOut();
		while (true) {
			if (!this.isStopping()) {
				Failure.fail(APP_INCONSISTENT_STOP_EXCEPTION,
						new RuntimeException(APP_INCONSISTENT_STOP_EXCEPTION));
			}
			// invoke step
			final int stepInt = this.step.get();
			final List<xAppStepDAO> lst = orderedSteps.get( new Integer(0 - stepInt) );
			if (lst != null) {
//				this.log().fine(
//					(new StringBuilder())
//						.append("Shutdown Step ")
//						.append(stepInt)
//						.append(" ..")
//						.toString()
//				);
				boolean hasInvoked = false;
				for (final xAppStepDAO dao : lst) {
					try {
						dao.invoke();
						hasInvoked = true;
					} catch (ReflectiveOperationException e) {
						Failure.fail("Failed to invoke shutdown step!", e);
					} catch (RuntimeException e) {
						Failure.fail("Failed to invoke shutdown step!", e);
					}
				}
				// finished step
				if (hasInvoked) {
					out.flush();
					// sleep a short bit
					if (xVars.debug()) {
						ThreadUtils.Sleep(20L);
					}
				}
			}
			// finished stopping
			if (stepInt >= STEP_OFF - 1) {
				break;
			}
			this.step.incrementAndGet();
		}
		hangCatcher.Stop();
		if (!this.isStopping()) {
			Failure.fail(APP_INCONSISTENT_STOP_EXCEPTION,
					new RuntimeException(APP_INCONSISTENT_STOP_EXCEPTION));
		}
		// finished stopping
		this.step.set(STEP_OFF);
	}



	protected static Map<Integer, List<xAppStepDAO>> getSteps(final StepType type) {
		final Map<Integer, List<xAppStepDAO>> orderedSteps =
				new HashMap<Integer, List<xAppStepDAO>>();
		final List<xAppStepDAO> steps = FindAllSteps();
		for (final xAppStepDAO dao : steps) {
			if (!dao.isType(type)) continue;
			List<xAppStepDAO> lst = orderedSteps.get(
				new Integer(dao.priority)
			);
			// add new list to map
			if (lst == null) {
				lst = new LinkedList<xAppStepDAO>();
				orderedSteps.put(
					new Integer(dao.priority),
					lst
				);
			}
			lst.add(dao);
		}
		return orderedSteps;
	}
	protected static List<xAppStepDAO> FindAllSteps() {
		final Class<? extends xApp> clss = instance.getClass();
		if (clss == null) throw new RuntimeException("Failed to get app class!");
		// get method annotations
		final Method[] methods = clss.getMethods();
		if (Utils.isEmpty(methods))
			throw new RuntimeException("Failed to get app methods!");
		final List<xAppStepDAO> steps = new LinkedList<xAppStepDAO>();
		for (final Method m : methods) {
			final xAppStep anno = m.getAnnotation(xAppStep.class);
			if (anno == null) continue;
			// found step method
			final xAppStepDAO dao =
				new xAppStepDAO(
					instance,
					m,
					anno
				);
			steps.add(dao);
		}
		return steps;
	}
	protected static int findHighestPriorityStep(final Map<Integer, List<xAppStepDAO>> steps) {
		int highest = 0;
		for (final Integer key : steps.keySet()) {
			if (key.intValue() > highest) {
				highest = key.intValue();
			}
		}
		return highest;
	}



	@Override
	public void run() {
		throw new UnsupportedOperationException();
	}



	@Override
	public boolean isRunning() {
		return (this.step.get() == STEP_RUN);
	}
	public boolean isStarting() {
		final int step = this.step.get();
		return (step > STEP_OFF && step < STEP_RUN);
	}
	public boolean isStopping() {
		return (this.step.get() < STEP_OFF);
	}
	public boolean isStopped() {
		return (this.step.get() == STEP_OFF);
	}



//TODO:
//	public long getUptime() {
//		if (this.startTime == -1)
//			return 0;
//		return xClock.get(true).millis() - this.startTime;
//	}
	public String getUptimeString() {
return "<uptime>";
//		final xTime time = xTime.get(this.getUptime());
//		if (time == null)
//			return null;
//		return time.toFullString();
	}



	// mvn properties
	public String getName() {
		return this.props.name;
	}
	public String getTitle() {
		return this.props.title;
	}
	public String getFullTitle() {
		return this.props.full_title;
	}
	public String getVersion() {
		return this.props.version;
	}
	public String getCommitHash() {
		final String hash = this.getCommitHashFull();
		if (Utils.isEmpty(hash))
			return "N/A";
		return hash.substring(0, 7);
	}
	public String getCommitHashFull() {
		return this.props.commitHash;
	}
	public String getURL() {
		return this.props.url;
	}
	public String getOrgName() {
		return this.props.org_name;
	}
	public String getOrgURL() {
		return this.props.org_url;
	}
	public String getIssueName() {
		return this.props.issue_name;
	}
	public String getIssueURL() {
		return this.props.issue_url;
	}



	// ------------------------------------------------------------------------------- //
	// startup steps



	// ensure not root
	@xAppStep(type=StepType.STARTUP, title="RootCheck", priority=10)
	public void __STARTUP_rootcheck() {
		final String user = System.getProperty("user.name");
		if ("root".equals(user)) {
			this.log().warning("It is recommended to run as a non-root user");
		} else
		if ("administrator".equalsIgnoreCase(user) || "admin".equalsIgnoreCase(user)) {
			this.log().warning("It is recommended to run as a non-administrator user");
		}
	}



	// load configs
	@xAppStep(type=StepType.STARTUP, title="Configs", priority=50)
	public void __STARTUP_configs() {
	}



	// clock
	@xAppStep(type=StepType.STARTUP, title="Clock", priority=60)
	public void __STARTUP_clock() {
		final xClock clock = xClock.get(true);
		this.startTime =
			xTime.get(
				clock.millis()
			);
	}



	// display logo
	@xAppStep(type=StepType.STARTUP, title="DisplayLogo", priority=80)
	public void __STARTUP_displaylogo() {
		this.displayLogo();
//		displayStartupVars();
	}



	// lock file
	@xAppStep(type=StepType.STARTUP, title="LockFile", priority=90)
	public void __STARTUP_lockfile() {
		final String filename = this.getName()+".lock";
		if (LockFile.get(filename) == null) {
			Failure.fail("Failed to get lock on file: "+filename);
		}
	}



	// start thread pools
	@xAppStep(type=StepType.STARTUP, title="ThreadPools", priority=100)
	public void __STARTUP_threadpools() {
		final xThreadPool pool =
				xThreadPoolFactory.getMainPool();
		pool.Start();
	}



	// start scheduler
	@xAppStep(type=StepType.STARTUP, title="Scheduler", priority=150)
	public void __STARTUP_scheduler() {
//TODO:
//		xScheduler.get()
//			.Start();
	}



	// ------------------------------------------------------------------------------- //
	// shutdown steps



	// stop scheduler
	@xAppStep(type=StepType.SHUTDOWN, title="Scheduler", priority=150)
	public void __SHUTDOWN_scheduler() {
//TODO:
//		xScheduler.get()
//			.Stop();
	}



	// stop thread pools
	@xAppStep(type=StepType.SHUTDOWN, title="ThreadPools", priority=100)
	public void __SHUTDOWN_threadpools() {
		xThreadPoolFactory
			.ShutdownAll();
	}



	// display uptime
	@xAppStep(type=StepType.SHUTDOWN, title="Uptime", priority=60)
	public void __SHUTDOWN_uptimestats() {
//TODO: display total time running
//this.getUptimeString();
	}



	// stop console input
	@xAppStep(type=StepType.SHUTDOWN, title="Console", priority=30)
	public void __SHUTDOWN_console() {
		xLog.Shutdown();
	}



	// release lock file
	@xAppStep(type=StepType.SHUTDOWN, title="LockFile", priority=20)
	public void __SHUTDOWN_lockfile() {
//TODO:
//		final String filename = this.getName()+".lock";
//		final LockFile lock = LockFile.peak(filename);
//		if (lock != null) {
//			lock.release();
//		}
	}



	// garbage collect
	@xAppStep(type=StepType.SHUTDOWN,title="GarbageCollect", priority=10)
	public void __SHUTDOWN_gc() {
//TODO:
//		Utils.Sleep(250L);
//		xScheduler.clearInstance();
		System.gc();
//		final xLog log = this.log();
//		if (xScheduler.hasLoaded()) {
//			log.warning("xScheduler hasn't fully unloaded!");
//		} else {
//			log.finest("xScheduler has been unloaded");
//		}
	}



	// ------------------------------------------------------------------------------- //



	// logger
	private volatile SoftReference<xLog> _log = null;
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null) {
				return log;
			}
		}
		final xLog log = xLog.getRoot();
		this._log = new SoftReference<xLog>(log);
		return log;
	}



	protected static void DisplayLineColors(final PrintStream out,
			final Map<Integer, String> colors, final String line) {
		final StringBuilder buffer = new StringBuilder();
		int last = 0;
		boolean hasColor = false;
		for (final Entry<Integer, String> entry : colors.entrySet()) {
			final int pos = entry.getKey().intValue() - 1;
			if (pos > last) {
				buffer.append(
					line.substring(last, pos)
				);
			}
			last = pos;
			if (hasColor) {
				buffer.append("|@");
			}
			hasColor = true;
			buffer
				.append("@|")
				.append(entry.getValue())
				.append(" ");
		}
		if (last < line.length()) {
			buffer.append(line.substring(last));
		}
		if (hasColor) {
			buffer.append("|@");
		}
		out.println(
			Ansi.ansi().a(" ")
				.render(buffer.toString())
				.reset().a(" ")
		);
	}



//	protected void displayTestColors() {
//		final PrintStream out = AnsiConsole.out;
//		out.println(Ansi.ansi().reset());
//		for (final Ansi.Color color : Ansi.Color.values()) {
//			final String name = Strings.padCenter(7, color.name(), ' ');
//			out.println(Ansi.ansi()
//				.a("   ")
//				.fg(color).a(name)
//				.a("   ")
//				.bold().a("BOLD-"+name)
//				.a("   ")
//				.boldOff().fg(Ansi.Color.WHITE).bg(color).a(name)
//				.reset()
//			);
//		}
//		out.println(Ansi.ansi().reset());
//		out.println();
//		out.flush();
//	}



//	public void displayStartupVars() {
//		final PrintStream out = AnsiConsole.out;
//		final String hash;
//		out.println();
//		out.println(" Pid: "+Proc.getPid());
//		out.println(" Version: "+this.getVersion());
//		out.println(" Commit:  "+this.getCommitHash());
//		out.println(" Running as:  "+System.getProperty("user.name"));
//		out.println(" Current dir: "+System.getProperty("user.dir"));
//		out.println(" java home:   "+System.getProperty("java.home"));
//		out.println(" Terminal:    "+System.getProperty("jline.terminal"));
//		if (xVars.debug())
//			out.println(" Debug: true");
//		if (Utils.notEmpty(args)) {
//			out.println();
//			out.println(utilsString.addStrings(" ", args));
//		}
//		out.println();
//		out.flush();
//	}



	// ascii header
	protected void displayLogo() {
		// colors
		final String COLOR_PXN_P       = "bold,green";
		final String COLOR_PXN_OI      = "bold,blue";
		final String COLOR_PXN_X       = "bold,green";
		final String COLOR_PXN_SON     = "bold,blue";
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
		colors2.put(new Integer(21), COLOR_PXN_P);
		colors2.put(new Integer(22), COLOR_PXN_OI);
		colors2.put(new Integer(24), COLOR_PXN_X);
		colors2.put(new Integer(25), COLOR_PXN_SON);
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
		colors6.put(new Integer(27), COLOR_FROG_EYES);
		colors6.put(new Integer(28), COLOR_FROG);
		colors6.put(new Integer(30), COLOR_FROG_EYES);
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

		// build lines
		final String version = StringUtils.padCenter(15, this.getVersion(), ' ');
		final PrintStream out = new xLogPrintStream(
				xLog.getRoot(),
				null
		);
		out.println();
		DisplayLineColors(out, colors1, "                                     _/\\_                        "    );
		DisplayLineColors(out, colors2, "         |`-.__     PoiXson          (('>         _   _          "     );
		DisplayLineColors(out, colors3, "         / ' _/    Software     _    /^|         /\\\\_/ \\         "  );
		DisplayLineColors(out, colors4, "       -****\\\"  "+version+" =>--/_\\|m---    / 0  0  \\        "     );
		DisplayLineColors(out, colors5, "      /    }                         ^^        /_   v   _\\       "    );
		DisplayLineColors(out, colors6, "     /    \\               @..@                   \\__^___/        "   );
		DisplayLineColors(out, colors7, " \\ /`    \\\\\\             (----)                  /  0    \\       ");
		DisplayLineColors(out, colors8, "  `\\     /_\\\\           ( >__< )                /        \\__     " );
		DisplayLineColors(out, colors9, "   `~~~~~~``~`          ^^ ~~ ^^                \\_(_|_)___  \\    "   );
		DisplayLineColors(out, colors10,"^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^(____//^/^"    );
		DisplayLineColors(out, colors11,"/////////////////////////////////////////////////////////////////"    );
		out.println();
		out.println(" This program comes with absolutely no warranty. This is free");
		out.println(" software and you are welcome to modify it or redistribute it");
		out.println(" under certain conditions. Type 'show license' at the command");
		out.println(" prompt for license details, or go to www.growcontrol.com for");
		out.println(" more information.");
		out.println();
		out.flush();
	}
//  |        A                B            C             D            |
//1 |                                     _/\_                        |
//2 |         |`-.__     PoiXson          (('>         _   _          |
//3 |         / ' _/    Software     _    /^|         /\\_/ \         |
//4 |       -****\"  <---version---> =>--/__|m---    / 0  0  \        |
//5 |      /    }                         ^^        /_   v   _\       |
//6 |     /    \               @..@                   \__^___/        |
//7 | \ /`    \\\             (----)                  /  0    \       |
//8 |  `\     /_\\           ( >__< )                /        \__     |
//9 |   `~~~~~~``~`          ^^ ~~ ^^                \_(_|_)___  \    |
//10 |^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^(____//^/^|
//11 |/////////////////////////////////////////////////////////////////|
//  0 2 4 6 8 0 2 4 6 8 0 2 4 6 8 0 2 4 6 8 0 2 4 6 8 0 2 4 6 8 0 2 4 |
//  0         1         2         3         4         5         6     |



}
