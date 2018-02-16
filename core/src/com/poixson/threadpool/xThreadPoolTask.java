package com.poixson.threadpool;

import java.lang.ref.SoftReference;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.xLog;
import com.poixson.tools.remapped.RunnableNamed;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class xThreadPoolTask<V> implements Future<V>, RunnableNamed {

	private final xThreadPool pool;
	private final String taskName;

	private final    long taskIndex;
	private volatile long runIndex;

	private final FutureTask<V> future;
	private volatile Exception ex = null;

	// state
	private final AtomicBoolean running = new AtomicBoolean(false);

	// worker owning this task
	private final AtomicReference<xThreadPoolWorker> worker =
			new AtomicReference<xThreadPoolWorker>(null);



	public xThreadPoolTask(final xThreadPool pool, final Callable<V> call) {
		this(
			pool,
			call,
			RunnableNamed.GetName(call)
		);
	}
	public xThreadPoolTask(final xThreadPool pool,
			final Callable<V> call, final String taskName) {
		this(
			pool,
			new FutureTask<V>(call),
			taskName
		);
	}
	public xThreadPoolTask(final xThreadPool pool,
			final FutureTask<V> future, final String taskName) {
		if (pool   == null) throw new RequiredArgumentException("pool");
		if (future == null) throw new RequiredArgumentException("future");
		this.pool   = pool;
		this.future = future;
		this.taskIndex = this.pool.getNextTaskIndex();
		this.taskName = ( Utils.isEmpty(taskName) ? null : taskName );
	}



	// run the task
	@Override
	public void run() {
		if (this.future.isCancelled())
			return;
		if ( ! this.running.compareAndSet(false, true) )
			throw new IllegalStateException("Task already running");
		final Thread currentThread = Thread.currentThread();
		final String originalThreadName = currentThread.getName();
		final xLog log = this.log();
		try {
			if (this.future.isCancelled()) return;
			if (this.future.isDone()) throw new IllegalStateException("Task already done");
			// set thread name
			final String threadName;
			{
				final StringBuilder str = new StringBuilder();
				if (Utils.isEmpty(this.taskName)) {
					str.append("Task")
						.append(this.taskIndex);
				} else {
					str.append(this.taskName);
				}
				threadName = str.toString();
			}
			currentThread.setName(threadName);
			log.finest("Running task:", threadName);
			// run the task
			try {
				this.future.run();
				this.future.get();
			} catch (Exception e) {
				this.ex = e;
				this.future.cancel(false);
			}
		} finally {
			// finished task
			this.running.set(false);
			// restore thread name
			currentThread.setName(originalThreadName);
		}
	}



	// ------------------------------------------------------------------------------- //
	// get result



	@Override
	public V get() throws InterruptedException, ExecutionException {
		if (this.ex != null)
			throw new ExecutionException(this.ex);
		final Future<V> future = this.future;
		if (future == null)
			return null;
		return future.get();
	}
	@Override
	public V get(final long timeout, final TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		if (this.ex != null)
			throw new ExecutionException(this.ex);
		final Future<V> future = this.future;
		if (future == null)
			return null;
		return future.get(timeout, unit);
	}



	public boolean hasException() {
		return (this.ex != null);
	}
	public Exception getException() {
		return this.ex;
	}



	// ------------------------------------------------------------------------------- //
	// task state



	public boolean isRunning() {
		return this.running.get();
	}
	@Override
	public boolean isDone() {
		return this.future
				.isDone();
	}



	public boolean cancel() {
		return this.cancel(false);
	}
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return this.future
				.cancel(mayInterruptIfRunning);
	}
	@Override
	public boolean isCancelled() {
		if (this.ex != null)
			return true;
		return this.future
				.isCancelled();
	}



	// ------------------------------------------------------------------------------- //
	// config



	@Override
	public String getTaskName() {
		return this.taskName;
	}
	@Override
	public void setTaskName(final String name) {
		throw new UnsupportedOperationException("Cannot change task name!");
	}
	@Override
	public boolean taskNameEquals(final String name) {
		if (Utils.isEmpty(name))
			return false;
		return name.equals(this.getTaskName());
	}



	public xThreadPoolWorker getWorker() {
		final xThreadPoolWorker worker = this.worker.get();
		if (worker == null) {
			throw new NullPointerException(
				StringUtils.ReplaceTags(
					"Task doesn't have a worker set! This should be handled by the thread pool!",
					this.getTaskName()
				)
			);
		}
		return worker;
	}
	public void setWorker(final xThreadPoolWorker worker) {
		if (worker == null) throw new RequiredArgumentException("worker");
		if ( ! this.worker.compareAndSet(null, worker))
			throw new RuntimeException("worker already set!");
	}



	public Future<V> getFuture() {
		return this.future;
	}
	public xThreadPool getPool() {
		return this.pool;
	}



	public long getTaskIndex() {
		return this.taskIndex;
	}
	public long getRunIndex() {
		return this.runIndex;
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
		{
			final xLog log =
				this.worker.get()
					.log();
			this._log = new SoftReference<xLog>(log);
			return log;
		}
	}



}
