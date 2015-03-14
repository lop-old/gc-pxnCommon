package com.poixson.commonjava.Utils.threads;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.Utils.CoolDown;
import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsNumbers;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.Utils.xStartable;
import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.Utils.xTimeU;
import com.poixson.commonjava.xLogger.xLog;


public class xThreadPool implements xStartable {

	public static final boolean DETAILED_LOGGING = false;

	// max threads
	public static final int HARD_LIMIT = 20;
	public static final int GLOBAL_LIMIT = 50;
	private volatile int size = 1;
	private volatile AtomicInteger nextThreadId = new AtomicInteger(1);
	private static final xTime threadSleepTime = xTime.get("200n");
	private static final xTime threadInactiveTimeout = xTime.get("20s");

	// run later queue
	protected final String queueName;
	protected final ThreadGroup group;
	protected final xThreadFactory threadFactory;
	protected final Set<Thread> threads = new HashSet<Thread>();
	protected final BlockingQueue<xRunnable> queue = new ArrayBlockingQueue<xRunnable>(10, true);
	protected static volatile Thread mainThread = null;

	// run now queue
	protected final Object nowLock = new Object();
	protected volatile xRunnable runThisNow = null;
	protected volatile boolean nowHasRun = false;

	protected volatile int priority = Thread.NORM_PRIORITY;
	protected volatile boolean stopping = false;
	protected volatile int active = 0;
	protected volatile int runCount = 0;
	// cool-down
	protected final CoolDown coolMaxReached = CoolDown.get("5s");

	// pool instances
	protected static final Map<String, xThreadPool> instances = new ConcurrentHashMap<String, xThreadPool>();



	/**
	 * Get main thread queue
	 */
	public static xThreadPool get() {
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
		final String nameStr = utils.isEmpty(name) ? "main" : name;
		final String key = nameStr.toLowerCase();
		xThreadPool pool = instances.get(key);
		if(pool != null)
			return pool;
		synchronized(instances) {
			if(instances.containsKey(key))
				return instances.get(key);
			pool = new xThreadPool(nameStr, size);
			instances.put(key, pool);
			// new main pool
			if(pool.isMainPool()) {
				// just to prevent gc
				Keeper.add(pool);
				pool.runLater(
					new xRunnable("xThreadPool-Startup") {
						private volatile xThreadPool pool = null;
						public xRunnable init(final xThreadPool pool) {
							this.pool = pool;
							return this;
						}
						@Override
						public void run() {
							this.pool.logLocal().fine("Thread queue is running..");
						}
					}.init(pool)
				);
			}
		}
		return pool;
	}
	protected xThreadPool(final String name, final Integer size) {
		this.queueName = utils.isEmpty(name) ? "main" : name;
		this.group = new ThreadGroup(this.queueName);
		this.threadFactory = new xThreadFactory(this.queueName, this.group, true, Thread.NORM_PRIORITY);
		if("main".equalsIgnoreCase(this.queueName))
			this.size = 0;
		else
		if(size != null)
			this.size = size.intValue();
	}



	@Override
	public void Start() {
		this.newThread();
	}
	@Override
	public void Stop() {
		this.stopping = true;
	}
	@Override
	public boolean isRunning() {
		return !this.stopping;
	}



	/**
	 * Create a new thread if needed, skip if queue is empty.
	 */
	protected void newThread() {
		if(this.stopping) {
			if(DETAILED_LOGGING)
				this.logLocal().warning("thread pool is stopping; cannot start new thread as requested");
			return;
		}
		if(isMainPool())
			return;
		if(this.size <= 0)
			return;
		synchronized(this.threads) {
			final int count = this.threads.size();
			final int globalCount = getGlobalThreadCount();
			final int globalFree  = GLOBAL_LIMIT - globalCount;
			final int free = utilsNumbers.MinMax(count - this.active, 0, globalFree);
			if(DETAILED_LOGGING) {
				this.logLocal().finer(
					"Pool Size: "+
						Integer.toString(count)+
						" ["+Integer.toString(this.size)+"]  "+
					"Active/Free: "+
						Integer.toString(this.active)+"/"+
						Integer.toString(free)+"  "+
					"Global: "+
						Integer.toString(globalCount)+
						" ["+Integer.toString(GLOBAL_LIMIT)+"]"
				);
			}
			// use an existing waiting thread
			if(free > 0) return;
			// global max threads
			if(globalFree <= 0) {
				if(this.coolMaxReached.runAgain()) {
					if(DETAILED_LOGGING)
						this.logLocal().warning("Global max threads limit [ "+Integer.toString(globalCount)+" ] reached!");
				} else
					this.logLocal().warning("Global max threads limit [ "+Integer.toString(globalCount)+" ] reached!");
				utilsThread.Sleep(10L);
				return;
			}
			// max threads (this pool)
			if(count >= this.size) {
				if(this.size > 1) {
					if(this.coolMaxReached.runAgain())
						this.logLocal().warning("Max threads limit [ "+Integer.toString(count)+" ] reached!");
					else
						this.logLocal().finest("Max threads limit [ "+Integer.toString(count)+" ] reached!                     **************                     SHOULD THIS RUN HERE???");
				}
				utilsThread.Sleep(10L);
				return;
			}
			// start new thread
			{
				final Thread thread = this.threadFactory.newThread(this);
				//final Thread thread = new Thread(this.group, this);
				this.threads.add(thread);
				thread.start();
				thread.setPriority(this.priority);
			}
		}
	}
	// set thread priority
	public void setThreadPriority(final int priority) {
		this.priority = priority;
		synchronized(this.threads) {
			for(final Thread thread : this.threads)
				thread.setPriority(priority);
		}
	}



	@Override
	public void run() {
		if(DETAILED_LOGGING)
			this.logLocal().fine("Started pool thread..");
		final int threadId = getNextThreadId();
		final Thread currentThread = Thread.currentThread();
		currentThread.setName( this.queueName+":"+Integer.toString(threadId) );
		final xTime sleeping = xTime.get();
		if(isMainPool())
			xThreadPool.mainThread = currentThread;
		while(true) {
			if(this.stopping && !isMainPool()) {
				this.logLocal().finer("Stopping thread id: "+Integer.toString(threadId));
				break;
			}
			// run task now
			if(this.runThisNow != null) {
				synchronized(this.nowLock) {
					this.nowHasRun = false;
					final xRunnable tmpTask = this.runThisNow;
					this.runThisNow = null;
					try {
						if(DETAILED_LOGGING)
							this.logLocal().publish("running thread id: "+Integer.toString(threadId));
						tmpTask.run();
						if(DETAILED_LOGGING)
							this.logLocal().publish("finished thread id: "+Integer.toString(threadId));
					} catch (Exception e) {
						this.logLocal().trace(e);
					}
					this.nowHasRun = true;
					this.nowLock.notifyAll();
				}
				continue;
			}
			// pull from queue
			final xRunnable task;
			try {
				task = this.queue.poll(1, xTimeU.S);
			} catch (InterruptedException ignore) {
				continue;
			}
			if(!isMainPool()) {
				// sleeping thread
				if(task == null) {
					// stop inactive thread after 5 minutes
					if(sleeping.value >= threadInactiveTimeout.value) {
						this.logLocal().finer("Inactive thread.. id: "+Integer.toString(threadId)+")");
						break;
					}
					sleeping.add(threadSleepTime);
					continue;
				}
			}
			// active thread
			if(task != null) {
				this.runCount++;
				this.active++;
				sleeping.reset();
				// rename thread
				currentThread.setName( this.queueName+":"+Integer.toString(threadId)+":"+task.getTaskName() );
				// run the task
				try {
					task.run();
					// low priority can sleep
					if(this.priority <= (Thread.NORM_PRIORITY - Thread.MIN_PRIORITY) / 2)
						utilsThread.Sleep(10L); // sleep 10ms
				} catch (Exception e) {
					this.logLocal().trace(e);
				}
				// task finished
				this.active--;
				if(this.active < 0) this.active = 0;
				// reset thread name
				currentThread.setName( this.queueName+":"+Integer.toString(threadId) );
			}
		}
		this.logLocal().finer("Thread stopped id: "+Integer.toString(threadId));
		synchronized(this.threads) {
			this.threads.remove(currentThread);
		}
	}



	// run a task as soon as possible (generally 0.9ms)
	public void runNow(final Runnable run) {
		if(run == null) throw new NullPointerException("run cannot be null");
		synchronized(this.nowLock) {
			this.nowHasRun = false;
			this.runThisNow = xRunnable.cast(run);
			if(DETAILED_LOGGING)
				this.logLocal().finest("Task running.. id: "+this.runThisNow.getTaskName());
			// make sure there's a thread
			synchronized(this.threads) {
				if(this.threads.size() == 0)
					newThread();
				// awaken thread if needed
				if(getActiveCount() <= 0) {
					if(isMainPool()) {
						try {
							xThreadPool.mainThread.interrupt();
						} catch (Exception ignore) {}
					} else {
						try {
							final Iterator<Thread> it = this.threads.iterator();
							it.hasNext();
							it.next().interrupt();
						} catch (Exception ignore) {}
					}
				}
			}
			// wait for task to complete
			while(this.runThisNow != null || !this.nowHasRun) {
				try {
					this.nowLock.wait(2000L);
					break;
				} catch (InterruptedException ignore) {}
			}
			this.nowHasRun = false;
		}
	}
	// queue a task to be run soon (generally 1.5ms)
	public void runLater(final Runnable run) {
		if(run == null) throw new NullPointerException("run cannot be null");
		final xRunnable task = xRunnable.cast(run);
		// add to main thread queue
		if(this.size <= 0 && !isMainPool()) {
			get().runLater(run);
			return;
		}
		try {
			if(this.queue.offer(task, 5, xTimeU.S)) {
				if(DETAILED_LOGGING)
					this.logLocal().finest("Task queued.. "+task.getTaskName());
			} else
				this.logLocal().warning("Thread queue jammed! "+task.getTaskName());
		} catch (InterruptedException ignore) {
			return;
		}
		newThread();
	}



	protected int getNextThreadId() {
		return this.nextThreadId.getAndAdd(1);
	}



	public String getName() {
		return this.queueName;
	}
	public boolean isMainPool() {
		return "main".equalsIgnoreCase(this.queueName);
	}



	/**
	 * Stop all thread pools (except main)
	 */
	public static void ShutdownAll() {
		// be sure to run in main thread
		if(mainThread != null && !mainThread.equals(Thread.currentThread())) {
			get().runNow(new Runnable() {
				@Override
				public void run() {
					ShutdownAll();
				}
			});
			return;
		}
		synchronized(instances) {
			if(instances.isEmpty()) return;
			// shutdown threads
			for(final xThreadPool pool : instances.values()) {
				if(pool.isMainPool()) continue;
				pool.Stop();
			}
			// wait for threads to stop
			final Iterator<Entry<String, xThreadPool>> it = instances.entrySet().iterator();
			while(it.hasNext()) {
				final Entry<String, xThreadPool> entry = it.next();
				final xThreadPool pool = entry.getValue();
				if(pool.isMainPool()) continue;
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
	}
	public static void Exit() {
		final xRunnable runexit = new xRunnable("Exit") {
			@Override
			public void run() {
				ExitNow();
			}
		};
		if(mainThread != null && !mainThread.equals(Thread.currentThread()) && get().isRunning())
			get().runLater(runexit);
		else
			runexit.run();
	}
	public static void ExitNow() {
		// display threads still running
		displayStillRunning();
		System.out.println();
		System.out.println();
		System.exit(0);
	}



	// display threads still running
	protected static void displayStillRunning() {
		if(!xVars.debug()) return;
		final String[] threadNames = utilsThread.getThreadNames(false);
		// no threads still running
		if(utils.isEmpty(threadNames)) return;
		// build message
		final StringBuilder msg = new StringBuilder();
		msg.append("Threads still running:  ").append(threadNames.length);
		for(final String name : threadNames)
			msg.append("\n  ").append(name);
		xLog.getRoot().publish(msg.toString());
	}



	/**
	 * Get the current thread count.
	 * @return number of threads in the pool.
	 */
	public int getThreadCount() {
		return this.threads.size();
	}
	/**
	 * Get the active thread count.
	 * @return number of active threads in the pool.
	 */
	public int getActiveCount() {
		return this.active;
	}
	/**
	 * Returns true if pool is not currently in use and queue is empty.
	 * @return true if inactive and empty.
	 */
	public boolean isEmpty() {
		return (this.queue.size() == 0 && this.active == 0);
	}
	/**
	 * Get the thread count for all thread pools.
	 * @return number of active threads in the application.
	 */
	public static int getGlobalThreadCount() {
		if(instances.size() == 0)
			return 0;
		int count = 0;
		synchronized(instances) {
			for(final xThreadPool pool : instances.values())
				count += pool.getThreadCount();
		}
		return count;
	}
	/**
	 * Get the maximum number of threads for this pool.
	 * @return max number of threads allowed.
	 */
	public int getMaxThreads() {
		return this.size;
	}
	/**
	 * Set the maximum number of threads for this pool.
	 * @param size max number of threads allowed.
	 */
	public void setMaxThreads(final Integer value) {
		if(value == null)
			this.size = 1;
		else
			this.size = utilsNumbers.MinMax(value.intValue(), 1, HARD_LIMIT);
	}



	// logger
	private volatile xLog _log = null;
	public xLog logLocal() {
		return this.log().get(this.queueName);
	}
	public xLog log() {
		if(this._log == null)
			return utils.log();
		return this._log;
	}
	public void setLog(final xLog log) {
		this._log = log;
	}



}
