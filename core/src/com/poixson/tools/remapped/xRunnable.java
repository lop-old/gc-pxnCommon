package com.poixson.tools.remapped;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.utils.Utils;


public class xRunnable implements RunnableNamed {

	public volatile String taskName = null;
	public final Runnable task;



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
		if (run instanceof RunnableNamed) {
			this.taskName = ((RunnableNamed) run).getTaskName();
		}
		this.task = run;
	}



	// ------------------------------------------------------------------------------- //
	// cast



	public static xRunnable cast(final Runnable run) {
		if (run == null)
			return null;
		// already correct type
		if (run instanceof xRunnable)
			return (xRunnable) run;
		// get name from interface
		if (run instanceof RunnableNamed) {
			return
				new xRunnable(
					((RunnableNamed) run).getTaskName(),
					run
				);
		}
		return new xRunnable(run);
	}



	// ------------------------------------------------------------------------------- //
	// run task



	@Override
	public void run() {
		final Runnable task = this.task;
		if (task == null) throw new RequiredArgumentException("task");
		task.run();
	}



	// ------------------------------------------------------------------------------- //
	// config



	@Override
	public String getTaskName() {
		if (this.task != null) {
			if (this.task instanceof RunnableNamed) {
				final String taskName = ((RunnableNamed) this.task).getTaskName();
				if (Utils.notEmpty(taskName))
					return taskName;
			}
		}
		return this.taskName;
	}
	@Override
	public void setTaskName(final String taskName) {
		this.taskName = (
			Utils.isEmpty(taskName)
			? null
			: taskName
		);
	}
	@Override
	public boolean taskNameEquals(final String taskName) {
		final String thisName = this.getTaskName();
		if (Utils.isEmpty(taskName))
			return Utils.isEmpty(thisName);
		return taskName.equals(thisName);
	}



}
