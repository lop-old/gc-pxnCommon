package com.poixson.utils.xThreadPool;

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.poixson.utils.xRunnable;
import com.poixson.utils.xTimeU;
import com.poixson.utils.xLogger.xLog;


public class xThreadPoolTask implements Runnable {

	private final xThreadPool pool;
	private volatile xThreadPoolWorker worker = null;
	private final xRunnable run;

	private static final AtomicLong taskCount = new AtomicLong(0L);
	private final long taskIndex;
	private final long runIndex;

	private volatile boolean running = false;
	private volatile boolean hasRun  = false;

	public final ReentrantLock lock = new ReentrantLock();
	public final Condition runLock = this.lock.newCondition();



	public xThreadPoolTask(final xThreadPool pool, final Runnable run) {
		if (pool == null) throw new IllegalArgumentException("pool");
		if (run  == null) throw new IllegalArgumentException("run");
		this.pool   = pool;
		this.run  = xRunnable.cast(run);
		this.taskIndex = taskCount.incrementAndGet();
		this.runIndex  = pool.runCount.incrementAndGet();
	}



	public void setWorker(final xThreadPoolWorker worker) {
		this.worker = worker;
	}



	@Override
	public void run() {
		final Thread currentThread = Thread.currentThread();
		final String originalThreadName = currentThread.getName();
		this.running = true;
		this.pool.activeCount
			.incrementAndGet();
		final int runIndex = this.pool.runCount.incrementAndGet();
//TODO: maybe use this?
//.append("run:").append(runIndex).append(" ")
//.append("name:").append(threadName).append(" ")
//.append("task:").append(task.getTaskName())
		{
			final StringBuilder threadName = new StringBuilder();
			threadName
				.append(runIndex)
				.append(':');
			final xThreadPoolWorker worker = this.worker;
			if (worker != null)
				threadName
					.append(worker.getWorkerIndex())
					.append(':');
			threadName
				.append(this.getTaskName());
			currentThread.setName(threadName.toString());
		}
		try {
			this.run.run();
		} catch (Exception e) {
			this.log()
				.trace(e);
		}
		this.hasRun  = true;
		this.running = false;
		this.pool.activeCount
			.decrementAndGet();
		currentThread.setName(originalThreadName);
		try {
			this.runLock.signalAll();
		} catch (IllegalMonitorStateException ignore) {}
	}



	public void await() {
		this.lock.lock();
		try {
			while (true) {
				if (this.hasRun())
					break;
				this.runLock.await(
					100L,
					xTimeU.MS
				);
			}
		} catch (InterruptedException e) {
			this.log()
				.trace(e);
			return;
		} finally {
			this.lock.unlock();
		}
	}



	public boolean isRunning() {
		return this.running;
	}
	public boolean hasRun() {
		return this.hasRun;
	}



	public String getTaskName() {
		return this.run.getTaskName();
	}



	public long getRunIndex() {
		return this.runIndex;
	}
	public long getTaskIndex() {
		return this.taskIndex;
	}



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
					(new StringBuilder())
						.append(this.runIndex)
						.append(this.taskIndex)
						.toString()
				);
		this._log = new SoftReference<xLog>(log);
		return log;
	}



}
