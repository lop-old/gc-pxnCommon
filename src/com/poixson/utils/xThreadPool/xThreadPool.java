package com.poixson.utils.xThreadPool;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.poixson.utils.CoolDown;
import com.poixson.utils.Keeper;
import com.poixson.utils.NumberUtils;
import com.poixson.utils.ReflectUtils;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.xRunnable;
import com.poixson.utils.xStartable;
import com.poixson.utils.xTime;
import com.poixson.utils.xTimeU;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLevel;
import com.poixson.utils.xLogger.xLog;


public class xThreadPool implements xStartable {

	protected static final xTime THREAD_LOOP_TIME        = xTime.get("1s");
	protected static final xTime INACTIVE_THREAD_TIMEOUT = xTime.get("10s");
	protected static final int GLOBAL_POOL_SIZE_LIMIT = 50;
	protected static final int MAX_QUEUE_SIZE = 1000;

	// main thread pool
	public static final String MAIN_POOL_NAME = "main";
	private final boolean isMainPool;

	// named pool
	private final String poolName;
	private volatile int poolSize = 1;

	// pool state
	private final AtomicBoolean running  = new AtomicBoolean(false);
	private volatile boolean    stopping = false;

	// counts/stats
	protected final AtomicInteger workerCount = new AtomicInteger(0);
	protected final AtomicInteger activeCount = new AtomicInteger(0);
	protected final AtomicInteger runCount    = new AtomicInteger(0);

	// worker threads
	protected final CopyOnWriteArraySet<xThreadPoolWorker> workers =
			new CopyOnWriteArraySet<xThreadPoolWorker>();
	protected final ThreadGroup group;
	protected volatile int priority = Thread.NORM_PRIORITY;

	// queues
	private final LinkedBlockingQueue<xThreadPoolTask> queueNorm;
	private final ConcurrentLinkedQueue<xThreadPoolTask> queueHigh;
	private final ConcurrentLinkedQueue<xThreadPoolTask> queueLow;

	// warning cool-down
	private CoolDown coolMaxReached       = CoolDown.get("5s");
	private CoolDown coolGlobalMaxReached = CoolDown.get("5s");



	protected xThreadPool(final String poolName) {
		if (Utils.isEmpty(poolName)) throw new RequiredArgumentException("poolName");
		this.isMainPool = MAIN_POOL_NAME.equalsIgnoreCase(poolName);
		this.poolName = (
			this.isMainPool
			? MAIN_POOL_NAME
			: poolName
		);
		this.group = new ThreadGroup(this.poolName);
		// queues
		this.queueNorm = new LinkedBlockingQueue<xThreadPoolTask>();
		this.queueHigh = new ConcurrentLinkedQueue<xThreadPoolTask>();
		this.queueLow  = new ConcurrentLinkedQueue<xThreadPoolTask>();
		// just to prevent gc
		if (this.isMainPool) {
			Keeper.add(this);
		}
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}



	// ------------------------------------------------------------------------------- //
	// running and threads



	@Override
	public void Start() {
		if (!this.running.compareAndSet(false, true))
			return;
		// initial task (creates the first thread)
		this.runLater(
			new xRunnable("xThreadPool-Startup") {
				private volatile xLog log = null;
				public xRunnable init(final xLog log) {
					this.log = log;
					return this;
				}
				@Override
				public void run() {
					this.log.fine("Thread queue is running..");
				}
			}.init(this.log())
		);
		ThreadUtils.Sleep(20L);
	}
	@Override
	public void Stop() {
		this.stopping = true;
		ThreadUtils.Sleep(50L);
		final Iterator<xThreadPoolWorker> it =
			this.workers.iterator();
		while (it.hasNext()) {
			it.next()
				.Stop();
		}
	}



	/**
	 * Create a new thread if needed, skip if queue is empty.
	 * @return true if new thread has been created,
	 *         false if using existing inactive thread,
	 *         null if stopping or thread limit reached.
	 */
	protected Boolean newThread() {
		final boolean detailed = this.log().isLoggable(xLevel.DETAIL);
		if (this.isStopping()) {
			if (detailed) {
				this.log().warning("thread pool is stopping; cannot start new thread as requested");
			}
			return null;
		}
		// check worker thread limits
		{
//TODO:
//			if (detail) {
//				this.displayStats();
//			}
			// use existing inactive thread
			final int inactiveThreads = this.getInactiveThreadCount();
			if (inactiveThreads > 0) {
				return Boolean.FALSE;
			}
			// check max threads (this pool)
			final int currentThreads  = this.getCurrentThreadCount();
			final int maxThreads      = this.getMaxThreadCount();
			if (currentThreads >= maxThreads) {
				if (maxThreads > 1) {
					if (this.coolMaxReached.runAgain() || detailed) {
						this.msgLimitReached(false);
					}
				}
				return null;
			}
			// check max threads (global)
			final int globalThreads    = getGlobalThreadCount();
			final int globalMaxThreads = getGlobalMaxThreads();
			if (globalThreads >= globalMaxThreads) {
				if (this.coolGlobalMaxReached.runAgain() || detailed) {
					this.msgLimitReached(true);
				}
				return null;
			}
			// increment and final check
			final int count = this.workerCount.incrementAndGet();
			if (count > maxThreads) {
				this.workerCount.decrementAndGet();
				if (maxThreads > 1) {
					if (this.coolMaxReached.runAgain() || detailed) {
						this.msgLimitReached(false);
					}
				}
				return null;
			}
		}
		// start new worker thread
		{
			final xThreadPoolWorker worker =
				new xThreadPoolWorker(this);
			this.workers
				.add(worker);
			worker.start();
		}
		// ok to create a new worker thread
		return Boolean.TRUE;
	}
	protected void msgLimitReached(final boolean isGlobal) {
		final int maxThreads =
			isGlobal
			? getGlobalMaxThreads()
			: this.getMaxThreadCount();
		this.log().warning(
			(new StringBuilder())
				.append(
					isGlobal
					? "Global max"
					: "Max"
				)
				.append(" threads limit [ ")
				.append(maxThreads)
				.append(" ] reached!")
				.toString()
		);
	}



	// set thread priority
	public void setThreadPriority(final int priority) {
		if (priority > this.group.getMaxPriority()) {
			this.group.setMaxPriority(priority);
		}
		this.priority = priority;
		ThreadUtils.Sleep(20L);
		final Iterator<xThreadPoolWorker> it =
			this.workers.iterator();
		while (it.hasNext()) {
			it.next()
				.setPriority(priority);
		}
		if (priority < this.group.getMaxPriority()) {
			this.group.setMaxPriority(priority);
		}
	}



	@Override
	public void run() {
		if (this.getMaxThreadCount() > 1)
			throw new UnsupportedOperationException();
		if (this.getCurrentThreadCount() > 0)
			throw new IllegalStateException("Cannot run thread pool, already started?!");
		this.running.set(true);
		// create new worker
		final xThreadPoolWorker worker =
			new xThreadPoolWorker(this);
		this.workers
			.add(worker);
		// pass current thread to worker
		worker.run();
		// stopping
		this.stopping = true;
		this.running.set(false);
	}



	protected xThreadPoolTask getTaskToRun() throws InterruptedException {
		xThreadPoolTask task = null;
		// highest priority queue
		task = this.queueHigh.poll();
		if (task != null)
			return task;
		// normal priority queue
		task = this.queueNorm.poll(
			THREAD_LOOP_TIME.getMS(),
			xTimeU.MS
		);
		if (task != null)
			return task;
		// low priority queue
		task = this.queueLow.poll();
		if (task != null)
			return task;
		return null;
	}



//TODO:
	protected void checkTaskTimeouts(final long currentTime) {
	}



	// ------------------------------------------------------------------------------- //
	// tasks



	// run a task as soon as possible
	public void runNow(final Runnable run) {
		if (run == null) throw new RequiredArgumentException("run");
		// run in main thread pool
		if (!this.isMainPool()) {
			if (this.getMaxThreadCount() == 0) {
				xThreadPoolFactory.getMainPool()
					.runNow(run);
				return;
			}
		}
		// queue task to run
		final xThreadPoolTask task =
			new xThreadPoolTask(
				this,
				run
			);
		this.queueHigh
			.offer(task);
		// make sure there's a thread
		this.newThread();
		// wait for task to finish
		task.await();
	}



	// queue a task
	public void runLater(final Runnable run) {
		if (run == null) throw new RequiredArgumentException("run");
//TODO:
//		// pass to main pool
//		if (this.getMaxThreads() < 1 && !this.isMainPool) {
//			getMainPool().runLater(task);
//			return;
//		}
		// run in main thread pool
		if (!this.isMainPool()) {
			if (this.getMaxThreadCount() == 0) {
				xThreadPoolFactory.getMainPool()
					.runLater(run);
				return;
			}
		}
		// queue task to run
		final xThreadPoolTask task =
			new xThreadPoolTask(
				this,
				run
			);
		try {
			final boolean result =
				this.queueNorm.offer(
					task,
					5L,
					xTimeU.S
				);
			if (!result) {
				this.log().warning("Thread queue jammed! "+task.getTaskName());
				throw new InterruptedException("queue-jam");
			}
		} catch (InterruptedException e) {
			this.log()
				.trace(e);
			return;
		}
		this.log().detail("Task queued: "+task.getTaskName());
		// make sure there's a thread
		this.newThread();
	}



	// ------------------------------------------------------------------------------- //
	// counts / stats



	public String getName() {
		return this.poolName;
	}
	public boolean isMainPool() {
		return this.isMainPool;
	}



	@Override
	public boolean isRunning() {
		if (this.isStopping())
			return false;
		return this.running.get();
	}
	@Override
	public boolean isStopping() {
		return this.stopping;
	}



	/**
	 * Are task queues empty.
	 * @return true if all task queues are empty.
	 */
	public boolean isEmpty() {
		if (!this.queueLow.isEmpty())
			return false;
		if (!this.queueNorm.isEmpty())
			return false;
		if (!this.queueHigh.isEmpty())
			return false;
		return true;
	}
	/**
	 * Is thread pool busy.
	 * @return true if pool contains active threads.
	 */
	public boolean isActive() {
		return (this.getActiveThreadCount() > 0);
	}



	// thread count



	public int getCurrentThreadCount() {
		return this.workerCount.get();
	}
	public int getActiveThreadCount() {
		return this.activeCount.get();
	}
	public int getInactiveThreadCount() {
		final int count = this.getCurrentThreadCount() - this.getActiveThreadCount();
		return
			NumberUtils.MinMax(
				count,
				0,
				this.getMaxThreadCount()
			);
	}
	public int getThreadCount() {
		return this.workerCount.get();
	}
	public int getThreadsFree() {
		final int count = this.getMaxThreadCount() - this.getCurrentThreadCount();
		return count;
	}



	public int getMaxThreadCount() {
		return this.poolSize;
	}
	/**
	 * Set the maximum number of threads for this pool.
	 * @param size max number of threads allowed.
	 */
	//TODO:
	public void setPoolSize(final int poolSize) {
		this.poolSize =
			NumberUtils.MinMax(
				poolSize,
				( this.isMainPool() ? 1 : 0 ),
				getGlobalMaxThreads()
			);
	}



	// queue size



	public int getQueueCount() {
		int count = 0;
		count += this.queueLow.size();
		count += this.queueNorm.size();
		count += this.queueHigh.size();
		return count;
	}
	public int getMaxQueueSize() {
		return MAX_QUEUE_SIZE;
	}



	// global counts



	public static int getGlobalThreadCount() {
		if (xThreadPoolFactory.pools.isEmpty())
			return 0;
		int count = 0;
		final Iterator<xThreadPool> it =
			xThreadPoolFactory.pools
				.values().iterator();
		while (it.hasNext()) {
			count += it.next()
					.getCurrentThreadCount();
		}
		return count;
	}
	public static int getGlobalMaxThreads() {
		return GLOBAL_POOL_SIZE_LIMIT;
	}



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



	// ------------------------------------------------------------------------------- //



	// logger
	private volatile SoftReference<xLog> _log = null;
	private volatile String _className = null;
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null)
				return log;
		}
		if (this._className == null) {
			this._className =
				ReflectUtils.getClassName(
					this.getClass()
				);
		}
		final xLog log =
			xLog.getRoot()
				.get(
					(new StringBuilder())
						.append(this._className)
						.append(':')
						.append(this.getName())
						.toString()
				);
		this._log = new SoftReference<xLog>(log);
		return log;
	}



}
