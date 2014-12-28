package com.poixson.commonjava.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.xLogger.xLog;


public class xThreadPool implements xStartable {

	// max threads
	public static final int HARD_LIMIT = 20;
	public static final int GLOBAL_LIMIT = 50;
	private volatile int size = 1;
	private volatile AtomicInteger nextThreadId = new AtomicInteger(1);
	private final Object nextLock = new Object();
	private static final xTime threadSleepTime = xTime.get("200n");
	private static final xTime threadInactiveTimeout = xTime.get("30s");

	// run later queue
	protected final String queueName;
	protected final ThreadGroup group;
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
	protected static final Map<String, xThreadPool> instances = new HashMap<String, xThreadPool>();



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
		synchronized(instances) {
			if(instances.containsKey(key))
				return instances.get(key);
			final xThreadPool pool = new xThreadPool(nameStr, size);
			instances.put(key, pool);
			return pool;
		}
	}
	protected xThreadPool(final String name, final Integer size) {
		this.queueName = name;
		this.group = new ThreadGroup(this.queueName);
		if(name.toLowerCase().equals("main")) {
			this.size = 0;
			// just to prevent gc
			Keeper.add(this);
		} else
		if(size != null)
			this.size = size.intValue();
		this.runLater(new xRunnable("Thread-Startup") {
			private volatile String qName = null;
			public xRunnable init(final String nameStr) {
				this.qName = nameStr;
				return this;
			}
			@Override
			public void run() {
				utils.log().fine("Started thread queue ("+this.qName+")");
			}
		}.init(this.queueName));
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
		if(this.stopping || this.size <= 0 || isMainPool()) return;
		synchronized(this.threads) {
			final int count = this.threads.size();
			final int globalCount = getGlobalThreadCount();
			final int globalFree  = GLOBAL_LIMIT - globalCount;
			final int free = utilsNumbers.MinMax(count - this.active, 0, globalFree);
			this.log().finer(
				"Pool Size: "+Integer.toString(count)+" ["+Integer.toString(this.size)+"]  "+
				"Active/Free: "+
					Integer.toString(this.active)+"/"+
					Integer.toString(free)+"  "+
				"Global: "+Integer.toString(globalCount)+" ["+Integer.toString(GLOBAL_LIMIT)+"]"
			);
			// use an existing waiting thread
			if(free > 0) return;
			// global max threads
			if(globalFree <= 0) {
				if(this.coolMaxReached.runAgain())
					this.log().warning("Global max threads limit [ "+Integer.toString(globalCount)+" ] reached!");
				else
					this.log().warning("Global max threads limit [ "+Integer.toString(globalCount)+" ] reached!");
				utilsThread.Sleep(10L);
				return;
			}
			// max threads (this pool)
			if(count >= this.size) {
				if(this.size > 1) {
					if(this.coolMaxReached.runAgain())
						this.log().warning("Max threads limit [ "+Integer.toString(count)+" ] reached!");
					else
						this.log().warning("Max threads limit [ "+Integer.toString(count)+" ] reached!");
				}
				utilsThread.Sleep(10L);
				return;
			}
			// start new thread
			synchronized(this.threads) {
				final Thread thread = new Thread(this.group, this);
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
		final int threadId = getNextThreadId();
		Thread.currentThread().setName(this.queueName);
		final xTime sleeping = xTime.get();
		if(isMainPool())
			xThreadPool.mainThread = Thread.currentThread();
		while(true) {
			if(this.stopping && !isMainPool()) {
				this.log().finer("Stopping thread ("+this.queueName+":"+Integer.toString(threadId)+")");
				break;
			}
			// run task now
			if(this.runThisNow != null) {
				synchronized(this.nowLock) {
					this.nowHasRun = false;
					final xRunnable tmpTask = this.runThisNow;
					this.runThisNow = null;
					try {
						tmpTask.run();
					} catch (Exception e) {
						this.log().trace(e);
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
						this.log().finer("Inactive thread.. ("+this.queueName+":"+Integer.toString(threadId)+")");
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
				Thread.currentThread().setName(this.queueName+":"+Integer.toString(threadId)+"["+task.getTaskName()+"]");
				// run the task
				try {
					task.run();
					// low priority can sleep
					if(this.priority <= (Thread.NORM_PRIORITY - Thread.MIN_PRIORITY) / 2)
						utilsThread.Sleep(10L); // sleep 10ms
				} catch (Exception e) {
					this.log().trace(e);
				}
				// task finished
				this.active--;
				if(this.active < 0) this.active = 0;
				Thread.currentThread().setName(this.queueName);
			}
		}
		this.log().finer("Thread stopped ("+this.queueName+":"+Integer.toString(threadId)+")");
		synchronized(this.threads) {
			this.threads.remove(Thread.currentThread());
		}
	}



	// run a task as soon as possible (generally 0.9ms)
	public void runNow(final Runnable run) {
		if(run == null) throw new NullPointerException("run cannot be null");
		synchronized(this.nowLock) {
			this.nowHasRun = false;
			this.runThisNow = xRunnable.cast(run);
			this.log().finest("Task running.. ("+this.queueName+") "+this.runThisNow.getTaskName());
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
			if(this.queue.offer(task, 5, xTimeU.S))
				this.log().finest("Task queued.. ("+this.queueName+") "+task.getTaskName());
			else
				this.log().warning("Thread queue jammed! ("+this.queueName+") "+task.getTaskName());
		} catch (InterruptedException ignore) {
			return;
		}
		newThread();
	}



	protected int getNextThreadId() {
		synchronized(this.nextLock) {
			final int next = this.nextThreadId.getAndAdd(1);
			return next;
		}
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
				// display threads still running
				displayStillRunning();
				System.out.println();
				System.out.println();
				System.exit(0);
			}
		};
		if(mainThread != null && !mainThread.equals(Thread.currentThread()) && get().isRunning())
			get().runLater(runexit);
		else
			runexit.run();
	}



	// display threads still running
	protected static void displayStillRunning() {
		if(!xVars.get().debug()) return;
		final String[] names = utilsThread.getThreadNames();
		if(utils.isEmpty(names)) return;
		final StringBuilder msg = new StringBuilder();
		msg.append("Threads still running:  ").append(names.length);
		for(final String name : names) {
			if("NonBlockingInputStreamThread".equals(name)) continue;
			if("Finalizer".equals(name)) continue;
			if(name.startsWith("main:")) continue;
			msg.append("\n  ").append(name);
		}
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
	 * Get the thread count for all thread pools.
	 * @return number of active threads in the application.
	 */
	public static int getGlobalThreadCount() {
		if(instances.size() == 0) return 0;
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
	public int maxThreads() {
		return this.size;
	}
	/**
	 * Set the maximum number of threads for this pool.
	 * @param size max number of threads allowed.
	 */
	public void maxThreads(final Integer value) {
		if(value == null)
			this.size = 1;
		else
			this.size = utilsNumbers.MinMax(value.intValue(), 1, HARD_LIMIT);
	}



	// logger
	private volatile xLog _log = null;
	public xLog log() {
		if(this._log == null)
			return utils.log();
		return this._log;
	}
	public void setLog(final xLog log) {
		this._log = log;
	}



}
