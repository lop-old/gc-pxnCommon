package com.poixson.commonapp.app;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.poixson.commonapp.app.annotations.xAppStep;
import com.poixson.commonapp.app.annotations.xAppStep.StepType;
import com.poixson.commonjava.Failure;
import com.poixson.commonjava.Failure.FailureAction;
import com.poixson.commonjava.xVars;
import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.xClock;
import com.poixson.commonjava.Utils.xStartable;
import com.poixson.commonjava.Utils.threads.xThreadPool;
import com.poixson.commonjava.xLogger.xLog;


public abstract class xAppAbstract implements xStartable, FailureAction {

//	protected static final String ALREADY_STARTED_EXCEPTION = "Illegal app state; this shouldn't happen; cannot start in this state; possibly already started?";
//	protected static final String ILLEGAL_STATE_EXCEPTION   = "Illegal app state; cannot continue; this shouldn't happen; Current state: ";

	protected final AtomicBoolean running = new AtomicBoolean(false);
	protected final AtomicBoolean stopped = new AtomicBoolean(false);

	protected final List<StepDAO> steps;
	public volatile AtomicInteger nextStep = null;
	protected final int minStep;
	protected final int maxStep;

	private volatile long startTime = -1;

	// just to prevent gc
	@SuppressWarnings("unused")
	private static final Keeper keeper = Keeper.get();



	// new instance
	public xAppAbstract() {
		Keeper.add(this);
		xVars.init();
		// find startup/shutdown steps
		final Class<? extends xAppAbstract> clss = this.getClass();
		if(clss == null) throw new RuntimeException("Failed to get app class!");
		// get method annotations
		final Method[] methods = clss.getMethods();
		if(utils.isEmpty(methods)) throw new RuntimeException("Failed to get app methods!");

		final List<StepDAO> steps = new ArrayList<StepDAO>();
		for(final Method method : methods) {
			final xAppStep anno = method.getAnnotation(xAppStep.class);
			if(anno == null) continue;
			// found step method
			final StepDAO dao = new StepDAO(anno, method);
			steps.add(dao);
		}
		this.steps = Collections.unmodifiableList(steps);
		// find min/max priority
		{
			int min = -1;
			int max = -1;
			for(final StepDAO step : this.steps) {
				final int p = step.step;
				if(min == -1 || p < min)
					min = p;
				if(max == -1 || p > max)
					max = p;
			}
			if(min == -1 || max == -1)
				throw new RuntimeException("No startup steps found!");
			this.minStep = min;
			this.maxStep = max;
		}
		Failure.register(this);
	}



	public abstract String getTitle();



	// ------------------------------------------------------------------------------- //
	// startup



	@Override
	public void Start() {
		if(this.stopped.get())
			throw new IllegalStateException("App already stopped!");
		if(!this.running.compareAndSet(false, true))
			throw new IllegalStateException("App already started!");
		this.log().title(
				(new StringBuilder())
				.append("Starting ")
				.append(this.getTitle())
				.append("..")
				.toString()
		);
		// startup task
		final StartupTask task = new StartupTask(this);
		xThreadPool.getMainPool()
			.runLater(task);
	}



	// ensure not root
	@xAppStep(type=StepType.STARTUP, title="RootCheck", priority=5)
	public void __STARTUP_rootcheck() {
		final String user = System.getProperty("user.name");
		if("root".equals(user))
			this.log().warning("It is recommended to run as a non-root user");
		else
		if("administrator".equalsIgnoreCase(user) || "admin".equalsIgnoreCase(user))
			this.log().warning("It is recommended to run as a non-administrator user");
	}



	// clock
	@xAppStep(type=StepType.STARTUP, title="Clock", priority=11)
	public void __STARTUP_clock() {
		this.startTime = xClock.get(true).millis();
	}



	// ------------------------------------------------------------------------------- //
	// shutdown



	@Override
	public void Stop() {
		if(!this.stopped.compareAndSet(false, true) ||
				!this.running.compareAndSet(true,  false)) {
			this.log().finest("Already stopping..");
			return;
		}
		this.log().title(
				(new StringBuilder())
				.append("Stopping ")
				.append(this.getTitle())
				.append("..")
				.toString()
		);
		// shutdown task
		final ShutdownTask task = new ShutdownTask(this);
		xThreadPool.getMainPool()
			.runLater(task);
	}
	@Override
	public void doFailAction() {
		this.Stop();
	}



	// total time running
	@xAppStep(type=StepType.SHUTDOWN, title="UptimeStats", priority=100)
	public void __SHUTDOWN_uptimestats() {
//TODO: display total time running
	}



	// ------------------------------------------------------------------------------- //



	@Override
	public abstract void run();
	@Override
	public boolean isRunning() {
		if(Failure.hasFailed())
			return false;
		return this.running.get()
			&& !this.stopped.get();
	}
	public boolean isStopping() {
		if(Failure.hasFailed())
			return true;
		return this.stopped.get();
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



	protected abstract void displayLogo();



	// logger
	private volatile xLog _log = null;
	public xLog log() {
		if(this._log == null)
			this._log = xLog.getRoot();
		return this._log;
	}



}
