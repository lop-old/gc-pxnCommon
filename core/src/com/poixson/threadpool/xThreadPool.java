package com.poixson.threadpool;

import java.lang.ref.SoftReference;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.poixson.abstractions.xStartable;
import com.poixson.exceptions.ContinueException;
import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.exceptions.UnknownThreadPoolException;
import com.poixson.logger.xLevel;
import com.poixson.logger.xLog;
import com.poixson.threadpool.xThreadPoolQueue.TaskPriority;
import com.poixson.tools.Keeper;
import com.poixson.tools.remapped.RemappedMethod;
import com.poixson.tools.remapped.xRunnable;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.Utils;


public abstract class xThreadPool implements xStartable {

	public static final int DEFAULT_MAX_WORKERS =
			Runtime.getRuntime().availableProcessors() + 1;
	public static final int GLOBAL_MAX_WORKERS = 50;

	// error messages
	private static final String ERR_ALREADY_STOPPING = "Cannot start thread pool, already stopping!";

	protected static final ConcurrentHashMap<String, xThreadPool> pools =
			new ConcurrentHashMap<String, xThreadPool>(3);
//TODO:

	protected final String poolName;

	protected final ThreadGroup threadGroup;
	protected volatile int threadPriority = Thread.NORM_PRIORITY;

	// pool state
	protected volatile boolean        stopping    = false;
	protected static volatile boolean stoppingAll = false;

	// stats/counts
	private final AtomicLong taskIndexCount   = new AtomicLong(0L);



	public static xThreadPool get(final String poolName) {
		final xThreadPool pool = pools.get(poolName);
		if (pool == null) throw new UnknownThreadPoolException(poolName);
		return pool;
	}



	protected xThreadPool(final String poolName) {
		if (stoppingAll)             throw new IllegalStateException(ERR_ALREADY_STOPPING);
		if (Utils.isEmpty(poolName)) throw new RequiredArgumentException("poolName");
		this.poolName = poolName;
		this.threadGroup = new ThreadGroup(this.getPoolName());
		// just to prevent gc
		Keeper.add(this);
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}



	// ------------------------------------------------------------------------------- //
	// start/stop workers



	@Override
	public void start() {
		if (stoppingAll) throw new IllegalStateException(ERR_ALREADY_STOPPING);
		// initial task (creates the first thread)
		this.runTaskLater(
			new xRunnable("xThreadPool-Startup") {
				@Override
				public void run() {
					xThreadPool.this
						.log().fine(
							(new StringBuilder())
							.append("Thread queue ")
							.append(xThreadPool.this.getPoolName())
							.append(" is running..")
							.toString()
						);
				}
			}
		);
	}
	@Override
	public void stop() {
		this.stopping = true;
		ThreadUtils.Sleep(20L);
	}



	public abstract void joinWorkers();
	public abstract void joinWorkers(final long timeout);



	public abstract void registerWorker(final xThreadPoolWorker worker);
	public abstract void unregisterWorker(final xThreadPoolWorker worker);






	// ------------------------------------------------------------------------------- //
	// run tasks



	protected abstract void startNewWorkerIfNeededAndAble();

	protected abstract LinkedBlockingQueue<xThreadPoolTask<?>> getQueueByPriority(final TaskPriority priority);

	// get next task from queue
	public abstract xThreadPoolTask<?> grabNextTask() throws InterruptedException;

//TODO:
	protected void checkTaskTimeouts(final long currentTime) {
	}



	// ------------------------------------------------------------------------------- //
	// queue task



	public abstract <V> Future<V> addTask(final Runnable run, final TaskPriority priority);
	public abstract <V> Future<V> addTask(final Callable<V> call, final TaskPriority priority);
	public abstract <V> Future<V> addTask(final xThreadPoolTask<V> task, final TaskPriority priority);



	// now (task)
	public abstract <V> V runTaskNow(final Runnable run);
	// later (task)
	public abstract <V> Future<V> runTaskLater(final Runnable run);
	// lazy (task)
	public abstract <V> Future<V> runTaskLazy(final Runnable run);



	// now (name, task)
	public abstract <V> V runTaskNow(final String name, final Runnable run);
	// later (name, task)
	public abstract <V> Future<V> runTaskLater(final String name, final Runnable run);
	// lazy (name, task)
	public abstract <V> Future<V> runTaskLazy(final String name, final Runnable run);



	/**
	 * Forces a method to be called from the correct thread.
	 * @param callingFrom Class object which contains the method.
	 * @param methodName The method which is being called.
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
	public <V> V forceResult(final Object callingFrom,
			final String methodName, final Object...args)
			throws ContinueException {
		if (callingFrom == null)       throw new RequiredArgumentException("callingFrom");
		if (Utils.isEmpty(methodName)) throw new RequiredArgumentException("methodName");
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
		this.runTaskNow(run);
		return run.getResult();
	}
	/**
	 * Forces a method to be called from the correct thread.
	 * @param callingFrom Class object which contains the method.
	 * @param methodName The method which is being called.
	 * @param now wait for the result.
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
	public boolean force(final Object callingFrom,
			final String methodName, final boolean now, final Object...args) {
		if (callingFrom == null)       throw new RequiredArgumentException("callingFrom");
		if (Utils.isEmpty(methodName)) throw new RequiredArgumentException("methodName");
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
		if (now) {
			this.runTaskNow(run);
		} else {
			this.runTaskLater(run);
		}
		return true;
	}



	// ------------------------------------------------------------------------------- //
	// config



	// pool name
	public String getPoolName() {
		return this.poolName;
	}



	// thread group
	public ThreadGroup getThreadGroup() {
		return this.threadGroup;
	}



	// thread priority
	public int getThreadPriority() {
		return this.threadPriority;
	}
	public xThreadPool setThreadPriority(final int priority) {
		this.threadPriority = priority;
		this.threadGroup.setMaxPriority(priority);
		return this;
	}



	// ------------------------------------------------------------------------------- //
	// which thread



	public boolean isMainPool() {
		return false;
	}
	public boolean isEventDispatchPool() {
		return false;
	}



	public boolean isCurrentThread() {
		return (this.getCurrentWorker() != null);
	}
	public abstract xThreadPoolWorker getCurrentWorker();



	// ------------------------------------------------------------------------------- //
	// pool state



	@Override
	public abstract boolean isRunning();
	@Override
	public boolean isStopping() {
		if (stoppingAll)
			return true;
		return this.stopping;
	}



	public abstract boolean isEmpty();



	// ------------------------------------------------------------------------------- //
	// stats



//TODO:
//	public void displayStats(final xLevel level) {
//		this.pool.log()
//			.publish(
//				level,
//				(new StringBuilder())
//					.append("Queued: [")
//						.append(this.getQueueCount())
//						.append("]  ")
//					.append("Threads: ")
//						.append(this.getCurrentThreadCount())
//						.append(" [")
//						.append(this.getMaxThreads())
//						.append("]  ")
//					.append("Active/Free: ")
//						.append(this.getActiveThreadCount())
//						.append("/")
//						.append(this.getInactiveThreadCount())
//						.append("  ")
//					.append("Global: ")
//						.append(getGlobalThreadCount())
//						.append(" [")
//						.append(getGlobalMaxThreads())
//						.append("]")
//						.toString()
//			);
//	}
	public abstract long getNextWorkerIndex();

	public long getNextTaskIndex() {
		return this.taskIndexCount
				.incrementAndGet();
	}



	// ------------------------------------------------------------------------------- //
	// logger



	private xLog _log = null;
	public xLog log() {
		if (this._log == null) {
			this._log =
				xLog.getRoot()
					.get(this.getPoolName());
		}
		return this._log;
	}



	// cached log level
	private volatile SoftReference<Boolean> _detail = null;
	public boolean isDetailedLogging() {
		if (this._detail != null) {
			final Boolean detail = this._detail.get();
			if (detail != null)
				return detail.booleanValue();
		}
		final boolean detail =
			this.log()
				.isLoggable(xLevel.DETAIL);
		this._detail = new SoftReference<Boolean>(Boolean.valueOf(detail));
		return detail;
	}



}
