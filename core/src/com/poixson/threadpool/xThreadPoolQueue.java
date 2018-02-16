package com.poixson.threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.poixson.exceptions.ContinueException;
import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.threadpool.types.xThreadPool_Main;
import com.poixson.tools.remapped.RemappedMethod;
import com.poixson.tools.remapped.xCallable;
import com.poixson.tools.remapped.xRunnable;
import com.poixson.utils.Utils;


public abstract class xThreadPoolQueue extends xThreadPool {

	public static final long DEFAULT_MAX_LOOP_WAIT = 25L;

	public static final int  DEFAULT_ADD_QUEUE_MAX_ATTEMPTS = 4;
	public static final long DEFAULT_ADD_QUEUE_TIMEOUT      = 250L;

	public enum TaskPriority {
		LOW,
		NORM,
		HIGH
	};

	// run now queue
	private final LinkedBlockingQueue<xThreadPoolTask<?>> queueHigh =
			new LinkedBlockingQueue  <xThreadPoolTask<?>>();
	// run later queue
	private final LinkedBlockingQueue<xThreadPoolTask<?>> queueNorm =
			new LinkedBlockingQueue  <xThreadPoolTask<?>>();
	// run lazy queue
	private final LinkedBlockingQueue<xThreadPoolTask<?>> queueLow =
			new LinkedBlockingQueue  <xThreadPoolTask<?>>();



	protected xThreadPoolQueue(final String poolName) {
		super(poolName);
	}



	// ------------------------------------------------------------------------------- //
	// run tasks



	@Override
	protected LinkedBlockingQueue<xThreadPoolTask<?>> getQueueByPriority(
			final TaskPriority priority) {
		if (priority == null) throw new RequiredArgumentException("priority");
		switch (priority) {
		case HIGH: // now
			return this.queueHigh;
		case NORM: // later
			return this.queueNorm;
		case LOW:  // lazy
			return this.queueLow;
		}
		throw new UnsupportedOperationException("Unknown task priority: "+priority.toString());
	}



	// get next task from queue
	@Override
	public xThreadPoolTask<?> grabNextTask()
			throws InterruptedException {
		OUTER_LOOP:
		while (true) {
			boolean moreLowPossible = false;
			// loop a few times (pool scope)
			IDLE_LOOP:
			while (true) {
				if (this.isStopping())
					break OUTER_LOOP;
				// check for high priority tasks
				{
					final xThreadPoolTask<?> task = this.queueHigh.poll();
					if (task != null)
						return task;
				}
				// check for normal priority tasks
				{
					final xThreadPoolTask<?> task = this.queueNorm.poll();
					if (task != null)
						return task;
				}
				// check for low priority tasks (before waiting on high)
				if (moreLowPossible)
					break IDLE_LOOP;
				// wait for high priority tasks
				{
					final xThreadPoolTask<?> task;
					task = this.queueHigh.poll(
						DEFAULT_MAX_LOOP_WAIT,
						TimeUnit.MILLISECONDS
					);
					if (task != null)
						return task;
				}
					break IDLE_LOOP;
			}
			// check for low priority tasks
			{
				final xThreadPoolTask<?> task = this.queueLow.poll();
				if (task == null) {
					moreLowPossible = false;
				} else {
					moreLowPossible = true;
					return task;
				}
			}
		} // end OUTER_LOOP
		return null;
	}



	// ------------------------------------------------------------------------------- //
	// queue task



	/**
	 * Forces a method to be called from the correct thread.
	 * @param callingFrom Class object which contains the method.
	 * @param methodName The method which is being called.
	 * @param priority
	 * @param args Arguments being passed to the method.
	 * @return false if already in the correct thread;
	 *   true if calling from some other thread. this will queue
	 *   a task to call the method in the correct thread and return
	 *   true to signal bypassing the method following.
	 * Example:
	 * public void getSomething() {
	 *     if (xThreadPool_Main.get()
	 *         .force(this, "getSomething", false))
	 *             return;
	 *     // do something here
	 * }
	 */
	@Override
	public boolean force(
			final Object callingFrom, final String methodName,
			final TaskPriority priority, final Object...args) {
		if (callingFrom == null)       throw new RequiredArgumentException("callingFrom");
		if (Utils.isEmpty(methodName)) throw new RequiredArgumentException("methodName");
		if (priority == null)          throw new RequiredArgumentException("priority");
		// already running in correct thread
		if (this.isCurrentThread())
			return false;
		// queue to run in correct thread
		final RemappedMethod<Object> run =
			new RemappedMethod<Object>(
				callingFrom,
				methodName,
				args
			);
		this.addTask(run, priority);
		return true;
	}
	@Override
	public boolean force(
			final Object callingFrom, final String methodName,
			final Object...args) {
		return
			this.force(
				callingFrom,
				methodName,
				TaskPriority.NORM,
				args
			);
	}



	/**
	 * Forces a method to be called from the correct thread.
	 * @param callingFrom Class object which contains the method.
	 * @param methodName The method which is being called.
	 * @param priority
	 * @param args Arguments being passed to the method.
	 * @return resulting return value if not in the correct thread.
	 *   this will queue a task to run in the correct thread.
	 *   if already in the correct thread, ContinueException is
	 *   throws to signal to continue running the method following.
	 * Example:
	 * public boolean getSomething() {
	 *     try {
	 *         return xThreadPool_Main.get()
	 *             .forceResult(this, "getSomething");
	 *     } catch (ContinueException ignore) {}
	 *     // do something here
	 *     return result;
	 * }
	 */
	@Override
	public <V> V forceResult(
			final Object callingFrom, final String methodName,
			final TaskPriority priority, final Object...args)
			throws ContinueException {
		if (callingFrom == null)       throw new RequiredArgumentException("callingFrom");
		if (Utils.isEmpty(methodName)) throw new RequiredArgumentException("methodName");
		if (priority == null)          throw new RequiredArgumentException("priority");
		// already running in correct thread
		if (this.isCurrentThread())
			throw new ContinueException();
		// queue to run in correct thread
		final RemappedMethod<V> run =
			new RemappedMethod<V>(
				callingFrom,
				methodName,
				args
			);
		this.addTask(run, priority);
		return run.getResult();
	}
	@Override
	public <V> V forceResult(
			final Object callingFrom, final String methodName,
			final Object...args) throws ContinueException {
		return
			this.forceResult(
				callingFrom,
				methodName,
				TaskPriority.HIGH,
				args
			);
	}



	@Override
	public <V> Future<V> addTask(final Runnable run, final TaskPriority priority) {
		if (run == null) throw new RequiredArgumentException("run");
		return
			this.addTask(
				new xThreadPoolTask<V>(
					this,
					(new xCallable<V>(run))
				),
				priority
			);
	}
	@Override
	public <V> Future<V> addTask(final Callable<V> call, final TaskPriority priority) {
		if (call == null) throw new RequiredArgumentException("call");
		return
			this.addTask(
				new xThreadPoolTask<V>(
					(xThreadPool) this,
					call
				),
				priority
			);
	}
	@Override
	public <V> Future<V> addTask(final xThreadPoolTask<V> task, final TaskPriority priority) {
		if (task == null) throw new RequiredArgumentException("task");
		// pass to main thread pool
		if (this.imposeMainPool()) {
			return
				xThreadPool_Main.get()
					.addTask(task, priority);
		}
		final TaskPriority pr = (
			priority == null
			? TaskPriority.NORM
			: priority
		);
		// run now as current thread
		if (TaskPriority.HIGH.equals(pr)) {
			if (this.isCurrentThread()) {
				final xThreadPoolWorker worker = this.getCurrentWorker();
				if (worker == null) throw new RuntimeException("Failed to get current worker!");
				// just run it
				task.setWorker(worker);
				task.run();
				return task.getFuture();
			}
		}
		// get task queue (default to normal/later)
		final LinkedBlockingQueue< xThreadPoolTask<?> > queue =
			this.getQueueByPriority(pr);
		// add task to queue
		{
			boolean result = false;
			QUEUE_LOOP:
			for (int i=0; i<DEFAULT_ADD_QUEUE_MAX_ATTEMPTS; i++) {
				try {
					result = queue.offer(
						task,
						DEFAULT_ADD_QUEUE_TIMEOUT,
						TimeUnit.MILLISECONDS
					);
					if (result) break QUEUE_LOOP;
				} catch (InterruptedException ignore) {}
				// failed to queue task
				this.log()
					.warning(
						"Thread queue jammed!",
						( i>0 ? " attempt "+Integer.toString(i+1) : "" )
					);
			}
			// failed to queue (after x attempts)
			if (!result) {
				// try a lower priority
				switch (priority) {
				case HIGH:
					this.log().warning("Thread queue jammed, trying a lower priority.. (high->norm)");
					return this.addTask(task, TaskPriority.NORM);
				case NORM:
					this.log().warning("Thread queue jammed, trying a lower priority.. (norm->low)");
					return this.addTask(task, TaskPriority.LOW);
				default:
					throw new RuntimeException("Thread queue jammed! Failed to queue task: "+task.getTaskName());
				}
			}
		}
		// new worker if needed
		this.startNewWorkerIfNeededAndAble();
		try {
			return (Future<V>) task.getFuture();
		} catch (Exception e) {
			this.log().trace(e);
		}
		return null;
	}



	// now (task)
	@Override
	public <V> V runTaskNow(final Runnable run) {
		final Future<V> future =
			this.addTask(
				run,
				TaskPriority.HIGH
			);
		try {
			return future.get();
		} catch (InterruptedException ignore) {
		} catch (ExecutionException ignore) {
		}
		return null;
	}
	// later (task)
	@Override
	public <V> Future<V> runTaskLater(final Runnable run) {
		return
			this.addTask(
				run,
				TaskPriority.NORM
			);
	}
	// lazy (task)
	@Override
	public <V> Future<V> runTaskLazy(final Runnable run) {
		return
			this.addTask(
				run,
				TaskPriority.LOW
			);
	}



	// now (name, task)
	@Override
	public <V> V runTaskNow(final String name, final Runnable run) {
		return
			this.runTaskNow(
				new xRunnable(name, run)
			);
	}
	// later (name, task)
	@Override
	public <V> Future<V> runTaskLater(final String name, final Runnable run) {
		return
			this.runTaskLater(
				new xRunnable(name, run)
			);
	}
	// lazy (name, task)
	@Override
	public <V> Future<V> runTaskLazy(final String name, final Runnable run) {
		return
			this.runTaskLazy(
				new xRunnable(name, run)
			);
	}



	// ------------------------------------------------------------------------------- //
	// config



	// ------------------------------------------------------------------------------- //
	// state



	/**
	 * Are task queues empty.
	 * @return true if all task queues are empty.
	 */
	@Override
	public boolean isEmpty() {
		if ( ! this.queueLow.isEmpty()  ) return false;
		if ( ! this.queueNorm.isEmpty() ) return false;
		if ( ! this.queueHigh.isEmpty() ) return false;
		return true;
	}



}
