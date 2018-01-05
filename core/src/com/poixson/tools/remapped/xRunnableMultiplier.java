package com.poixson.tools.remapped;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


public class xRunnableMultiplier extends xRunnable {

	private final Set<xRunnable> runs = new CopyOnWriteArraySet<xRunnable>();



	public xRunnableMultiplier() {
		super();
	}
	public xRunnableMultiplier(final String taskName) {
		super(taskName);
	}
	public xRunnableMultiplier(final xRunnable run) {
		super(run);
	}
	public xRunnableMultiplier(final Runnable run) {
		super(run);
	}
	public xRunnableMultiplier(final String taskName, final Runnable run) {
		super(taskName, run);
	}



	public void add(final xRunnable run) {
		if (run == null)
			throw new NullPointerException("run argument is required!");
		this.runs.add(run);
	}



	@Override
	public void run() {
		final Iterator<xRunnable> it = this.runs.iterator();
		while (it.hasNext()) {
			final xRunnable run = it.next();
			run.run();
		}
	}



}
