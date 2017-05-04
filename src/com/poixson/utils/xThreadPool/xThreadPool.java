package com.poixson.utils.xThreadPool;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.poixson.utils.CoolDown;
import com.poixson.utils.Keeper;
import com.poixson.utils.NumberUtils;
import com.poixson.utils.ReflectUtils;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.xRunnable;
import com.poixson.utils.xStartable;
import com.poixson.utils.xThreadFactory;
import com.poixson.utils.xTime;
import com.poixson.utils.xTimeU;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLog;


public class xThreadPool implements xStartable {
	private static final String LOG_NAME = "xThreadPool";
	public static final boolean DETAILED_LOGGING = false;

	// max threads
	public static final int  POOL_LIMIT         = 20;
	public static final int  GLOBAL_LIMIT       = 50;
	public static final long THREAD_LIMIT_SLEEP = 10L;
	public static final long POSTSTART_SLEEP    = 50L;
	public static final int  QUEUE_SIZE         = 10;

	// main thread pool
	public static final String MAIN_POOL_NAME = "main";
	protected final boolean isMainPool;

	// named thread pool
	protected final String poolName;
	protected volatile int poolSize;

	// run now
	protected final AtomicReference<xRunnable> runTaskNow =
			new AtomicReference<xRunnable>();
//TODO:
//	protected final Object runNowWaiter = new Object();

	// pool state
	protected final xThreadPoolStats stats;
	protected final AtomicBoolean running  = new AtomicBoolean(false);
	protected volatile boolean    stopping = false;
	protected final AtomicInteger threadCount = new AtomicInteger(0);
	protected final AtomicInteger active      = new AtomicInteger(0);
	protected final AtomicInteger runCount    = new AtomicInteger(0);

	protected final Set<Thread> threads = new CopyOnWriteArraySet<Thread>();
	protected final ThreadGroup group;
	protected volatile int priority = Thread.NORM_PRIORITY;
	protected xThreadFactory threadFactory;

	// pool timing
	protected final xTime timeThreadSleep       = xTime.get("200n");
	protected final xTime timeoutInactiveThread = xTime.get("10s");
//TODO:
//	protected final xTime maxTaskTime           = xTime.get("5s");
	// warning cool-down
	protected CoolDown coolMaxReached = CoolDown.get("5s");

	protected final BlockingQueue<xRunnable> queue =
			new ArrayBlockingQueue<xRunnable>(QUEUE_SIZE, true);



	protected xThreadPool(final String poolName, final int poolSize) {
		if (Utils.isEmpty(poolName)) throw new RequiredArgumentException("poolName");
		if (poolSize < 1)            throw new IllegalArgumentException("Invalid pool size: "+Integer.toString(poolSize));
		this.poolSize =
			NumberUtils.MinMax(
				poolSize,
				1,
				POOL_LIMIT
			);
		this.isMainPool = MAIN_POOL_NAME.equals(poolName);
		if (this.isMainPool) {
			// just to prevent gc
			Keeper.add(this);
		}
		this.stats = new xThreadPoolStats(this);
		this.poolName = poolName;
		this.group = new ThreadGroup(poolName);
		this.threadFactory = new xThreadFactory(
			this.poolName, // thread name
			this.group,    // thread group
			false,         // daemon
			this.priority  // thread priority
		);
	}



	//TODO:
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
		final boolean firstThread = (this.threadCount.get() == 0);
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
		// sleep if initial thread created
		if (firstThread) {
			ThreadUtils.Sleep(POSTSTART_SLEEP);
		}
	}
	@Override
	public void Stop() {
		this.stopping = true;
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

			final xThreadPoolStats stats = this.getStats();
			final int currentThreads  = stats.getCurrentThreadCount();
			final int inactiveThreads = stats.getInactiveThreadCount();
			final int maxThreads      = stats.getMaxThreads();
			// use existing inactive thread
			if (inactiveThreads > 0) {
				return Boolean.FALSE;
			}
			// check max threads (this pool)
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
				ThreadUtils.Sleep(THREAD_LIMIT_SLEEP);
				return null;
			}
			// check max threads (global)
			final int globalThreads    = xThreadPoolStats.getGlobalThreadCount();
			final int globalMaxThreads = xThreadPoolStats.getGlobalMaxThreads();
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
				ThreadUtils.Sleep(THREAD_LIMIT_SLEEP);
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
					ThreadUtils.Sleep(THREAD_LIMIT_SLEEP);
					return null;
				}
			}
		}
		// start new thread
		{
			final Thread thread = this.threadFactory.newThread(this);
			this.threads.add(thread);
			thread.start();
		}
		return Boolean.TRUE;
	}



	// set thread priority
	public void setThreadPriority(final int priority) {
		if (priority > this.group.getMaxPriority()) {
			this.group.setMaxPriority(priority);
		}
		this.threadFactory.setPriority(priority);
		this.priority = priority;
		final Iterator<Thread> it = this.threads.iterator();
		while (it.hasNext()) {
			it.next()
				.setPriority(priority);
		}
		if (priority < this.group.getMaxPriority()) {
			this.group.setMaxPriority(priority);
		}
	}



	// run loop
	@Override
	public void run() {
		final Thread currentThread = Thread.currentThread();
		final String threadName = currentThread.getName();
		final xLog log = this.log().getWeak(threadName);
		log.finer("Started thread..");
		// inactive thread timer
		final CoolDown inactive = CoolDown.get(this.timeoutInactiveThread);
		inactive.resetRun();
		// run loop
		while (true) {
			{
				if (this.active.get() < 0) {
					while (this.active.get() < 0) {
						this.active.incrementAndGet();
					}
					log.trace(
						new IllegalStateException(
							(new StringBuilder())
								.append("Invalid active count: ")
								.append(active)
								.toString()
						)
					);
				}
			}
			// run task now
			{
				final boolean result = this.doRunTaskNow();
				if (result) {
					continue;
				}
			}
			// pull from queue
			final xRunnable task;
			try {
				task = this.queue.poll(
						this.timeThreadSleep.getMS(),
						xTimeU.MS
				);
			} catch (InterruptedException ignore) {
				ThreadUtils.Sleep(250L);
				continue;
			}
			// run queued task
			if (task != null) {
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
					log.trace(e);
				}
				currentThread.setName(threadName);
				this.active.decrementAndGet();
				inactive.resetRun();
				continue;
			}
			// inactive thread or stopping
			if (this.isStopping() || inactive.runAgain()) {
				// main pool
				if (this.isMainPool()) {
					// always leave one main pool thread
					if (this.threadCount.get() > 1) {
						if (this.threadCount.decrementAndGet() > 0) {
							log.finer(
								this.isStopping()
								? "Stopping pool thread.."
								: "Stopping inactive main thread.."
							);
							break;
						}
						this.threadCount.incrementAndGet();
					}
				// other (not main pool)
				} else {
					this.threadCount.decrementAndGet();
					log.finer(
						this.isStopping()
						? "Stopping pool thread.."
						: "Stopping inactive thread.."
					);
					break;
				}
			}
			if (DETAILED_LOGGING) {
				log.warning("Thread idle..");
			}
		}
		// remove stopped thread
		this.threads.remove(currentThread);
	}
	// run task now
	protected boolean doRunTaskNow() {
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
return false;
	}



	// run a task as soon as possible (generally less than 1ms)
	public void runNow(final Runnable run) {
		if (run == null) throw new RequiredArgumentException("run");
		final xRunnable task = xRunnable.cast(run);
		final xThreadPoolStats stats = this.getStats();
		if (!this.isMainPool()) {
			// pass to main thread pool
			if (stats.getMaxThreads() <= 0) {
				xThreadPoolFactory.getMainPool()
					.runNow(task);
				return;
			}
		}
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
		final xRunnable task = xRunnable.cast(run);
//TODO:
//		// pass to main pool
//		if (this.getMaxThreads() < 1 && !this.isMainPool) {
//			getMainPool().runLater(task);
//			return;
//		}
		try {
			if (this.queue.offer(task, 5, xTimeU.S)) {
				if (DETAILED_LOGGING) {
					this.log().finest("Task queued: "+task.getTaskName());
				}
			} else
				this.log().warning("Thread queue jammed! "+task.getTaskName());
		} catch (InterruptedException e) {
			this.log()
				.trace(e);
			return;
		}
		newThread();
	}



	/**
	 * Set the maximum number of threads for this pool.
	 * @param size max number of threads allowed.
	 */
//TODO:
//	public void setMaxThreads(final int size) {
//		if (size < 1) throw new IllegalArgumentException("Invalid pool size: "+Integer.toString(size));
//		if (size > POOL_LIMIT)
//			this.poolSize = POOL_LIMIT;
//		else
//			this.poolSize = size;
//	}



	/**
	 * Returns true if pool is not currently in use and queue is empty.
	 * @return true if inactive and empty.
	 */
	public boolean isEmpty() {
		return (
			this.queue.size() == 0 &&
			this.active.get() == 0
		);
	}



	public xThreadPoolStats getStats() {
		return this.stats;
	}
//TODO:
//	public void displayStats() {
//	new xThreadPoolStats(this)
//		.displayStats(xLevel.FINE);
//}



	// logger
	private volatile SoftReference<xLog> _log = null;
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null) {
				return log;
			}
		}
		final xLog log = xLog.getRoot()
			.get(
				(new StringBuilder())
					.append(LOG_NAME)
					.append("|")
					.append(this.poolName)
					.toString()
			);
		this._log = new SoftReference<xLog>(log);
		return log;
	}



}
