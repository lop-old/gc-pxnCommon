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
import com.poixson.utils.xLogger.xLog;


public class xThreadPool implements xStartable {
	public static final boolean DETAILED_LOGGING = false;

	protected static final xTime THREAD_LOOP_TIME        = xTime.get("1s");
	protected static final xTime INACTIVE_THREAD_TIMEOUT = xTime.get("10s");
	protected static final int GLOBAL_POOL_SIZE_LIMIT = 50;
	protected static final int MAX_QUEUE_SIZE = 1000;

	// main thread pool
	public static final String MAIN_POOL_NAME = "main";
	protected final boolean isMainPool;

	// named thread pool
	protected final String poolName;
	protected volatile int poolSize;

	// pool state
	protected final AtomicBoolean running  = new AtomicBoolean(false);
	protected volatile boolean    stopping = false;

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
	protected CoolDown coolMaxReached = CoolDown.get("5s");

	private CoolDown coolGlobalMaxReached = CoolDown.get("5s");



	protected xThreadPool(final String poolName, final int poolSize) {
		if (Utils.isEmpty(poolName)) throw new RequiredArgumentException("poolName");
		if (poolSize < 1)            throw new IllegalArgumentException("Invalid pool size: "+Integer.toString(poolSize));
		this.isMainPool = MAIN_POOL_NAME.equals(poolName);
		this.poolName = poolName;
		this.poolSize =
			NumberUtils.MinMax(
				poolSize,
				(
					this.isMainPool
					? 1
					: 0
				),
				GLOBAL_POOL_SIZE_LIMIT
			);
	}



	//TODO:
	// ------------------------------------------------------------------------------- //
	// cast to task



//	// warning: checking pools of more than 1 thread can hurt performance.
//	public boolean isPoolThread() {
//		if (this.threadCount.get() == 0)
//			return false;
//		final Thread thread = Thread.currentThread();
//		// main pool
//		if (this.isMainPool())
//			return thread.equals(mainPool);
//		// check threads in this pool
//		for (final Thread t : this.threads) {
//			if (thread.equals(t))
//				return true;
//		}
//		return false;
//	}
	public boolean forcePoolThread(final String className,
			final String methodName, final Object...args) {
		if (Utils.isEmpty(className))  throw new RequiredArgumentException("className");
		if (Utils.isEmpty(methodName)) throw new RequiredArgumentException("methodName");
		final Object targetClass;
		try {
			targetClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Invalid class: "+className, e);
		}
		if (targetClass == null)
			throw new IllegalArgumentException("Invalid class: "+className);
		return forcePoolThread(
			targetClass,
			methodName,
			args
		);
	}
	public boolean forcePoolThread(final Object targetClass,
			final String methodName, final Object...args) {
		if (targetClass == null)       throw new RequiredArgumentException("targetClass");
		if (Utils.isEmpty(methodName)) throw new RequiredArgumentException("methodName");
		final Method targetMethod =
			ReflectUtils.getMethodByName(
				targetClass,
				methodName,
				args
			);
		if (targetMethod == null)
			throw new IllegalArgumentException("Invalid method name: "+methodName);
		return forcePoolThread(
			targetClass,
			targetMethod,
			args
		);
	}
	public boolean forcePoolThread(final Object targetClass,
			final Method targetMethod, final Object...args) {
		if (targetClass == null)  throw new RequiredArgumentException("targetClass");
		if (targetMethod == null) throw new RequiredArgumentException("targetMethod");
//TODO:
//		if (this.isPoolThread()) {
//			return false;
//		}
		final String taskName =
			(new StringBuilder())
				.append("Force: ")
				.append(targetClass.getClass().getName())
				.append("->")
				.append(targetMethod.getName())
				.append("()")
				.toString();

		final xRunnable run = new xRunnable(taskName) {

			private volatile Object targetClass  = null;
			private volatile Method targetMethod = null;
			private volatile Object[] args       = null;
			public xRunnable init(final Object targetClass,
					final Method targetMethod, final Object[] args) {
				this.targetClass  = targetClass;
				this.targetMethod = targetMethod;
				this.args         = args;
				return this;
			}

			@Override
			public void run() {
				ReflectUtils.InvokeMethod(
					this.targetClass,
					this.targetMethod,
					this.args
				);
			}

		}.init(
			targetClass,
			targetMethod,
			args
		);

		this.runLater(run);
		return true;
	}



	// ------------------------------------------------------------------------------- //
	// running and threads



	@Override
	public void Start() {
//TODO:
//		if (this.isMainPool()) {
//			if (mainThread == null) {
//				new Thread() {
//					@Override
//					public void run() {
//						getMainPool().run();
//					}
//				}.start();
//				this.log().fine("Started main thread internally..");
//				utilsThread.Sleep(10L);
//			}
//			return;
//		}
		if (!this.running.compareAndSet(false, true)) {
			return;
		}
		// initial task
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



	public boolean isStopping() {
		return this.stopping;
	}
	@Override
	public boolean isRunning() {
		if (this.isStopping())
			return false;
		return this.running.get();
	}
	public boolean isActive() {
		return ! this.threads.isEmpty();
	}



	public String getName() {
		return this.poolName;
	}
	public boolean isMainPool() {
		return this.isMainPool;
	}



	protected void checkTaskTimeouts(final long currentTime) {
//TODO:
	}



	/**
	 * Create a new thread if needed, skip if queue is empty.
	 * @return true if new thread has been created,
	 *         false if using existing inactive thread,
	 *         null if stopping or thread limit reached.
	 */
	protected Boolean newThread() {
		if (this.isStopping()) {
			if (DETAILED_LOGGING) {
				this.log().warning("thread pool is stopping; cannot start new thread as requested");
			}
			return null;
		}
		// thread count limits
		{
//TODO:
//			if (DETAILED_LOGGING) {
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
					if (this.coolMaxReached.runAgain() || DETAILED_LOGGING) {
						this.log().warning(
							(new StringBuilder())
								.append("Max threads limit [ ")
								.append(maxThreads)
								.append(" ] reached!")
								.toString()
						);
					}
				}
				return null;
			}
			// check max threads (global)
			final int globalThreads    = getGlobalThreadCount();
			final int globalMaxThreads = getGlobalMaxThreads();
			if (globalThreads >= globalMaxThreads) {
				if (this.coolMaxReached.runAgain() || DETAILED_LOGGING) {
					this.log().warning(
						(new StringBuilder())
							.append("Global max threads limit [ ")
							.append(globalMaxThreads)
							.append(" ] reached!")
							.toString()
					);
				}
				return null;
			}
			// increment and final check
			{
				final int count = this.threadCount.incrementAndGet();
				if (count > maxThreads) {
					this.threadCount.decrementAndGet();
					if (maxThreads > 1) {
						this.log().warning(
							(new StringBuilder())
								.append("Max threads limit [ ")
								.append(maxThreads)
								.append(" ] reached!")
								.toString()
						);
					}
				}
				return null;
			}
		}
		// start new thread
		{
			final xThreadPoolWorker worker =
				new xThreadPoolWorker(this);
			this.workers
				.add(worker);
			worker.start();
		}
		return Boolean.TRUE;
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
//		if (this.runNow.get() != null) {
//			this.active.incrementAndGet();
//			final xRunnable task = this.runNow.get();
//			if (task != null) {
//				final int runIndex = this.runCount.incrementAndGet();
//				currentThread.setName(
//						(new StringBuilder())
//						.append("run:").append(runIndex).append(" ")
//						.append("name:").append(threadName).append(" ")
//						.append("task:").append(task.getTaskName())
//						.toString()
//				);
//				try {
//					task.run();
//				} catch (Exception e) {
//					log.trace(e);
//				}
//				currentThread.setName(threadName);
//				this.runNow.set(null);
//			}
//			synchronized(this.runNowWaiter) {
//				this.runNowWaiter.notifyAll();
//			}
//			this.active.decrementAndGet();
//			inactive.resetRun();
//			continue;
//		}
	}



	// ------------------------------------------------------------------------------- //
	// tasks



	// run a task as soon as possible (generally less than 1ms)
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
/*
//TODO: if already in main thread, just run and return

		// queue to run next
//TODO:
		while (true) {
			if (this.runTaskNow.compareAndSet(null, task)) {
				break;
			}
//TODO:
//			try {
//				synchronized(this.runNowWaiter) {
//					this.runNowWaiter.wait(ThreadSleepTime.getMS());
//				}
//			} catch (InterruptedException e) {
//				this.log().trace(e);
//				return;
//			}
ThreadUtils.Sleep(1L);
		}

		// be sure there's a thread
//TODO:
//		this.newThread();

//TODO:
		// make sure there's a thread
		if (this.isMainPool()) {
			if (this.getActiveCount() < 1) {
				try {
					mainThread.interrupt();
				} catch (Exception ignore) {}
			}
		} else {
			this.newThread();
			if (this.getActiveCount() < 1) {
				try {
					final Iterator<Thread> it = this.threads.iterator();
					it.hasNext();
					it.next().interrupt();
				} catch (Exception ignore) {}
			}
		}
		// wait until task finishes
		try {
			synchronized(this.runNowWaiter) {
				this.runNowWaiter.wait(MaxTaskTime.getMS());
			}
		} catch (InterruptedException e) {
			this.log().trace(e);
		}

		// already in main thread, run task
		{
			final Thread currentThread = Thread.currentThread();
			final String threadName = currentThread.getName();
			final int runIndex = this.runCount.incrementAndGet();
			this.active.incrementAndGet();
			currentThread.setName(
				(new StringBuilder())
					.append(runIndex)
					.append(":")
					.append(threadName)
					.append(":")
					.append(task.getTaskName())
					.toString()
			);
			try {
				task.run();
			} catch (Exception e) {
				this.log().getWeak(threadName)
					.trace(e);
			}
			// restore thread name
			currentThread.setName(
				(new StringBuilder())
					.append(threadName)
					.append(":")
					.append(task.getTaskName())
					.toString()
			);
			this.active.decrementAndGet();
		}
*/
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
		newThread();
	}



	// ------------------------------------------------------------------------------- //
	// counts / stats



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



	/**
	 * Is task queue empty.
	 * @return true if task queue is empty.
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
	public void setMaxThreads(final int size) {
		this.poolSize =
			NumberUtils.MinMax(
				size,
				0,
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
