package com.poixson.app;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.poixson.abstractions.xStartable;
import com.poixson.app.xAppStep.StepType;
import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.AttachedLogger;
import com.poixson.logger.xLevel;
import com.poixson.logger.xLog;
import com.poixson.logger.xLogPrintStream;
import com.poixson.logger.xLogRoot;
import com.poixson.threadpool.xThreadPool;
import com.poixson.threadpool.types.xThreadPool_Main;
import com.poixson.tools.AppProps;
import com.poixson.tools.AsciiArtBuilder;
import com.poixson.tools.HangCatcher;
import com.poixson.tools.Keeper;
import com.poixson.tools.xClock;
import com.poixson.tools.xTime;
import com.poixson.tools.xTimeU;
import com.poixson.tools.remapped.xRunnable;
import com.poixson.utils.ProcUtils;
import com.poixson.utils.StringUtils;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.Utils;


/*
 * Startup sequence
 *   10  prevent root
 *   50  load main configs
 *   60  sync clock
 *   70  display logo
 *   80  lock file
 *   90  start console input
 *  100  start aux thread pools
 *  150  start schedulers
 * Shutdown sequence
 *  150  stop schedulers
 *  100  stop aux thread pools
 *   60  display uptime
 *   30  stop console input
 *   20  release lock file
 *   10  final garpage collect
 */
public abstract class xApp implements xStartable, AttachedLogger {

	protected static final int STATE_OFF     = 0;
	protected static final int STATE_START   = 1;
	protected static final int STATE_STOP    = Integer.MIN_VALUE + 1;
	protected static final int STATE_RUNNING = Integer.MAX_VALUE;

	// startup/shutdown steps
	protected final AtomicInteger state = new AtomicInteger(0);
	protected final HashMap<Integer, List<xAppStepDAO>> currentSteps =
			new HashMap<Integer, List<xAppStepDAO>>();
	protected final Object runLock = new Object();
	protected volatile HangCatcher hangCatcher = null;

	protected final xTime startTime = xTime.getNew();

	// mvn properties
	protected final AppProps props;



	protected xApp() {
		this.props = new AppProps(this.getClass());
		Keeper.add(this);
	}



	protected abstract Object[] getStepObjects(final StepType type);



	// ------------------------------------------------------------------------------- //
	// start/stop app



	@Override
	public void start() {
		if (Failure.hasFailed()) return;
		// check state (should be 0 stopped)
		{
			final int stepInt = this.state.get();
			if (stepInt != STATE_OFF) {
				// <0 already stopping
				if (stepInt < STATE_OFF) {
					this.warning(
//						ERR_ALREADY_STOPPING_EXCEPTION,
						stepInt
					);
				}
				// >0 already starting or running
				return;
			}
		}
		// set starting state
		if ( ! this.state.compareAndSet(STATE_OFF, STATE_START)) {
			this.warning(
//				ERR_INVALID_STATE_EXCEPTION,
				this.state.get()
			);
			return;
		}
		if (Failure.hasFailed()) return;
		// start hang catcher
		this.startHangCatcher();
		// load startup steps
		{
			final xThreadPool_Main pool = xThreadPool_Main.get();
			pool.runTaskNow(
					new xRunnable("Load startup steps") {
						private volatile xThreadPool pool = null;
						public xRunnable init(final xThreadPool pool) {
							this.pool = pool;
							return this;
						}
						@Override
						public void run() {
							if (Failure.hasFailed()) return;
							final xApp app = xApp.this;
							app.publish();
							app.title("Starting {}..", app.getTitle());
							// prepare startup steps
							synchronized (app.currentSteps) {
								app.currentSteps.clear();
								app.loadSteps(StepType.STARTUP);
							}
							if (Failure.hasFailed()) return;
							// queue startup sequence
							final int stepInt = xApp.this.state.get();
							xApp.QueueNextStep(xApp.this, this.pool, stepInt);
						}
					}.init(pool)
				);
		}
	}
	@Override
	public void stop() {
	}



	public void join() {
		xThreadPool_Main.get()
			.joinWorkers();
	}



	// ------------------------------------------------------------------------------- //
	// startup/shutdown queue



	// run next step
	@Override
	public void run() {
		if (Failure.hasFailed()) return;
		synchronized (this.runLock) {
			// finished startup/shutdown
			if (this.currentSteps.isEmpty()) {
				this.stopHangCatcher();
				final int stepInt = this.state.get();
				if (stepInt == STATE_START || stepInt == STATE_STOP) {
					this.state.set(STATE_OFF);
					this.log()
						.severe(
							"No {} steps found!",
							(stepInt > STATE_OFF ? "startup" : "shutdown")
						);
				} else
				if (stepInt > STATE_OFF) {
					this.state.set(STATE_RUNNING);
					this.info("Finished startup!");
				} else {
					this.state.set(STATE_OFF);
					this.info("Finished shutdown.");
				}
				return;
			}
			// get current step
			final xAppStepDAO step = this.grabNextStepDAO();
			if (Failure.hasFailed()) return;
			final int stepInt = this.state.get();
			if (step != null) {
				if (this.log().isLoggable(xLevel.DETAIL)) {
					this.fine(
						"{} step {}.. {}",
						( stepInt > STATE_OFF ? "Startup" : "Shutdown" ),
						stepInt,
						step.title
					);
				}
				// run current step
				this.resetHangCatcher();
				step.run();
				if (Failure.hasFailed()) return;
				this.resetHangCatcher();
			}
			// queue next step
			final xThreadPool_Main pool = xThreadPool_Main.get();
			QueueNextStep(this, pool, stepInt);
		}
	}



	protected static void QueueNextStep(final xApp app,
			final xThreadPool pool, final int stepInt) {
		if (Failure.hasFailed()) return;
		pool.runTaskLater(
				(stepInt > STATE_OFF ? "Startup" : "Shutdown")
					+Integer.toString(stepInt),
				app
			);
	}
	protected xAppStepDAO grabNextStepDAO() {
		if (Failure.hasFailed()) return null;
		synchronized (this.currentSteps) {
			// is finished
			if (this.currentSteps.isEmpty())
				return null;
			final int stepInt = this.state.get();
			// check current step
			final List<xAppStepDAO> steps = this.currentSteps.get( Integer.valueOf(stepInt) );
			if (steps != null && !steps.isEmpty()) {
				// run next task in current step
				final xAppStepDAO nextStep;
				synchronized (this.currentSteps) {
					nextStep = steps.get(0);
					if (nextStep == null)
						throw new RuntimeException("Failed to get next startup step!");
					steps.remove(0);
				}
				return nextStep;
			}
			// find next step int
			this.currentSteps.remove( Integer.valueOf(stepInt) );
			if (this.currentSteps.isEmpty())
				return null;
			int nextStepInt;
			final Iterator<Integer> it = this.currentSteps.keySet().iterator();
			// startup
			if (stepInt > STATE_OFF) {
				nextStepInt = Integer.MAX_VALUE;
				while (it.hasNext()) {
					final int index = it.next().intValue();
					if (index < nextStepInt) {
						nextStepInt = index;
					}
				}
				// no steps left
				if (nextStepInt == Integer.MAX_VALUE)
					return null;
			// shutdown
			} else {
				nextStepInt = Integer.MIN_VALUE;
				while (it.hasNext()) {
					final int index = it.next().intValue();
					if (index > nextStepInt) {
						nextStepInt = index;
					}
				}
				// no steps left
				if (nextStepInt == Integer.MIN_VALUE)
					return null;
			}
			this.state.set(nextStepInt);
			return this.grabNextStepDAO();
		}
	}



	protected void loadSteps(final StepType type) {
		this.loadSteps(
			type,
			this.getStepObjects(type)
		);
	}
	protected void loadSteps(final StepType type, final Object[] containers) {
		for (final Object obj : containers) {
			this.loadSteps(type, obj);
		}
	}
	protected void loadSteps(final StepType type, final Object container) {
		if (type      == null) throw new RequiredArgumentException("type");
		if (container == null) throw new RequiredArgumentException("container");
		if (Failure.hasFailed()) return;
		synchronized (this.currentSteps) {
			// find annotations
			final Class<?> clss = container.getClass();
			if (clss == null) throw new RuntimeException("Failed to get app step container class!");
			final Method[] methods = clss.getMethods();
			if (Utils.isEmpty(methods)) throw new RuntimeException("Failed to get app methods!");
			for (final Method m : methods) {
				final xAppStep anno = m.getAnnotation(xAppStep.class);
				if (anno == null) continue;
				// found step method
				if (type.equals(anno.type())) {
					final xAppStepDAO dao =
						new xAppStepDAO(
							this,
							container,
							m,
							anno
						);
					// store the step
					final List<xAppStepDAO> list = new ArrayList<xAppStepDAO>();
					list.add(dao);
					final List<xAppStepDAO> existing =
						this.currentSteps.putIfAbsent(
							Integer.valueOf(dao.priority),
							list
						);
					if (existing != null)
						existing.add(dao);
				}
			}
		}
	}



	// ------------------------------------------------------------------------------- //
	// startup steps



	// clock
	@xAppStep(type=StepType.STARTUP, title="Clock", priority=60)
	public void __STARTUP_clock(final xApp app) {
		final xClock clock = xClock.get(true);
		this.startTime.set(
			clock.millis(),
			xTimeU.MS
		);
		this.startTime.lock();
	}



	// display logo
	@xAppStep(type=StepType.STARTUP, title="DisplayLogo", priority=70)
	public void __STARTUP_displaylogo(final xApp app) {
		this.displayLogo();
	}



	// ------------------------------------------------------------------------------- //
	// shutdown steps



	// display uptime
	@xAppStep(type=StepType.SHUTDOWN, title="Uptime", priority=60)
	public void __SHUTDOWN_uptimestats() {
//TODO: display total time running
	}



	// garbage collect
	@xAppStep(type=StepType.SHUTDOWN,title="GarbageCollect", priority=10)
	public void __SHUTDOWN_gc() {
		System.gc();
		xVars.getOriginalOut()
			.println();
	}



	// ------------------------------------------------------------------------------- //
	// hang catcher



	private void startHangCatcher() {
		if (ProcUtils.isDebugWireEnabled())
			return;
		final HangCatcher catcher =
			new HangCatcher(
				xTime.getNew("10s").getMS(),
				100L,
				new Runnable() {
					@Override
					public void run() {
						xApp.this.publish();
						xApp.this.publish(" *********************** ");
						xApp.this.publish(" *  Startup has hung!  * ");
						xApp.this.publish(" *********************** ");
						xApp.this.publish();
						ThreadUtils.DisplayStillRunning();
						System.exit(1);
					}
				}
		);
		catcher.start();
		this.hangCatcher = catcher;
	}
	private void resetHangCatcher() {
		final HangCatcher catcher = this.hangCatcher;
		if (catcher != null) {
			catcher.resetTimeout();
		}
	}
	private void stopHangCatcher() {
		final HangCatcher catcher = this.hangCatcher;
		if (catcher != null) {
			catcher.stop();
		}
	}



	// ------------------------------------------------------------------------------- //
	// state



	@Override
	public boolean isRunning() {
		return (this.state.get() == STATE_RUNNING);
	}
	public boolean isStarting() {
		return (this.state.get() > STATE_OFF);
	}
	@Override
	public boolean isStopping() {
		return (this.state.get() < STATE_OFF);
	}
	public boolean isStopped() {
		return (this.state.get() == STATE_OFF);
	}



	// ------------------------------------------------------------------------------- //
	// config



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
	// logger



	private final xLog _log;
	@Override
	public xLog log() {
		return this._log;
	}



	private static final String APP_ALREADY_STARTED_EXCEPTION    = "Cannot init app, already inited!";
	private static final String APP_ALREADY_STOPPING_EXCEPTION   = "Cannot start app, already stopping!";
	private static final String APP_INVALID_STATE_EXCEPTION      = "Invalid state, cannot start: {}";
	private static final String APP_INCONSISTENT_STATE_EXCEPTION = "Failed to start, inconsistent state!";
	private static final String APP_INCONSISTENT_STOP_EXCEPTION  = "Failed to stop, inconsistent state!";

	// app instance
	protected static final AtomicReference<xApp> instance =
			new AtomicReference<xApp>(null);

	// startup/shutdown steps
	protected final AtomicInteger step = new AtomicInteger(0);
	protected final HashMap<Integer, List<xAppStepDAO>> currentSteps =
			new HashMap<Integer, List<xAppStepDAO>>();
	protected volatile HangCatcher hangCatcher = null;

	protected static final int STEP_OFF      = 0;
	protected static final int STEP_START    = 1;
	protected static final int STEP_STOPPING = Integer.MIN_VALUE;
	protected static final int STEP_RUNNING  = Integer.MAX_VALUE;

	protected volatile xTime startTime = null;

	// mvn properties
	protected final AppProps props;

	// just to prevent gc
	@SuppressWarnings("unused")
	private static final Keeper keeper = Keeper.get();



	/**
	 * Get the app class instance.
	 * @return xApp instance object.
	 */
	public static xApp get() {
		return instance.get();
	}
	public static xApp peek() {
		return get();
	}



	public xApp() {
		if (!instance.compareAndSet(null, this)) {
			final RuntimeException e =
				new RuntimeException(APP_ALREADY_STARTED_EXCEPTION);
			this.trace(e);
			Failure.fail(APP_ALREADY_STARTED_EXCEPTION, e);
		}
		this.props = new AppProps(this.getClass());
	}



	@Override
	public void start() {
		// already starting or running
		if (this.isRunning() || this.isStarting()) {
			return;
		}
		// already stopping
		if (this.isStopping()) {
			this.warning(APP_ALREADY_STOPPING_EXCEPTION);
			return;
		}
		// set starting state
		if (!this.step.compareAndSet(STEP_OFF, STEP_START)) {
			this.warning(
				APP_INVALID_STATE_EXCEPTION,
				Integer.toString(this.step.get())
			);
			return;
		}
		// init logger
		this.initLogger();

//TODO:
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

		this.publish();
		this.title("Starting {}..", this.getTitle());
		// register shutdown hook
		xThreadPool.addShutdownHook(
			new RemappedMethod(
				this,
				"stop"
			)
		);
/*
		// prepare startup steps
		final Map<Integer, List<xAppStepDAO>> orderedSteps =
				getSteps(StepType.STARTUP);
		final int highestStep = findHighestPriorityStep(orderedSteps);
		// hang catcher
		final HangCatcher hangCatcher;
		if (ProcUtils.isDebugWireEnabled()) {
			hangCatcher = null;
		} else {
			hangCatcher = new HangCatcher(
				"10s",
				"100n"
			);
			hangCatcher.start();
		}
		// startup loop
		final PrintStream out = xVars.getOriginalOut();
		while (true) {
			if (!this.isStarting()) {
				Failure.fail(APP_INCONSISTENT_STATE_EXCEPTION,
						new RuntimeException(APP_INCONSISTENT_STATE_EXCEPTION));
			}
			// invoke step
			final int stepInt = this.step.get();
			final List<xAppStepDAO> lst =
				orderedSteps.get(
					new Integer(stepInt)
				);
			if (lst != null) {
				if (this.log().isLoggable(xLevel.DETAIL)) {
					final StringBuilder stepNames = new StringBuilder();
					for (final xAppStepDAO dao : lst) {
						if (stepNames.length() > 0)
							stepNames.append(", ");
						stepNames.append(dao.title);
					}
					this.detail(
						"Startup Step {}.. {}",
						Integer.valueOf(stepInt),
						stepNames.toString()
					);
				}
				boolean hasInvoked = false;
				for (final xAppStepDAO dao : lst) {
					try {
						dao.invoke();
						hasInvoked = true;
					} catch (ReflectiveOperationException e) {
						Failure.fail("Failed to invoke startup step: "+dao.title, e);
					} catch (RuntimeException e) {
						Failure.fail("Failed to invoke startup step: "+dao.title, e);
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
		if (hangCatcher != null) {
			hangCatcher.stop();
		}
		if (!this.isStarting()) {
			Failure.fail(APP_INCONSISTENT_STATE_EXCEPTION,
					new RuntimeException(APP_INCONSISTENT_STATE_EXCEPTION));
		}
		// finished starting
		this.step.set(STEP_RUN);
*/
	}
//TODO: ThreadUtils.displayStillRunning();
	@Override
	public void stop() {
/*
		// already stopping or stopped
		if (this.isStopped())  return;
		// get ready to stop thread pools
		xThreadPool.ShutdownAll();
		if (this.isStopping()) return;
		// set stopping state
		this.step.set(STEP_STOP);
		this.title(
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
//TODO: remove this, moved to ShutdownTask class
//		// hang catcher
//		final HangCatcher hangCatcher = new HangCatcher(
//			"10s",
//			"100n"
//		);
//		hangCatcher.start();
		// shutdown loop
		final PrintStream out = xVars.getOriginalOut();
		while (true) {
			if (!this.isStopping()) {
				Failure.fail(APP_INCONSISTENT_STOP_EXCEPTION,
						new RuntimeException(APP_INCONSISTENT_STOP_EXCEPTION));
			}
			// invoke step
			final int stepInt = this.step.get();
			final List<xAppStepDAO> lst =
				orderedSteps.get(
					new Integer(
						Math.abs(stepInt)
					)
				);
			if (lst != null) {
				if (this.log().isLoggable(xLevel.DETAIL)) {
					final StringBuilder stepNames = new StringBuilder();
					for (final xAppStepDAO dao : lst) {
						if (stepNames.length() > 0)
							stepNames.append(", ");
						stepNames.append(dao.title);
					}
					this.detail(
						"Shutdown Step {}.. {}",
						Integer.valueOf(stepInt),
						stepNames.toString()
					);
				}
				boolean hasInvoked = false;
				for (final xAppStepDAO dao : lst) {
					try {
						dao.invoke();
						hasInvoked = true;
					} catch (ReflectiveOperationException e) {
						Failure.fail("Failed to invoke shutdown step: "+dao.title, e);
					} catch (RuntimeException e) {
						Failure.fail("Failed to invoke shutdown step: "+dao.title, e);
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
		hangCatcher.stop();
		if (!this.isStopping()) {
			Failure.fail(APP_INCONSISTENT_STOP_EXCEPTION,
					new RuntimeException(APP_INCONSISTENT_STOP_EXCEPTION));
		}
		// finished stopping
		this.step.set(STEP_OFF);
*/
	}



	protected void initLogger() {
		final xLog log = xLog.getRoot();
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
			log.setHandler(
				new xLogHandlerConsole()
			);
			// enable console color
			log.setFormatter(
				new xLogFormatter_Color(),
				xLogHandlerConsole.class
			);
		}
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
		final xApp app = get();
		final Class<? extends xApp> clss = app.getClass();
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
					app,
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
		return (this.step.get() > 0);
	}
	public boolean isStarting() {
		final int step = this.step.get();
		return (step > STEP_OFF && step < STEP_RUNNING);
	}
	@Override
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



//TODO: move these functions to a new class
	// ------------------------------------------------------------------------------- //
	// startup steps



	// ensure not root
	@xAppStep(type=StepType.STARTUP, title="RootCheck", priority=10)
	public void __STARTUP_rootcheck() {
//TODO: move try/catch to calling function
		try {
			final String user = System.getProperty("user.name");
			if ("root".equals(user)) {
				this.warning("It is recommended to run as a non-root user");
			} else
			if ("administrator".equalsIgnoreCase(user)
			|| "admin".equalsIgnoreCase(user)) {
				this.warning("It is recommended to run as a non-administrator user");
			}
		} catch (Exception e) {
			Failure.fail(e);
		}
	}



	// load configs
	@xAppStep(type=StepType.STARTUP, title="Configs", priority=50)
	public void __STARTUP_configs() {
//TODO:
//		try {
//		} catch (Exception e) {
//			Failure.fail(e);
//		}
	}



	// clock
	@xAppStep(type=StepType.STARTUP, title="Clock", priority=60)
	public void __STARTUP_clock() {
		try {
			final xClock clock = xClock.get(true);
			this.startTime =
				xTime.getNew(
					clock.millis()
				);
		} catch (Exception e) {
			Failure.fail(e);
		}
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
		try {
			final String filename = this.getName()+".lock";
			final LockFile lock = LockFile.get(filename);
			if (!lock.acquire()) {
				Failure.fail("Failed to get lock on file: "+filename);
			}
		} catch (Exception e) {
			Failure.fail(e);
		}
	}



//	// start thread pools
//	@xAppStep(type=StepType.STARTUP, title="ThreadPools", priority=100)
//	public void __STARTUP_threadpools() {
//		try {
//			final xThreadPool pool =
//				xThreadPool_Main.get();
//			pool.start();
//		} catch (Exception e) {
//			Failure.fail(e);
//		}
//	}



	// start scheduler
	@xAppStep(type=StepType.STARTUP, title="Scheduler", priority=150)
	public void __STARTUP_scheduler() {
		try {
//TODO:
//			// start main scheduler
//			final xScheduler sched = xScheduler.getMainSched();
//			sched.start();
//TODO:
//			// start ticker
//			final xTicker ticker = xTicker.get();
//			ticker.Start();
		} catch (Exception e) {
			Failure.fail(e);
		}
	}



	// ------------------------------------------------------------------------------- //
	// shutdown steps



	// stop scheduler
	@xAppStep(type=StepType.SHUTDOWN, title="Scheduler", priority=150)
	public void __SHUTDOWN_scheduler() {
		try {
//			// stop ticker
//			final xTicker ticker = xTicker.get();
//			ticker.Stop();
//TODO:
//			// stop main scheduler
//			final xScheduler sched = xScheduler.getMainSched();
//			sched.stop();
		} catch (Exception e) {
			Failure.fail(e);
		}
	}



//	// stop thread pools
//	@xAppStep(type=StepType.SHUTDOWN, title="ThreadPools", priority=100)
//	public void __SHUTDOWN_threadpools() {
//		try {
//TODO:
//			xThreadPoolFactory
//				.ShutdownAll();
//		} catch (Exception e) {
//			Failure.fail(e);
//		}
//	}



	// display uptime
	@xAppStep(type=StepType.SHUTDOWN, title="Uptime", priority=60)
	public void __SHUTDOWN_uptimestats() {
//TODO: display total time running
//this.getUptimeString();
	}



	// stop console input
	@xAppStep(type=StepType.SHUTDOWN, title="Console", priority=30)
	public void __SHUTDOWN_console() {
		try {
			xLog.Shutdown();
		} catch (Exception e) {
			Failure.fail(e);
		}
	}



	// release lock file
	@xAppStep(type=StepType.SHUTDOWN, title="LockFile", priority=20)
	public void __SHUTDOWN_lockfile() {
		try {
			final String filename = this.getName()+".lock";
			LockFile.getRelease(filename);
		} catch (Exception e) {
			Failure.fail(e);
		}
	}



	// garbage collect
	@xAppStep(type=StepType.SHUTDOWN,title="GarbageCollect", priority=10)
	public void __SHUTDOWN_gc() {
//TODO:
//		Utils.Sleep(250L);
//		xScheduler.clearInstance();
		System.gc();
//		if (xScheduler.hasLoaded()) {
//			this.warning("xScheduler hasn't fully unloaded!");
//		} else {
//			this.finest("xScheduler has been unloaded");
//		}
		xVars.getOriginalOut()
			.println();
	}



	// ------------------------------------------------------------------------------- //



	// logger
	private volatile SoftReference<xLog> _log = null;
	@Override
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



	protected static void DisplayLineColors(
			final PrintStream out, final String bgColor,
			final Map<Integer, String> colors, final String line) {
		final StringBuilder buffer = new StringBuilder();
		int last = 0;
		final Map<Integer, String> cols;
		final boolean hasBgColor = Utils.notBlank(bgColor);
		if (hasBgColor) {
			// prepend background color
			cols = new LinkedHashMap<Integer, String>();
			cols.put(Integer.valueOf(1), "bg_"+bgColor);
			cols.putAll(colors);
		} else {
			cols = colors;
		}
		boolean withinColorTag = false;
		for (final Entry<Integer, String> entry : cols.entrySet()) {
			final int pos = entry.getKey().intValue() - 1;
			if (pos > last) {
				buffer.append(
					line.substring(last, pos)
				);
			}
			last = pos;
			if (withinColorTag) {
				buffer.append("|@");
			}
			withinColorTag = true;
			buffer.append("@|");
			if (hasBgColor) {
				buffer
					.append("bg_")
					.append(bgColor)
					.append(',');
			}
			buffer
				.append(entry.getValue())
				.append(' ');
		}
		if (last < line.length()) {
			buffer.append(line.substring(last));
		}
		if (withinColorTag) {
			buffer.append("|@");
		}
		{
			final String str =
				Ansi.ansi().a(' ')
					.render(buffer.toString())
					.reset().a(' ')
					.toString();
			out.println(str);
		}
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
		final String COLOR_BG = "blue";
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
		final String version = StringUtils.PadCenter(15, this.getVersion(), ' ');
		final PrintStream out =
			new xLogPrintStream(
				xLog.getRoot(),
				null
			);
		out.println();
		DisplayLineColors(out, COLOR_BG, colors1, "                                     _/\\_                        "    );
		DisplayLineColors(out, COLOR_BG, colors2, "         |`-.__     PoiXson          (('>         _   _          "     );
		DisplayLineColors(out, COLOR_BG, colors3, "         / ' _/    Software     _    /^|         /\\\\_/ \\         "  );
		DisplayLineColors(out, COLOR_BG, colors4, "       -****\\\"  "+version+" =>--/_\\|m---    / 0  0  \\        "     );
		DisplayLineColors(out, COLOR_BG, colors5, "      /    }                         ^^        /_   v   _\\       "    );
		DisplayLineColors(out, COLOR_BG, colors6, "     /    \\               @..@                   \\__^___/        "   );
		DisplayLineColors(out, COLOR_BG, colors7, " \\ /`    \\\\\\             (----)                  /  0    \\       ");
		DisplayLineColors(out, COLOR_BG, colors8, "  `\\     /_\\\\           ( >__< )                /        \\__     " );
		DisplayLineColors(out, COLOR_BG, colors9, "   `~~~~~~``~`          ^^ ~~ ^^                \\_(_|_)___  \\    "   );
		DisplayLineColors(out, COLOR_BG, colors10,"^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^/^(____//^/^"     );
		DisplayLineColors(out, COLOR_BG, colors11,"/////////////////////////////////////////////////////////////////"     );
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
