package com.poixson.commonapp.app;

import java.util.concurrent.atomic.AtomicInteger;

import com.poixson.commonapp.app.annotations.xAppStep.StepType;
import com.poixson.commonjava.Failure;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.Utils.threads.HangCatcher;
import com.poixson.commonjava.Utils.threads.xThreadPool;
import com.poixson.commonjava.xLogger.xLog;


public class ShutdownTask extends xRunnable {

	protected static final long STEP_SLEEP       = 0L;
	protected static final int  MAX_NESTED_CALLS = 10;

	public final xAppAbstract app;

	protected final AtomicInteger currentStep;
	public final int minStep;
	public final int maxStep;

	private int nestedCalls = 0;

	private final HangCatcher hangCatcher;



	public ShutdownTask(final xAppAbstract app) {
		super("Shutdown");
		if(app == null) throw new NullPointerException("app argument is required!");
		this.app = app;
		this.minStep = app.minStep;
		this.maxStep = app.maxStep;
		if(this.minStep > this.maxStep) throw new IllegalArgumentException("minStep cannot be larger than maxStep!");
		this.currentStep = new AtomicInteger(this.maxStep);
		// start hang catcher
		this.hangCatcher = HangCatcher.get();
		this.hangCatcher.Start();
	}



	@Override
	public void run() {
		if(this.currentStep.get() == Integer.MIN_VALUE)
			return;
		final int step = this.currentStep.getAndDecrement();
		// finished
		if(step < this.minStep) {
			this.currentStep.set(Integer.MIN_VALUE);
			this.log().title(this.app.getTitle()+" Stopped :-)");
			xThreadPool.Exit();
			return;
		}
		// run all tasks of current step
		boolean hasRunSomething = false;
		for(final StepDAO dao : this.app.steps) {
			if(!StepType.SHUTDOWN.equals(dao.type)) continue;
			if(dao.step != step)                    continue;
			hasRunSomething = true;
			final String desc =
					(new StringBuilder())
					.append(step)
					.append("-")
					.append(dao.title)
					.toString();
			// perform step
			this.setTaskName("Shutdown-"+desc);
			this.log().finest("Shutdown.. "+desc);
			try {
				dao.method.invoke(this.app);
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
			if(++this.nestedCalls < MAX_NESTED_CALLS) {
				this.run();
				return;
			}
			this.nestedCalls = 0;
			this.hangCatcher.resetTimeout();
			// queue next step
			xThreadPool.getMainPool()
					.runLater(this);
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
