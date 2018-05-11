package com.poixson.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class xThreadFactory implements ThreadFactory {

	protected final String  name;
	protected final ThreadGroup group;
	protected final boolean daemon;
	protected volatile int  priority;

	protected final AtomicInteger count = new AtomicInteger(0);



	public xThreadFactory(final String name) {
		this(
			name,
			false
		);
	}
	public xThreadFactory(final String name, final boolean daemon) {
		this(
			name,
			null,
			daemon,
			Thread.NORM_PRIORITY
		);
	}
	public xThreadFactory(final String name, final ThreadGroup group, final boolean daemon, final int priority) {
		this.name     = name;
		this.group    = group;
		this.daemon   = daemon;
		this.priority = priority;
	}



	@Override
	public Thread newThread(final Runnable run) {
		if (this.count.get() > Integer.MAX_VALUE - 100)
			throw new IllegalStateException("ThreadFactory count overflow!");
		final int id = this.count.incrementAndGet();
		final Thread thread = new Thread(this.group, run);
		thread.setPriority(this.priority);
		thread.setDaemon(this.daemon);
		thread.setName(
			(new StringBuilder())
				.append(this.name)
				.append(':')
				.append(id)
				.toString()
		);
		return thread;
	}



	public void setPriority(final int priority) {
		if (priority > this.priority) {
			this.group.setMaxPriority(priority);
		}
		this.priority = priority;
		if (priority < this.group.getMaxPriority()) {
			this.group.setMaxPriority(priority);
		}
	}



}
