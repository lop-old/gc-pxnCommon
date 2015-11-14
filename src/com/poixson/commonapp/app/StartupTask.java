package com.poixson.commonapp.app;

import java.util.concurrent.atomic.AtomicInteger;

import com.poixson.commonapp.app.annotations.xAppStep.StepType;
import com.poixson.commonjava.Failure;
import com.poixson.commonjava.xVars;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.Utils.exceptions.RequiredArgumentException;
import com.poixson.commonjava.Utils.threads.xThreadPool;
import com.poixson.commonjava.xLogger.xLog;


public class StartupTask extends xRunnable {

	protected static final long STEP_SLEEP       = 0L;
	protected static final int  MAX_NESTED_CALLS = 10;

	public final xAppAbstract app;

	protected final AtomicInteger currentStep;
	public final int minStep;
	public final int maxStep;

	private int nestedCalls = 0;



	public StartupTask(final xAppAbstract app) {
		super("Startup");
		if(app == null) throw new RequiredArgumentException("app");
		this.app = app;
		this.minStep = app.minStep;
		this.maxStep = app.maxStep;
		if(this.minStep > this.maxStep) throw new IllegalArgumentException("minStep cannot be larger than maxStep!");
		this.currentStep = new AtomicInteger(this.minStep);
	}



	@Override
	public void run() {
		if(this.currentStep.get() == Integer.MAX_VALUE)
			return;
		final int step = this.currentStep.getAndIncrement();
		// finished
		if(step > this.maxStep) {
			this.currentStep.set(Integer.MAX_VALUE);
			this.log().title(this.app.getTitle()+" Ready and Running!");
			return;
		}
		// run all tasks of current step
		boolean hasRunSomething = false;
		for(final StepDAO dao : this.app.steps) {
			if(!StepType.STARTUP.equals(dao.type)) continue;
			if(dao.step != step)                   continue;
			if(Failure.hasFailed())    break;
			if(this.app.stopped.get()) break;
			hasRunSomething = true;
			final String desc =
					(new StringBuilder())
					.append(step)
					.append("-")
					.append(dao.title)
					.toString();
			// perform step
			this.setTaskName("Startup-"+desc);
			this.log().finest("Startup.. "+desc);
			try {
				dao.method.invoke(this.app);
			} catch (Exception e) {
				this.log().getWeak(desc).trace(e);
				Failure.fail("Startup failed at step: "+desc);
				break;
			}
			// sleep a moment
			utilsThread.Sleep(STEP_SLEEP);
			if(xVars.debug())
				utilsThread.Sleep(100L);
		}
		// something failed
		if(Failure.hasFailed() || this.app.stopped.get()) {
			this.app.Stop();
			return;
		}
		// nothing has run this step
		if(!hasRunSomething) {
			if(++this.nestedCalls < MAX_NESTED_CALLS) {
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
	private volatile xLog _log = null;
	private xLog _log_default  = null;
	public xLog log() {
		final xLog log = this._log;
		if(log != null)
			return log;
		if(this._log_default == null)
			this._log_default = xLog.getRoot();
		return this._log_default;
	}
	public void setLog(final xLog log) {
		this._log = log;
	}



}
