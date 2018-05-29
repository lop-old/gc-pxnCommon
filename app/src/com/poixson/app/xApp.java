package com.poixson.app;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import com.poixson.abstractions.xStartable;
import com.poixson.app.xAppStep.StepType;
import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.AttachedLogger;
import com.poixson.logger.xLog;
import com.poixson.logger.xLogRoot;
import com.poixson.threadpool.xThreadPool;
import com.poixson.threadpool.types.xThreadPool_Main;
import com.poixson.tools.AppProps;
import com.poixson.tools.CoolDown;
import com.poixson.tools.HangCatcher;
import com.poixson.tools.Keeper;
import com.poixson.tools.xTime;
import com.poixson.tools.comparators.IntComparator;
import com.poixson.tools.remapped.xRunnable;
import com.poixson.utils.FileUtils;
import com.poixson.utils.ProcUtils;
import com.poixson.utils.StringUtils;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.Utils;


/*
 * Startup sequence
 *   10  prevent root        - xAppSteps_Tool
 *   50  load configs        - xAppSteps_Config
 *   70  lock file           - xAppSteps_LockFile
 *   80  display logo        - xAppSteps_Logo
 *   85  sync clock          - xAppStandard
 *  100  prepare commands    - xCommandHandler
 *  105  start console input - xAppSteps_Console
 *  200  startup time        - xAppStandard
 *  400  load plugins        - xPluginManager
 *  405  start plugins       - xPluginManager
 *
 * Shutdown sequence
 *  405  stop plugins        - xPluginManager
 *  400  unload plugins      - xPluginManager
 *  150  stop schedulers     - xAppSteps_Scheduler
 *  105  stop console input  - xAppSteps_Console
 *  100  stop thread pools   - xAppStandard
 *   60  display uptime      - xAppStandard
 *   20  release lock file   - xAppSteps_LockFile
 *   10  garbage collect     - xApp
 *    1  exit
 */
public abstract class xApp implements xStartable, AttachedLogger {

	protected static final String ERR_ALREADY_STOPPING_EXCEPTION    = "Cannot start app, already stopping!";
	protected static final String ERR_INVALID_STATE_EXCEPTION       = "Invalid startup/shutdown state!";
	protected static final String ERR_INVALID_START_STATE_EXCEPTION = "Invalid state, cannot start: {}";
	protected static final String ERR_INVALID_STOP_STATE_EXCEPTION  = "Invalid state, cannot shutdown: {}";

	// app instances
	protected static final CopyOnWriteArraySet<xApp> apps =
			new CopyOnWriteArraySet<xApp>();

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

	// properties
	protected final AppProps props;



	public xApp() {
		this._log = xLogRoot.get();
		this.props = new AppProps(this.getClass());
		// debug mode
		if (ProcUtils.isDebugWireEnabled()) {
			xVars.setDebug(true);
		}
		// search for .debug file
		if (Utils.notEmpty(xVars.SEARCH_DEBUG_FILES)) {
			final String result =
				FileUtils.SearchLocalFile(
					xVars.SEARCH_DEBUG_FILES,
					xVars.SEARCH_DEBUG_PARENTS
				);
			if (result != null)
				xVars.setDebug(true);
		}
		Keeper.add(this);
		apps.add(this);
//TODO:
//		Failure.register(
//			new Runnable() {
//				@Override
//				public void run() {
//					xApp.this.fail();
//				}
//			}
//		);
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
	}



	// ------------------------------------------------------------------------------- //
	// start/stop app



	protected Object[] getStepObjects(final StepType type) {
		return null;
	}



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
						ERR_ALREADY_STOPPING_EXCEPTION,
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
				ERR_INVALID_START_STATE_EXCEPTION,
				this.state.get()
			);
			return;
		}
//TODO:
//		// register shutdown hook
//		xThreadPool.addShutdownHook(
//			new RemappedMethod(this, "stop")
//		);
		if (Failure.hasFailed()) return;
		this.title(
			new String[] { "Starting {}.." },
			this.getTitle()
		);
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
		// check state
		{
			final int stepInt = this.state.get();
			// <=0 already stopping or stopped
			if (stepInt <= STATE_OFF)
				return;
			// set stopping state
			if ( ! this.state.compareAndSet(stepInt, STATE_STOP) ) {
				this.warning(
					ERR_INVALID_STOP_STATE_EXCEPTION,
					this.state.get()
				);
				return;
			}
		}
		this.title(
			new String[] { "Stopping {}.." },
			this.getTitle()
		);
		// start hang catcher
		this.startHangCatcher();
		// load shutdown steps
		{
			final xThreadPool_Main pool = xThreadPool_Main.get();
			pool.runTaskNow(
				new xRunnable("Load shutdown steps") {
					private volatile xThreadPool pool = null;
					public xRunnable init(final xThreadPool pool) {
						this.pool = pool;
						return this;
					}
					@Override
					public void run() {
						if (Failure.hasFailed()) return;
						final xApp app = xApp.this;
						// prepare shutdown steps
						synchronized (app.currentSteps) {
							app.currentSteps.clear();
							app.loadSteps(StepType.SHUTDOWN);
						}
						if (Failure.hasFailed()) return;
						// queue shutdown sequence
						final int stepInt = xApp.this.state.get();
						xApp.QueueNextStep(xApp.this, this.pool, stepInt);
					}
				}.init(pool)
			);
		}
	}



	public static void shutdown() {
		final Iterator<xApp> it = apps.iterator();
		while (it.hasNext()) {
			final xApp app = it.next();
			app.stop();
		}
	}
	public static void kill() {
		System.exit(1);
	}



	@SuppressWarnings("unchecked")
	public static <T extends xApp> T getApp(final Class<T> clss) {
		if (clss == null) throw new RequiredArgumentException("clss");
		final Iterator<xApp> it = apps.iterator();
		while (it.hasNext()) {
			final xApp app = it.next();
			if (clss.isInstance(app))
				return (T) app;
		}
		return null;
	}
	public static xApp[] getApps() {
		return apps.toArray(new xApp[0]);
	}



	public void join() {
		xThreadPool_Main.get()
			.joinWorkers();
	}



	// run next step
	@Override
	public void run() {
		if (Failure.hasFailed()) return;
		synchronized (this.runLock) {
			final int stepInt = this.state.get();
			// finished startup/shutdown
			if (this.currentSteps.isEmpty()) {
				this.stopHangCatcher();
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
					this.info("{} is ready!", this.getTitle());
				} else {
					this.state.set(STATE_OFF);
					this.info("{} has finished stopping.", this.getTitle());
				}
				return;
			}
			// get current step
			final xAppStepDAO step = this.grabNextStepDAO();
			if (Failure.hasFailed()) return;
			if (step != null) {
				if (this.log().isDetailLoggable()) {
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
		final String taskName =
			StringUtils.ReplaceTags(
				"{}({})",
				( stepInt > STATE_OFF ? "Startup" : "Shutdown" ),
				stepInt
			);
		pool.runTaskLater(taskName, app);
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
			if (stepInt == STATE_OFF)         throw new IllegalStateException(ERR_INVALID_STATE_EXCEPTION);
			if (stepInt == Integer.MIN_VALUE) throw new IllegalStateException(ERR_INVALID_STATE_EXCEPTION);
			if (stepInt == Integer.MAX_VALUE) throw new IllegalStateException(ERR_INVALID_STATE_EXCEPTION);
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
				nextStepInt = 0;
				while (it.hasNext()) {
					final int index = it.next().intValue();
					if (index < nextStepInt) {
						nextStepInt = index;
					}
				}
				// no steps left
				if (nextStepInt == 0)
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
		this.loadSteps(type, this);
		for (final Object obj : containers) {
			this.loadSteps(type, obj);
		}
		// log loaded steps
		if (this.log().isDetailLoggable()) {
			final List<String> lines = new ArrayList<String>();
			lines.add("Found {} {} steps:");
			// list steps in order
			final TreeSet<Integer> orderedValues =
				new TreeSet<Integer>(
					new IntComparator(false)
				);
			orderedValues.addAll(
				this.currentSteps.keySet()
			);
			int count = 0;
			//ORDERED_LOOP:
			for (final Integer stepInt : orderedValues) {
				final List<xAppStepDAO> list = this.currentSteps.get(stepInt);
				if (Utils.isEmpty(list))
					continue;
				//LIST_LOOP:
				for (final xAppStepDAO dao : list) {
					count++;
					lines.add(
						(new StringBuilder())
							.append(
								StringUtils.PadFront(
									5,
									stepInt.toString(),
									' '
								)
							)
							.append(" - ")
							.append(dao.title)
							.toString()
					);
				} // end LIST_LOOP
			} // end ORDERED_LOOP
			this.log()
				.detail(
					lines.toArray(new String[0]),
					count,
					( StepType.STARTUP.equals(type) ? "Startup" : "Shutdown" )
				);
		} // end log steps
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
			METHODS_LOOP:
			for (final Method m : methods) {
				final xAppStep anno = m.getAnnotation(xAppStep.class);
				if (anno == null) continue METHODS_LOOP;
				// found step method
				if (type.equals(anno.Type())) {
					final xAppStepDAO dao =
						new xAppStepDAO(
							this,
							container,
							m,
							anno
						);
					// add to existing list or new list
					this.currentSteps.computeIfAbsent(
						Integer.valueOf(dao.stepValue),
						key -> new ArrayList<xAppStepDAO>()
					).add(dao);
				}
			} // end METHODS_LOOP
		}
	}



	// ------------------------------------------------------------------------------- //
	// hang catcher



	private void startHangCatcher() {
		if (ProcUtils.isDebugWireEnabled())
			return;
		final HangCatcher catcher =
			new HangCatcher(
				xTime.getNew("5s").getMS(),
				100L,
				new Runnable() {
					@Override
					public void run() {
						final int step = xApp.this.state.get();
						xApp.this.publish(
							new String[] {
								(
									step > 0
									? "Startup step: "
									: "Shutdown step: "
								) + Integer.toString(step),
								"                                  ",
								" ******************************** ",
								" *  Startup/Shutdown has hung!  * ",
								" ******************************** ",
								"                                  "
							}
						);
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
	// properties



	public String getName() {
		return this.props.name;
	}
	public String getTitle() {
		return this.props.title;
	}
	public String getFullTitle() {
		return this.props.titleFull;
	}
	public String getVersion() {
		return this.props.version;
	}
	public String getCommitHashFull() {
		return this.props.commitHashFull;
	}
	public String getCommitHashShort() {
		return this.props.commitHashShort;
	}
	public String getURL() {
		return this.props.url;
	}
	public String getOrgName() {
		return this.props.orgName;
	}
	public String getOrgURL() {
		return this.props.orgUrl;
	}
	public String getIssueName() {
		return this.props.issueName;
	}
	public String getIssueURL() {
		return this.props.issueUrl;
	}



	// ------------------------------------------------------------------------------- //
	// startup steps



	// ------------------------------------------------------------------------------- //
	// shutdown steps



	// garbage collect
	@xAppStep( Type=StepType.SHUTDOWN, Title="Garbage Collect", StepValue=10 )
	public void __SHUTDOWN_gc(final xApp app, final xLog log) {
		Keeper.remove(this);
		ThreadUtils.Sleep(50L);
		xVars.getOriginalOut()
			.flush();
		System.gc();
//TODO: is this useful?
//		xScheduler.clearInstance();
//		if (xScheduler.hasLoaded()) {
//			this.warning("xScheduler hasn't fully unloaded!");
//		} else {
//			this.finest("xScheduler has been unloaded");
//		}
	}



	@xAppStep( Type=StepType.SHUTDOWN, Title="Exit", StepValue=1)
	public void __SHUTDOWN_exit() {
		final Thread stopThread =
			new Thread() {
				private volatile int exitCode = 0;
				public Thread init(final int exitCode) {
					this.exitCode = exitCode;
					return this;
				}
				@Override
				public void run() {
					// stop thread pools
					xThreadPool.StopAll();
					if (xVars.isDebug()) {
						final CoolDown cool = CoolDown.getNew("1s");
						cool.resetRun();
						try {
							while ( ! cool.runAgain() ) {
								Thread.sleep(50L);
								if (ThreadUtils.CountStillRunning() == 0)
									break;
							}
						} catch (InterruptedException ignore) {}
						ThreadUtils.DisplayStillRunning();
					}
					xVars.getOriginalOut()
						.println();
					System.exit(this.exitCode);
				}
			}.init(0);
		stopThread.setName("EndThread");
		stopThread.setDaemon(true);
		stopThread.start();
	}



	// ------------------------------------------------------------------------------- //
	// logger



	private final xLog _log;
	@Override
	public xLog log() {
		return this._log;
	}



}
