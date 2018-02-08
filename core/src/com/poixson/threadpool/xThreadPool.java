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
import com.poixson.logger.xLog;
import com.poixson.logger.xLogRoot;
import com.poixson.threadpool.xThreadPoolQueue.TaskPriority;
import com.poixson.tools.Keeper;
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
					xThreadPool.this.log()
						.fine(
							"Thread queue {} is running..",
							xThreadPool.this
								.getPoolName()
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
//	protected void checkTaskTimeouts(final long currentTime) {
//	}



	// ------------------------------------------------------------------------------- //
	// queue task



	public abstract boolean force(
			final Object callingFrom, final String methodName,
			final TaskPriority priority, final Object...args);

	public abstract <V> V forceResult(
			final Object callingFrom, final String methodName,
			final TaskPriority priority, final Object...args)
			throws ContinueException;



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



	public abstract boolean imposeMainPool();
	public abstract void setImposeMainPool();



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
				xLogRoot.get()
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
		final boolean detail = this.log().isDetailLoggable();
		this._detail = new SoftReference<Boolean>(Boolean.valueOf(detail));
		return detail;
	}



}
