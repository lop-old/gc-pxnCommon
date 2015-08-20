package com.poixson.commonjava.Utils.threads;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.poixson.commonjava.Utils.CoolDown;
import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsNumbers;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.Utils.xStartable;
import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.Utils.xTimeU;
import com.poixson.commonjava.remapped.RemappedRunnable;
import com.poixson.commonjava.xLogger.xLevel;
import com.poixson.commonjava.xLogger.xLog;


public class xThreadPool implements xStartable {

	public static final boolean DETAILED_LOGGING = false;

	// max threads
	public static final int POOL_LIMIT   = 20;
	public static final int GLOBAL_LIMIT = 50;

	// pool timing
	private static final xTime threadSleepTime       = xTime.get("200n");
	private static final xTime threadInactiveTimeout = xTime.get("10s");
	// warning cool-down
	private final CoolDown coolMaxReached = CoolDown.get("5s");

	// pool instances
	protected static final ConcurrentMap<String, xThreadPool> instances =
			new ConcurrentHashMap<String, xThreadPool>();

	// main thread pool
	protected static final xThreadPool mainPool = getMainPool();
	protected static volatile Thread mainThread = null;
	public static final String MAIN_POOL_NAME = "main";
	protected final boolean isMainPool;

	// named thread pool
	protected volatile int poolSize;
	protected final String poolName;
	protected final ThreadGroup group;
	protected final xThreadFactory threadFactory;
	protected final AtomicInteger threadCount = new AtomicInteger(0);
	protected final Set<Thread> threads = new CopyOnWriteArraySet<Thread>();
	protected final BlockingQueue<xRunnable> queue = new ArrayBlockingQueue<xRunnable>(10, true);

	// run now
	protected final Object runNowWaiter    = new Object();
	protected final AtomicReference<xRunnable> runNow =
			new AtomicReference<xRunnable>();

	protected volatile int priority = Thread.NORM_PRIORITY;
	protected volatile boolean stopping = false;
	protected final    AtomicInteger active   = new AtomicInteger(0);
	protected volatile AtomicInteger runCount = new AtomicInteger(0);



	/**
	 * Get main thread queue
	 */
	public static xThreadPool getMainPool() {
		return get(null, null);
	}
	/**
	 * Get thread queue by name
	 */
	public static xThreadPool get(final String name) {
		return get(name, null);
	}
	/**
	 * Get thread queue by name or create with x threads
	 * @param name Thread queue name to get or create.
	 * @param size Number of threads which can be created for this queue.
	 */
	public static xThreadPool get(final String name, final Integer size) {
		final String poolName;
		final int    poolSize;
		if(utils.isEmpty(name) || MAIN_POOL_NAME.equalsIgnoreCase(name)) {
			if(mainPool != null)
				return mainPool;
			poolName = MAIN_POOL_NAME;
			poolSize = 0;
		} else {
			if(size < 1) throw new IllegalArgumentException("Invalid pool size: "+Integer.toString(size));
			poolName = name;
			poolSize = size;
		}
		// existing instance
		{
			final xThreadPool pool = instances.get(poolName);
			if(pool != null)
				return pool;
		}
		final xThreadPool pool;
		synchronized(instances) {
			// existing instance
			if(instances.containsKey(poolName))
				return instances.get(poolName);
			// new instance
			pool = new xThreadPool(poolName, poolSize);
			instances.put(poolName, pool);
		}
		return pool;
	}
	protected xThreadPool(final String name, final int size) {
		if(utils.isEmpty(name)) throw new NullPointerException("name argument is required!");
		if(MAIN_POOL_NAME.equals(name)) {
			if(size != 0) throw new IllegalArgumentException();
			this.isMainPool = true;
			// just to prevent gc
			Keeper.add(this);
		} else {
			if(size < 1)  throw new IllegalArgumentException();
			this.isMainPool = false;
		}
		this.poolName = name;
		this.poolSize = (size > POOL_LIMIT ? POOL_LIMIT : size);
		this.group = new ThreadGroup(name);
		this.threadFactory = new xThreadFactory(
				this.poolName,
				this.group,
				false, // daemon
				this.priority
		);
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
	}



	@Override
	public void Start() {
		if(this.isMainPool()) {
			if(mainThread == null) {
				new Thread() {
					@Override
					public void run() {
						getMainPool().run();
					}
				}.start();
				this.log().fine("Started main thread internally..");
				utilsThread.Sleep(10L);
			}
			return;
		}
		this.newThread();
	}
	@Override
	public void Stop() {
		this.stopping = true;
	}
	@Override
	public boolean isRunning() {
		if(!this.stopping)
			return true;
		if(!this.threads.isEmpty())
			return true;
		return false;
	}



	public String getName() {
		return this.poolName;
	}
	public static String[] getPoolNames() {
		return instances.keySet().toArray(new String[0]);
	}
	public boolean isMainPool() {
		return this.isMainPool;
	}



	/**
	 * Create a new thread if needed, skip if queue is empty.
	 */
	protected void newThread() {
		if(this.isMainPool() || this.poolSize < 1)
			return;
		if(this.stopping) {
			if(DETAILED_LOGGING)
				this.log().warning("thread pool is stopping; cannot start new thread as requested");
			return;
		}
		// thread stats
		{
			final xThreadPoolStats stats = new xThreadPoolStats(this);
			if(DETAILED_LOGGING)
				this.displayStats();
			// use existing thread
			if(stats.inactiveThreads > 0)
				return;
			// check max threads (global)
			if(stats.globalThreadsFree < 1) {
				if(this.coolMaxReached.runAgain() || DETAILED_LOGGING)
					this.log().warning("Global max threads limit [ "+Integer.toString(GLOBAL_LIMIT)+" ] reached!");
				utilsThread.Sleep(1L);
				return;
			}
			// check max threads (this pool)
			if(stats.currentThreads >= stats.maxThreads) {
				if(this.coolMaxReached.runAgain() || DETAILED_LOGGING)
					this.log().warning("Max threads limit [ "+Integer.toString(stats.maxThreads)+" ] reached!");
				utilsThread.Sleep(1L);
				return;
			}
		}
		synchronized(this.threadCount) {
			// check max threads
			if(this.getThreadCount() >= this.getMaxThreads()) {
				if(this.coolMaxReached.runAgain() || DETAILED_LOGGING)
					this.log().warning("Max threads limit [ "+Integer.toString(this.getMaxThreads())+" ] reached!");
				utilsThread.Sleep(1L);
				return;
			}
			this.threadCount.incrementAndGet();
		}
		// start new thread
		{
			final Thread thread = this.threadFactory.newThread(this);
			this.threads.add(thread);
			thread.start();
		}
	}



	// set thread priority
	public void setThreadPriority(final int priority) {
		if(priority > this.group.getMaxPriority())
			this.group.setMaxPriority(priority);
		this.threadFactory.setPriority(priority);
		this.priority = priority;
		final Iterator<Thread> it = this.threads.iterator();
		while(it.hasNext())
			it.next().setPriority(priority);
		if(priority < this.group.getMaxPriority())
			this.group.setMaxPriority(priority);
	}



	@Override
	public void run() {
		final Thread currentThread = Thread.currentThread();
		final String threadName = currentThread.getName();
		final xLog log = this.log().getWeak(threadName);
		log.finer("Started thread..");
		// main thread
		if(this.isMainPool()) {
			if(mainThread != null) throw new IllegalStateException("Main thread already running!");
			mainThread = currentThread;
			mainThread.setName("Main");
		}
		// thread loop
		final CoolDown inactive = CoolDown.get(threadInactiveTimeout);
		inactive.resetRun();
		while(true) {
			// stopping thread pool
			if(this.stopping && !this.isMainPool()) {
				log.finer("Stopping pool thread..");
				break;
			}
			{
				final int active = this.active.get();
				if(active < 0) {
					while(this.active.get() < 0)
						this.active.incrementAndGet();
					log.trace(new IllegalStateException("Invalid active count: "+Integer.toString(active)));
				}
			}
			// run task now
			if(this.runNow.get() != null) {
				this.active.incrementAndGet();
				final xRunnable task = this.runNow.get();
				if(task != null) {
					final int runIndex = this.runCount.incrementAndGet();
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
					this.runNow.set(null);
				}
				try {
					this.runNowWaiter.notifyAll();
				} catch (Exception ignore) {}
				this.active.decrementAndGet();
				inactive.resetRun();
				continue;
			}
			// pull from queue
			final xRunnable task;
			try {
				task = this.queue.poll(threadSleepTime.getMS(), xTimeU.MS);
			} catch (InterruptedException ignore) {
				continue;
			}
			// run task
			if(task != null) {
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
			// inactive thread
			if(inactive.runAgain() && !this.isMainPool()) {
				log.finer("Stopping inactive thread..");
				break;
			}
			if(DETAILED_LOGGING)
				log.warning("Thread idle..");
		}
		// main thread (this shouldn't happen)
		if(this.isMainPool())
			mainThread = null;
		// thread stopping
		this.threadCount.decrementAndGet();
		this.threads.remove(currentThread);
	}



	// run a task as soon as possible (generally 0.9ms)
	public void runNow(final Runnable run) {
		if(run == null) throw new NullPointerException("run argument is required!");
		final xRunnable task = xRunnable.cast(run);
		// already in main thread, run it now
		if(this.isMainPool()) {
			final Thread currentThread = Thread.currentThread();
			if(currentThread.equals(mainThread)) {
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
				currentThread.setName(threadName+":"+task.getTaskName());
				this.active.decrementAndGet();
				return;
			}
		} else
		// pass to main pool
		if(this.getMaxThreads() < 1) {
			getMainPool().runNow(task);
			return;
		}
		// queue to run next
		while(true) {
			if(this.runNow.get() == null) {
				if(this.runNow.compareAndSet(null, task))
					break;
			}
			try {
				this.runNowWaiter.wait(threadSleepTime.getMS());
			} catch (InterruptedException e) {
				this.log().trace(e);
				return;
			}
		}
		// make sure there's a thread
		if(this.isMainPool()) {
			if(this.getActiveCount() < 1) {
				try {
					mainThread.interrupt();
				} catch (Exception ignore) {}
			}
		} else {
			this.newThread();
			if(this.getActiveCount() < 1) {
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
				this.runNowWaiter.wait();
			}
		} catch (InterruptedException e) {
			this.log().trace(e);
		}
	}
	// queue a task
	public void runLater(final Runnable run) {
		if(run == null) throw new NullPointerException("run argument is required!");
		final xRunnable task = xRunnable.cast(run);
		// pass to main pool
		if(this.getMaxThreads() < 1 && !this.isMainPool) {
			getMainPool().runLater(task);
			return;
		}
		try {
			if(this.queue.offer(task, 5, xTimeU.S)) {
				if(DETAILED_LOGGING)
					this.log().finest("Task queued: "+task.getTaskName());
			} else
				this.log().warning("Thread queue jammed! "+task.getTaskName());
		} catch (InterruptedException e) {
			this.log().trace(e);
			return;
		}
		newThread();
	}



	/**
	 * Stop all thread pools (except main)
	 */
	public static void ShutdownAll() {
		// be sure to run in main thread
		if(!Thread.currentThread().equals(mainThread)) {
			getMainPool().runNow(new Runnable() {
				@Override
				public void run() {
					ShutdownAll();
				}
			});
			return;
		}
		synchronized(instances) {
			if(instances.isEmpty()) return;
			// stop threads
			for(final xThreadPool pool : instances.values()) {
				//if(pool.isMainPool())
				//	continue;
				pool.Stop();
			}
		}
		// wait for threads to stop
		final Iterator<xThreadPool> it = instances.values().iterator();
		while(it.hasNext()) {
			final xThreadPool pool = it.next();
			if(pool.isMainPool())
				continue;
			try {
				synchronized(pool) {
					pool.wait();
				}
			} catch (InterruptedException e) {
				xLog.getRoot().trace(e);
			}
			it.remove();
		}
	}
	public static void Exit() {
		try {
			getMainPool().runLater(
					new RemappedRunnable(
							xThreadPool.class,
							"ExitNow"
					)
			);
		} catch (Exception e) {
			xLog.getRoot().trace(e);
			ExitNow(1);
		}
	}
	public static void ExitNow() {
		ExitNow(0);
	}
	public static void ExitNow(final int status) {
		// display threads still running
		utilsThread.displayStillRunning();
		System.out.println();
		System.out.println();
		System.exit(status);
	}



	/**
	 * Get the current thread count, excluding main pool.
	 * @return number of threads in the pool.
	 */
	public int getThreadCount() {
		if(this.isMainPool())
			return 0;
		return this.threadCount.get();
	}
	/**
	 * Get the thread count for all thread pools, excluding main pool.
	 * @return number of active threads in the application.
	 */
	public static int getGlobalThreadCount() {
		if(instances.isEmpty())
			return 0;
		int count = 0;
		final Iterator<xThreadPool> it = instances.values().iterator();
		while(it.hasNext()) {
			count += it.next()
				.getThreadCount();
		}
		return count;
	}



	/**
	 * Get the maximum number of threads for this pool.
	 * @return max number of threads allowed.
	 */
	public int getMaxThreads() {
		return this.poolSize;
	}
	/**
	 * Set the maximum number of threads for this pool.
	 * @param size max number of threads allowed.
	 */
	public void setMaxThreads(final int size) {
		if(size < 1) throw new IllegalArgumentException("Invalid pool size: "+Integer.toString(size));
		if(size > POOL_LIMIT)
			this.poolSize = POOL_LIMIT;
		else
			this.poolSize = size;
	}



	/**
	 * Get the active thread count.
	 * @return number of active threads in the pool.
	 */
	public int getActiveCount() {
		return this.active.get();
	}
	/**
	 * Returns true if pool is not currently in use and queue is empty.
	 * @return true if inactive and empty.
	 */
	public boolean isEmpty() {
		return (this.queue.size() == 0 && this.active.get() == 0);
	}



	public xThreadPoolStats getStatsDAO() {
		return new xThreadPoolStats(this);
	}
	public void displayStats() {
		new xThreadPoolStats(this)
			.displayStats(xLevel.FINE);
	}



	public static class xThreadPoolStats {

		private final xThreadPool pool;

		public final int currentThreads;
		public final int maxThreads;
		public final int activeThreads;
		public final int inactiveThreads;
		public final int queueSize;
		public final int globalThreads;
		public final int globalThreadsFree;

		public xThreadPoolStats(final xThreadPool pool) {
			this.pool = pool;
			this.currentThreads    = this.pool.getThreadCount();
			this.maxThreads        = this.pool.getMaxThreads();
			this.queueSize         = this.pool.queue.size();
			this.globalThreads     = xThreadPool.getGlobalThreadCount();
			this.globalThreadsFree = xThreadPool.GLOBAL_LIMIT - this.globalThreads;
			this.activeThreads     = this.pool.getActiveCount();
			this.inactiveThreads   = utilsNumbers.MinMax(this.currentThreads - this.activeThreads, 0, this.maxThreads);
		}

		public void displayStats(final xLevel level) {
			this.pool.log().publish(level,
					"Queued: ["+  Integer.toString(this.queueSize)+"]  "+
					"Pool size: "+Integer.toString(this.currentThreads)+
						" ["+     Integer.toString(this.maxThreads)+"]  "+
					"Active/Free: "+
						Integer.toString(this.activeThreads)+"/"+
						Integer.toString(this.inactiveThreads)+"  "+
					"Global: "+Integer.toString(this.globalThreads)+
						" ["+  Integer.toString(xThreadPool.GLOBAL_LIMIT)+"]"
			);
		}

	}



	// logger
	private volatile xLog _log = null;
	public xLog log() {
		if(this._log == null)
			this._log = xLog.getRoot()
				.get("xThreadPool|"+this.poolName);
		return this._log;
	}



}
