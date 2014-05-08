package com.poixson.commonjava.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.poixson.commonjava.xLogger.xLog;


public class xThreadPool implements Runnable {

	// max threads
	public static final int HARD_LIMIT = 20;
	public static final int GLOBAL_LIMIT = 50;
	private volatile int size = 1;
	private volatile int nextThreadId = 1;
	private final Object nextLock = new Object();
	private static final xTime threadSleepTime = xTime.get("200n");
	private static final xTime threadInactiveTimeout = xTime.get("30s");

	protected final String queueName;
	protected final ThreadGroup group;
	protected final Set<Thread> threads = new HashSet<Thread>();
	protected final BlockingQueue<xRunnable> queue = new ArrayBlockingQueue<xRunnable>(10);

	protected volatile int priority = Thread.NORM_PRIORITY;
	protected volatile boolean stopping = false;
	protected volatile int active = 0;
	protected volatile int runCount = 0;
	// cool-down
	protected final CoolDown coolMaxReached = CoolDown.get("5s");

	// pool instances
	protected static final Map<String, xThreadPool> instances = new HashMap<String, xThreadPool>();


	public static xThreadPool get() {
		return get(null, null);
	}
	public static xThreadPool get(final String name) {
		return get(name, null);
	}
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
		if(name.toLowerCase().equals("main"))
			this.size = 1;
		else if(size != null)
			this.size = size.intValue();
		runLater(new xRunnable("Thread-Startup") {
			private volatile String qName = null;
			public xRunnable init(final String nameStr) {
				this.qName = nameStr;
				return this;
			}
			@Override
			public void run() {
				log().fine("Started thread queue ("+this.qName+")");
			}
		}.init(this.queueName));
	}


	public void start() {
		newThread();
	}
	public void stop() {
		this.stopping = true;
	}


	/**
	 * Create a new thread if needed, skip if queue is empty.
	 */
	protected void newThread() {
		if(this.stopping || this.size <= 0) return;
		if(isMainThread()) return;
		synchronized(this.threads) {
			final int count = this.threads.size();
			final int globalCount = getGlobalThreadCount();
			final int globalFree  = GLOBAL_LIMIT - globalCount;
			final int free = utilsMath.MinMax(count - this.active, 0, globalFree);
			log().finer(
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
					log().warning("Global max threads limit [ "+Integer.toString(globalCount)+" ] reached!");
				else
					log().warning("Global max threads limit [ "+Integer.toString(globalCount)+" ] reached!");
				utilsThread.Sleep(10L);
				return;
			}
			// max threads (this pool)
			if(count >= this.size) {
				if(this.size > 1) {
					if(this.coolMaxReached.runAgain())
						log().warning("Max threads limit [ "+Integer.toString(count)+" ] reached!");
					else
						log().warning("Max threads limit [ "+Integer.toString(count)+" ] reached!");
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
		while(true) {
			if(this.stopping && !isMainThread()) {
				log().finer("Stopping thread ("+this.queueName+":"+Integer.toString(threadId)+")");
				break;
			}
			xRunnable task = null;
			try {
				task = this.queue.poll(1, xTimeU.S);
			} catch (InterruptedException ignore) {
				break;
			}
			if(!isMainThread()) {
				// sleeping thread
				if(task == null) {
					// stop inactive thread after 5 minutes
					if(sleeping.value >= threadInactiveTimeout.value) {
						log().finer("Inactive thread.. ("+this.queueName+":"+Integer.toString(threadId)+")");
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
					log().trace(e);
				}
				// task finished
				this.active--;
				if(this.active < 0) this.active = 0;
				Thread.currentThread().setName(this.queueName);
			}
		}
		log().finer("Thread stopped ("+this.queueName+":"+Integer.toString(threadId)+")");
		synchronized(this.threads) {
			this.threads.remove(Thread.currentThread());
		}
	}


	public void runLater(final Runnable run) {
		if(run == null) throw new NullPointerException("run cannot be null");
		final xRunnable task = xRunnable.cast(run);
		// add to main thread queue
		if(this.size <= 0 && !isMainThread()) {
			get().runLater(run);
			return;
		}
		try {
			if(this.queue.offer(task, 5, xTimeU.S))
				log().finer("Task queued.. ("+this.queueName+") "+task.getTaskName());
			else
				log().warning("Thread queue jammed! ("+this.queueName+") "+task.getTaskName());
		} catch (InterruptedException ignore) {
			return;
		}
		newThread();
	}


	protected int getNextThreadId() {
		synchronized(this.nextLock) {
			final int next = this.nextThreadId;
			this.nextThreadId++;
			return next;
		}
	}


	private boolean isMainThread() {
		return this.queueName.toLowerCase().equals("main");
	}


	/**
	 * Stop all thread pools.
	 */
	public static void ShutdownAll() {
//TODO: queue and delay this
		synchronized(instances) {
			if(instances.isEmpty()) return;
			// shutdown threads
			for(final xThreadPool pool : instances.values())
				pool.stop();
			// wait for threads to stop
			Iterator<Entry<String, xThreadPool>> it = instances.entrySet().iterator();
			while(it.hasNext()) {
				final Entry<String, xThreadPool> entry = it.next();
				final xThreadPool pool = entry.getValue();
				try {
					pool.wait();
				} catch (InterruptedException e) {
					log().trace(e);
				}
				it.remove();
			}
		}
	}


	public static void Exit() {
		get().runLater(new xRunnable("Exit") {
			@Override
			public void run() {
				// display threads still running
				displayStillRunning();
				System.out.println();
				System.out.println();
				System.exit(0);
			}
		});
	}


	// display threads still running
	protected static void displayStillRunning() {
		String[] names = utilsThread.getThreadNames();
		if(names == null || names.length == 0) return;
		String msg = "Threads still running:  [ "+Integer.toString(names.length)+" ]";
		for(String name : names)
			msg += "\n  "+name;
		System.out.println(msg);
		//pxnLog.get().Publish(msg);
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
			this.size = utilsMath.MinMax(value.intValue(), 1, HARD_LIMIT);
	}


	// logger
	public static xLog log() {
		return xLog.getRoot();
	}


}
