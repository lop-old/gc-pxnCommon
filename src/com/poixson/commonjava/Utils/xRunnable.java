package com.poixson.commonjava.Utils;


public class xRunnable implements Runnable {

	protected volatile String taskName = null;
	protected final Runnable run;


	public xRunnable() {
		this.run = null;
	}
	public xRunnable(final String taskName) {
		this(taskName, null);
	}
	public xRunnable(final Runnable run) {
		this(null, run);
	}
	public xRunnable(final String taskName, final Runnable run) {
		if(utils.notEmpty(taskName))
			this.taskName = taskName;
		else if(run instanceof xRunnable)
			this.taskName = ((xRunnable) run).getTaskName();
		this.run = run;
	}


	public static xRunnable cast(final Runnable run) {
		if(run instanceof xRunnable)
			return (xRunnable) run;
		return new xRunnable(run);
	}


	@Override
	public void run() {
		if(this.run == null) throw new NullPointerException();
		this.run.run();
	}


	public String getTaskName() {
		return this.taskName;
	}


}
