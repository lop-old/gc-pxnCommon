package com.poixson.commonapp.app;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.poixson.commonapp.app.annotations.xAppStep;
import com.poixson.commonapp.app.annotations.xAppStep.StepType;
import com.poixson.commonjava.Failure;
import com.poixson.commonjava.Failure.FailureAction;
import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.Utils.xStartable;
import com.poixson.commonjava.Utils.threads.xThreadPool;
import com.poixson.commonjava.xLogger.xLevel;
import com.poixson.commonjava.xLogger.xLog;


public class xAppManager implements xStartable, FailureAction {

	protected static final long STEP_SLEEP = 0;
	protected static final int  MAX_NESTED = 10;

	protected static volatile xAppManager instance = null;
	protected static final Object instanceLock = new Object();
	protected final xApp app;

	protected final AtomicBoolean running  = new AtomicBoolean(false);
	protected final AtomicBoolean stopping = new AtomicBoolean(false);

	protected final Set<StepDAO> steps;
	public volatile AtomicInteger nextStep = null;
	protected final int minPriority;
	protected final int maxPriority;



	protected static class StepDAO {

		public final StepType type;
		public final int    priority;
		public final String name;
		public final String title;
		public final Method method;

		public StepDAO(final xAppStep annotation, final Method method) {
			if(annotation == null) throw new NullPointerException("annotation argument is required!");
			if(method     == null) throw new NullPointerException("method argument is required!");
			this.type     = annotation.type();
			this.priority = annotation.priority();
			// strip method down to name
			{
				String name = method.getName();
				name = utilsString.trims(name, "_");
				for(final String trim : new String[] {
						"startup",
						"start",
						"shutdown",
						"stop"
				}) {
					if(name.startsWith(trim))
						name = name.substring(trim.length());
					name = utilsString.trims(name, "_");
				}
				if(utils.isEmpty(name))
					name = utilsString.trims(method.getName(), "_");
				this.name = name;
			}
			this.title = utils.isEmpty(annotation.title()) ? this.name : annotation.title();
			this.method = method;
		}

	}



	public static xAppManager get() {
		if(instance == null) {
			synchronized(instanceLock) {
				if(instance == null)
					instance = new xAppManager();
			}
		}
		return instance;
	}
	protected xAppManager() {
		Keeper.add(this);
		this.app = xApp.get();
		if(this.app == null) throw new RuntimeException("app variable not set!");
		// init logger
		xLog.getRoot().setLevel(xLevel.ALL);
		if(Failure.hasFailed()) {
			System.out.println("Failure, pre-init!");
			System.exit(1);
		}
		// find startup/shutdown steps
		final Class<? extends xApp> clss = this.app.getClass();
		if(clss == null) throw new RuntimeException("Failed to get app class!");
		// get method annotations
		final Method[] methods = clss.getMethods();
		if(utils.isEmpty(methods)) throw new RuntimeException("Failed to get app methods!");
		final Set<StepDAO> steps = new HashSet<StepDAO>();
		for(final Method method : methods) {
			final xAppStep anno = method.getAnnotation(xAppStep.class);
			if(anno == null) continue;
			// found step method
			final StepDAO dao = new StepDAO(anno, method);
			steps.add(dao);
		}
		this.steps = Collections.unmodifiableSet(steps);
		// find min/max priority
		{
			int min = -1;
			int max = -1;
			for(final StepDAO step : this.steps) {
				final int p = step.priority;
				if(min == -1 || p < min)
					min = p;
				if(max == -1 || p > max)
					max = p;
			}
			if(min == -1 || max == -1)
				throw new RuntimeException("No startup steps found!");
			this.minPriority = min;
			this.maxPriority = max;
		}
		Failure.register(this);
	}



	// ------------------------------------------------------------------------------- //
	// startup



	@Override
	public void Start() {
		if(this.stopping.get())
			throw new IllegalStateException("App already stopped!");
		if(!this.running.compareAndSet(false, true))
			throw new IllegalStateException("App already started!");
		this.log().title("Starting "+this.app.getTitle()+"..");
		// startup task
		final StartupTask task = new StartupTask(this);
		xThreadPool.getMainPool()
			.runLater(task);
	}



	protected static class StartupTask extends xRunnable {

		public final xAppManager manager;
		private int nestedCalls = 0;

		public StartupTask(final xAppManager manager) {
			super("Startup");
			this.manager = manager;
			manager.nextStep = new AtomicInteger(manager.minPriority);
		}

		@Override
		public void run() {
			final int step = this.manager.nextStep.getAndIncrement();
			// finished
			if(step > this.manager.maxPriority) {
				this.manager.nextStep = null;
				this.log().title(this.manager.app.getTitle()+" Ready and Running!");
				return;
			}
			// run all steps of current priority
			boolean hasRunSomething = false;
			for(final StepDAO dao : this.manager.steps) {
				if(!StepType.STARTUP.equals(dao.type)) continue;
				if(dao.priority != step) continue;
				if(Failure.hasFailed()) break;
				if(this.manager.stopping.get()) break;
				hasRunSomething = true;
				final String desc = Integer.toString(step)+"-"+dao.title;
				// perform step
				this.setTaskName("Startup-"+desc);
				this.log().finest("Startup.. "+desc);
				try {
					dao.method.invoke(this.manager.app);
				} catch (Exception e) {
					this.log().getWeak(desc).trace(e);
					Failure.fail("Startup failed at step: "+desc);
					break;
				}
				// sleep a moment
				utilsThread.Sleep(STEP_SLEEP);
			}
			// something failed
			if(Failure.hasFailed() || this.manager.stopping.get()) {
				this.manager.Stop();
				return;
			}
			// nothing has run this step
			if(!hasRunSomething) {
				if(++this.nestedCalls < MAX_NESTED) {
					this.run();
					return;
				}
			}
			this.nestedCalls = 0;
			// queue next step
			xThreadPool.getMainPool()
				.runLater(this);
		}

		// logger
		public xLog log() {
			return this.manager.log();
		}

	}



	// ------------------------------------------------------------------------------- //
	// shutdown



	@Override
	public void Stop() {
		if(!this.stopping.compareAndSet(false, true) ||
				!this.running.compareAndSet(true, false)) {
			this.log().finest("Already stopping..");
			return;
		}
		this.log().title("Stopping "+this.app.getTitle()+"..");
		// shutdown task
		final ShutdownTask task = new ShutdownTask(this);
		xThreadPool.getMainPool()
			.runLater(task);
	}
	@Override
	public void doFailAction() {
		this.Stop();
	}



	protected static class ShutdownTask extends xRunnable {

		public final xAppManager manager;
		private int nestedCalls = 0;

		public ShutdownTask(final xAppManager manager) {
			super("Shutdown");
			this.manager = manager;
			if(manager.nextStep == null)
				manager.nextStep = new AtomicInteger(manager.maxPriority);
		}

		@Override
		public void run() {
			final int step = this.manager.nextStep.getAndDecrement();
			// finished
			if(step < this.manager.minPriority) {
				this.manager.nextStep = null;
				this.log().title(this.manager.app.getTitle()+" Stopped");
				xThreadPool.Exit();
				return;
			}
			// run all steps of current priority
			boolean hasRunSomething = false;
			for(final StepDAO dao : this.manager.steps) {
				if(!StepType.SHUTDOWN.equals(dao.type)) continue;
				if(dao.priority != step) continue;
				hasRunSomething = true;
				final String desc = Integer.toString(step)+"-"+dao.title;
				// perform step
				this.setTaskName("Shutdown-"+desc);
				this.log().finest("Shutdown.. "+desc);
				try {
					dao.method.invoke(this.manager.app);
				} catch (Exception e) {
					this.log().getWeak(desc).trace(e);
					Failure.fail("Shutdown failed at step: "+desc);
					break;
				}
				// sleep a moment
				utilsThread.Sleep(STEP_SLEEP);
			}
			// nothing has run this step
			if(!hasRunSomething) {
				if(++this.nestedCalls < MAX_NESTED) {
					this.run();
					return;
				}
			}
			this.nestedCalls = 0;
			// queue next step
			xThreadPool.getMainPool()
				.runLater(this);
		}

		// logger
		public xLog log() {
			return this.manager.log();
		}

	}



	// ------------------------------------------------------------------------------- //



	@Override
	public void run() {
		// pass main thread to thread pool
		xThreadPool.getMainPool()
			.run();
	}
	@Override
	public boolean isRunning() {
		if(Failure.hasFailed())
			return false;
		return this.running.get() && !this.stopping.get();
	}
	public boolean isStopping() {
		if(Failure.hasFailed())
			return true;
		return this.stopping.get();
	}



	// logger
	private volatile xLog _log = null;
	public xLog log() {
		if(this._log == null)
			this._log = xApp.log();
		return this._log;
	}



}
