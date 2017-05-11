package com.poixson.utils;

import com.poixson.utils.exceptions.RequiredArgumentException;


public class xRunnable implements Runnable {

	protected volatile String taskName = null;
	protected final Runnable task;



	public xRunnable() {
		this.taskName = null;
		this.task     = null;
	}
	public xRunnable(final String taskName) {
		this(
			taskName,
			(Runnable) null
		);
	}
	public xRunnable(final xRunnable run) {
		this(
			run.getTaskName(),
			run
		);
	}
	public xRunnable(final Runnable run) {
		this(
			(String) null,
			run
		);
	}
	public xRunnable(final String taskName, final Runnable run) {
		if (Utils.notEmpty(taskName)) {
			this.taskName = taskName;
		} else
		if (run instanceof xRunnable) {
			this.taskName = ((xRunnable) run).getTaskName();
		}
		this.task = run;
	}



	public static xRunnable cast(final Runnable run) {
		return cast(null, run);
	}
	public static xRunnable cast(final String taskName, final Runnable run) {
		if (run == null) throw new RequiredArgumentException("run");
		if (run instanceof xRunnable) {
			final xRunnable xrun = (xRunnable) run;
			if (Utils.notEmpty(taskName)) {
				xrun.setTaskName(taskName);
			}
			return xrun;
		}
		return new xRunnable(
			(
				Utils.isEmpty(taskName)
				? "<Runnable>"
				: taskName
			),
			run
		);
	}



	@Override
	public void run() {
		final Runnable task = this.task;
		if (task == null) throw new RequiredArgumentException("task");
		task.run();
	}



	public String getTaskName() {
		return this.taskName;
	}
	public void setTaskName(final String taskName) {
		this.taskName =
			Utils.isEmpty(taskName)
			? null
			: taskName;
	}
	public boolean taskNameEquals(final String taskName) {
		if (Utils.isEmpty(taskName))
			return Utils.isEmpty(this.taskName);
		return taskName.equals(this.taskName);
	}



}
