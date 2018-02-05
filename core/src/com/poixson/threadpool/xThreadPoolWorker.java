package com.poixson.threadpool;

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.poixson.abstractions.xStartable;
import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.xLog;
import com.poixson.tools.CoolDown;
import com.poixson.utils.ThreadUtils;


public class xThreadPoolWorker implements xStartable {

	public static final long DEFAULT_WAIT_FOR_START = 500L;

	protected final xThreadPool pool;
	protected final AtomicReference<Thread> thread =
			new AtomicReference<Thread>(null);

	protected final long workerIndex;

	// worker state
	protected final AtomicBoolean running = new AtomicBoolean(false);
	protected volatile boolean active   = false;
	protected volatile boolean stopping = false;

	protected final AtomicLong runCount = new AtomicLong(0L);



	public xThreadPoolWorker(final xThreadPool pool) {
		this(
			pool,
			null
		);
	}
	public xThreadPoolWorker(final xThreadPool pool, final Thread thread) {
		if (pool == null) throw new RequiredArgumentException("pool");
		this.pool = pool;
		this.workerIndex = pool.getNextWorkerIndex();
		if (thread != null) {
			this.thread.set(thread);
			this.configureThread(thread);
		}
	}



	// ------------------------------------------------------------------------------- //
	// run loop



	@Override
	public void start() {
		if (this.isRunning())
			return;
		if ( ! this.getThread().isAlive() ) {
			this.getThread()
				.start();
		}
	}
	public void startAndWait() {
		this.start();
		this.waitForStart(DEFAULT_WAIT_FOR_START);
	}
	public void startAndWait(final long timeout) {
		this.start();
		this.waitForStart(timeout);
	}
	public void waitForStart() {
		this.waitForStart(DEFAULT_WAIT_FOR_START);
	}
	public void waitForStart(final long timeout) {
		// wait for worker to start
		CoolDown cool = null;
		while (true) {
			// is running
			if (this.isRunning())
				break;
			// timeout
			if (cool == null) {
				cool = CoolDown.getNew(timeout);
				cool.resetRun();
			} else {
				if (cool.runAgain())
					break;
			}
			ThreadUtils.Sleep(20L);
		}
	}



	@Override
	public void stop() {
		this.stopping = true;
	}



	@Override
	public void run() {
		if (this.running.get()) return;
		this.pool.registerWorker(this);
		if ( ! this.running.compareAndSet(false, true) ) {
			try {
				this.pool.unregisterWorker(this);
			} catch (RuntimeException ignore) {}
			throw new RuntimeException("Thread pool worker already running!");
		}
		if (this.stopping) throw new IllegalStateException("Worker cannot run again, already stopped");
		if (this.thread.get() == null) {
			final Thread thread = Thread.currentThread();
			if (this.thread.compareAndSet(null, thread)) {
				this.configureThread(thread);
			}
		}
		if ( ! this.thread.get().equals(Thread.currentThread()) )
			throw new IllegalStateException("Invalid thread state!");
		try {
			// worker run loop
			OUTER_LOOP:
			while (true) {
				if (this.pool.isStopping())
					break OUTER_LOOP;
				// get task from queues
				try {
					final xThreadPoolTask<?> task =
						this.pool.grabNextTask();
					// run the task
					if (task != null) {
						this.runTask(task);
						this.runCount.incrementAndGet();
						continue OUTER_LOOP;
					}
				} catch (InterruptedException e) {
					this.log().trace(e);
					break;
				}
				// idle
				this.log()
					.detail("Idle thread..");
			} // end OUTER_LOOP
		} finally {
			this.stopping = true;
			this.pool.unregisterWorker(this);
			this.running.set(false);
		}
	}
	protected void runTask(final xThreadPoolTask<?> task) {
		if (task == null) throw new RequiredArgumentException("task");
		this.active = true;
		task.setWorker(this);
		task.run();
		this.active = false;
		if (task.hasException()) {
			this.log()
				.trace(task.getException());
		}
	}



	public Thread getThread() {
		// existing thread
		{
			final Thread thread = this.thread.get();
			if (thread != null)
				return thread;
		}
		// new thread
		{
			final Thread thread = new Thread(this);
			if ( ! this.thread.compareAndSet(null, thread) )
				return this.thread.get();
			this.configureThread(thread);
			this.log()
				.finer("New worker thread..");
			return thread;
		}
	}
	public void setThread(final Thread thread) {
		if ( ! this.thread.compareAndSet(null, thread) ) {
			final String threadName = this.thread.get().getName();
			throw new RuntimeException("Worker thread already set: "+threadName);
		}
	}
	protected void configureThread(final Thread thread) {
		thread.setDaemon(false);
		thread.setPriority(this.pool.getThreadPriority());
		thread.setName(this.getNameFormatted());
	}



	public void join(final long timeout)
			throws InterruptedException {
		final Thread thread = this.thread.get();
		if (thread == null)
			return;
		if (timeout > 0L) {
			thread.join(timeout);
		} else {
			thread.join();
		}
	}
	public void join() throws InterruptedException {
		this.join(0L);
	}



	// ------------------------------------------------------------------------------- //
	// worker state



	public boolean isRunning() {
		return this.running.get();
	}
	public boolean isActive() {
		return this.active;
	}
	public boolean isStopping() {
		return this.stopping;
	}



	public boolean isThread(final Thread match) {
		if (match == null)
			return false;
		return match.equals(this.getThread());
	}
	public boolean isCurrentThread() {
		return
			this.isThread(
				Thread.currentThread()
			);
	}



	// ------------------------------------------------------------------------------- //
	// config



	public String getNameFormatted() {
		return
			(new StringBuilder())
				.append(this.pool.getPoolName())
				.append("[w")
				.append(this.workerIndex)
				.append(']')
				.toString();
	}



	public long getWorkerIndex() {
		return this.workerIndex;
	}



	public void setPriority(final int priority) {
		final Thread thread = this.thread.get();
		if (thread == null) throw new NullPointerException();
		thread.setPriority(priority);
	}



	// ------------------------------------------------------------------------------- //
	// logger



	private volatile SoftReference<xLog> _log = null;
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null) {
				return log;
			}
		}
		final xLog log =
			this.pool.log()
				.getWeak(
					Long.toString(this.workerIndex)
				);
		this._log = new SoftReference<xLog>(log);
		return log;
	}



}
