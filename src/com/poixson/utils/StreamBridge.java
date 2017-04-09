package com.poixson.commonjava.Utils;
/*
 * cron4j - A pure Java cron-like scheduler
 *
 * Copyright (C) 2007-2010 Carlo Pelliccia (www.sauronsoftware.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version
 * 2.1, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License 2.1 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License version 2.1 along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.poixson.commonjava.Utils.exceptions.RequiredArgumentException;
import com.poixson.commonjava.xLogger.xLog;


/**
 * A package-reserved utility class. It spawns a secondary thread in which the
 * supplied {@link InputStream} instance is read, and the incoming contents are
 * written in the supplied {@link OutputStream}.
 * @author Carlo Pelliccia
 */
public class StreamBridge implements xStartable {

	protected static final Set<StreamBridge> instances = new HashSet<StreamBridge>();
	protected static final AtomicInteger nextIndex = new AtomicInteger(0);

	protected final Thread thread;
	protected volatile boolean running  = false;
	protected volatile boolean stopping = false;

	protected final InputStream  in;
	protected final OutputStream out;



	public StreamBridge(final InputStream in, final OutputStream out) {
		if(in  == null) throw new RequiredArgumentException("in");
		if(out == null) throw new RequiredArgumentException("out");
		synchronized(instances) {
			instances.add(this);
		}
		this.in  = in;
		this.out = out;
		this.thread = new Thread(this);
		this.thread.setName("StreamBridge"+Integer.toString(nextIndex.incrementAndGet()));
	}
	protected void remove() {
		synchronized(instances) {
			instances.remove(this);
		}
	}



	@Override
	public void run() {
		if(this.running)  throw new RuntimeException("StreamBridge already running");
		if(this.stopping) throw new RuntimeException("StreamBridge already stopped");
		synchronized(this.thread) {
			if(this.running)  throw new RuntimeException("StreamBridge already running");
			if(this.stopping) throw new RuntimeException("StreamBridge already stopped");
			this.running = true;
		}
		while(!this.stopping) {
			final int b;
			try {
				b = this.in.read();
			} catch (IOException e) {
				if(!this.stopping && !Thread.interrupted())
					xLog.getRoot().trace(e);
				break;
			}
			if(b == -1)
				break;
			try {
				this.out.write(b);
			} catch (IOException e) {
				if(!this.stopping && !Thread.interrupted())
					xLog.getRoot().trace(e);
				break;
			}
		}
		utils.safeClose(this.out);
		utils.safeClose(this.in);
		this.stopping = true;
		this.running = false;
		this.remove();
	}



	@Override
	public void Start() {
		this.thread.start();
	}
	@Override
	public void Stop() {
		this.stopping = true;
		this.thread.interrupt();
		utils.safeClose(this.out);
		utils.safeClose(this.in);
	}
	@Override
	public boolean isRunning() {
		return this.running;
	}
	public boolean isAlive() {
		return this.isRunning();
	}



	/**
	 * Waits for this job to die.
	 * @throws InterruptedException
	 *             If another thread has interrupted the current thread. The
	 *             interrupted status of the current thread is cleared when this
	 *             exception is thrown.
	 */
	public void join() throws InterruptedException {
		this.thread.join();
	}
	/**
	 * Waits at most <code>millis</code> milliseconds for this thread to die. A
	 * timeout of <code>0</code> means to wait forever.
	 * @param millis the time to wait in milliseconds.
	 * @throws InterruptedException
	 *             If another thread has interrupted the current thread. The
	 *             interrupted status of the current thread is cleared when this
	 *             exception is thrown.
	 */
	public void join(final long millis) throws InterruptedException {
		this.thread.join(millis);
	}
	/**
	 * @param millis the time to wait in milliseconds.
	 * @param nanos 0-999999 additional nanoseconds to wait.
	 * @throws IllegalArgumentException if the value of millis is negative the
	 *             value of nanos is not in the range 0-999999.
	 * @throws InterruptedException
	 *             If another thread has interrupted the current thread. The
	 *             interrupted status of the current thread is cleared when this
	 *             exception is thrown.
	 */
	public void join(final long millis, final int nanos)
			throws IllegalArgumentException, InterruptedException {
		this.thread.join(millis, nanos);
	}



}
