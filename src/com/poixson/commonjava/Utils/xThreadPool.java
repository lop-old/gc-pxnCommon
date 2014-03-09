package com.poixson.commonjava.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class xThreadPool implements Runnable {

	// max threads
	public static final int HARD_LIMIT = 20;
	public static final int GLOBAL_LIMIT = 50;
	private volatile int size = 1;
	private volatile Integer nextThreadId = 1;
	private static final xTime threadSleepTime = xTime.get("200n");
	private static final xTime threadInactiveTimeout = xTime.get("5m");

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
	public static xThreadPool get(String name, final Integer size) {
		if(utils.isEmpty(name))
			name = "main";
		final String key = name.toLowerCase();
		synchronized(instances) {
			if(instances.containsKey(key))
				return instances.get(key);
			final xThreadPool pool = new xThreadPool(name, size);
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
			this.size = size;
		runLater(new xRunnable("Thread-Startup") {
			@Override
			public void run() {
				System.out.println("Started thread queue ("+queueName+")");
			}
		});
	}


	public void start() {
		newThread();
	}
	public void stop() {
		stopping = true;
	}


	/**
	 * Create a new thread if needed, skip if queue is empty.
	 */
	protected void newThread() {
		if(stopping || size <= 0) return;
		if(isMainThread()) return;
		synchronized(threads) {
			final int count = threads.size();
			final int globalCount = getGlobalThreadCount();
			final int globalFree  = GLOBAL_LIMIT - globalCount;
			final int free = utilsMath.MinMax(count - active, 0, globalFree);
			System.out.println(
				"Pool Size: "+Integer.toString(count)+" ["+Integer.toString(size)+"]  "+
				"Active/Free: "+
					Integer.toString(active)+"/"+
					Integer.toString(free)+"  "+
				"Global: "+Integer.toString(globalCount)+" ["+Integer.toString(GLOBAL_LIMIT)+"]"
			);
			// use an existing waiting thread
			if(free > 0) return;
			// restart an existing thread
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
		Thread.currentThread().setName(queueName);
		final xTime sleeping = xTime.get();
		while(true) {
			if(stopping && !isMainThread()) {
				System.out.println("Stopping thread ("+queueName+":"+Integer.toString(threadId)+")");
				break;
			}
			xRunnable task = null;
			try {
				task = queue.poll(1, xTimeU.S);
			} catch (InterruptedException ignore) {
				break;
			}
			if(!isMainThread()) {
				// sleeping thread
				if(task == null) {
					// stop inactive thread after 5 minutes
					if(sleeping.value >= threadInactiveTimeout.value) {
System.out.println("Inactive thread.. ("+queueName+":"+Integer.toString(threadId)+")");
						break;
					}
					sleeping.add(threadSleepTime);
					continue;
				}
			}
			// active thread
			if(task != null) {
				runCount++;
				active++;
				sleeping.reset();
				Thread.currentThread().setName(queueName+":"+Integer.toString(threadId)+"["+task.getTaskName()+"]");
				// run the task
				try {
					task.run();
					// low priority can sleep
					if(priority <= (Thread.NORM_PRIORITY - Thread.MIN_PRIORITY) / 2)
						utilsThread.Sleep(10L); // sleep 10ms
				} catch (Exception e) {
					e.printStackTrace();
				}
				// task finished
				active--;
				if(active < 0) active = 0;
				Thread.currentThread().setName(queueName);
			}
		}
		System.out.println("Thread stopped ("+queueName+":"+Integer.toString(threadId)+")");
		synchronized(threads) {
			threads.remove(Thread.currentThread());
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
			if(queue.offer(task, 5, xTimeU.S))
				System.out.println("Thread task queued.. "+queueName+"["+task.getTaskName()+"]");
			else
				System.out.println("Thread queue jammed! "+queueName+"["+task.getTaskName()+"]");
		} catch (InterruptedException ignore) {
			return;
		}
		newThread();
	}


	protected int getNextThreadId() {
		synchronized(nextThreadId) {
			final int next = nextThreadId;
			nextThreadId++;
			return next;
		}
	}


	private boolean isMainThread() {
		return queueName.toLowerCase().equals("main");
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
					e.printStackTrace();
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
	private static void displayStillRunning() {
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
		return threads.size();
	}
	/**
	 * Get the active thread count.
	 * @return number of active threads in the pool.
	 */
	public int getActiveCount() {
		return active;
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
	public void maxThreads(final Integer size) {
		this.size = (
			size == null ?
			1 :
			utilsMath.MinMax(size, 1, HARD_LIMIT)
		);
	}


}
