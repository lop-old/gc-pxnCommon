package com.poixson.tools.remapped;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.poixson.exceptions.RequiredArgumentException;


public class RunnableMultiplier implements Runnable {

	private final Set<Runnable> runs = new CopyOnWriteArraySet<Runnable>();



	public RunnableMultiplier() {
		super();
	}



	public void add(final Runnable run) {
		if (run == null) throw new RequiredArgumentException("run");
		this.runs.add(run);
	}



	@Override
	public void run() {
		final Iterator<Runnable> it = this.runs.iterator();
		while (it.hasNext()) {
			final Runnable run = it.next();
			run.run();
		}
	}



}
