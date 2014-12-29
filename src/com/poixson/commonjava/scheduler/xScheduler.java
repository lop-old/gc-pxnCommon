package com.poixson.commonjava.scheduler;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xStartable;
import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.xLogger.xLog;


public class xScheduler implements xStartable {

	private static volatile xScheduler instance = null;
	private static final Object instanceLock = new Object();

	protected final xTime threadSleepTime = xTime.get("5s");

	protected final Set<xScheduledTask> tasks = new CopyOnWriteArraySet<xScheduledTask>();
//	protected final Set<xScheduledTask> soon  = new CopyOnWriteArraySet<xScheduledTask>();

	protected final Thread thread;
	protected volatile boolean running  = false;
	protected volatile boolean stopping = false;



	public static xScheduler get() {
		if(instance == null) {
			synchronized(instanceLock) {
				if(instance == null)
					instance = new xScheduler();
			}
		}
		return instance;
	}
	protected xScheduler() {
		this.thread = new Thread() {
			private volatile xScheduler sched = null;
			public Thread init(final xScheduler sched) {
				this.sched = sched;
				return this;
			}
			@Override
			public void run() {
				this.sched.run();
			}
		}.init(this);
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}



	@Override
	public void Start() {
		if(this.stopping) throw new RuntimeException("Scheduler already stopped"); 
		if(this.running)  throw new RuntimeException("Scheduler already running");
		synchronized(this.thread){
			if(this.stopping) throw new RuntimeException("Scheduler already stopped");
			if(this.running)  throw new RuntimeException("Scheduler already running");
			this.thread.start();
		} 
	}
	@Override
	public void Stop() {
		this.stopping = true;
	}
	@Override
	public boolean isRunning() {
		if(this.stopping) return false;
		return this.running;
	}



	// scheduler manager loop
	@Override
	public void run() {
		if(this.stopping) throw new RuntimeException("Scheduler already stopped");
		if(this.running)  throw new RuntimeException("Scheduler already running");
		synchronized(this.thread) {
			if(this.stopping) throw new RuntimeException("Scheduler already stopped");
			if(this.running)  throw new RuntimeException("Scheduler already running");
			this.running = true;
		}
		xLog.getRoot().warning("Starting xScheduler");
		while(!this.stopping) {
			long sleep = this.threadSleepTime.getMS();
			// check task triggers
			final Iterator<xScheduledTask> it = this.tasks.iterator();
			while(it.hasNext()) {
				final xScheduledTask task = it.next();
				final long s = task.untilNextTrigger();
				if(s == -1) continue;
				// trigger now
				if(s == 0)
					task.trigger();
				// sleep less
				if(s < sleep)
					sleep = s;
			}
			// sleep thread
			sleep = (long) (((double) sleep) * 0.9);
			if(sleep > 0)
				utilsThread.Sleep(sleep);
		}
		xLog.getRoot().finer("Stopped xScheduler thread");
		this.stopping = true;
		this.running = false;
	}



	public void schedule(final xScheduledTask task) {
		this.tasks.add(task);
		this.thread.interrupt();
	}



	public boolean hasTask(final String name) {
		final Iterator<xScheduledTask> it = this.tasks.iterator();
		while(it.hasNext()) {
			if(it.next().taskNameEquals(name))
				return true;
		}
		return false;
	}
	public boolean cancel(final String name) {
		boolean found = false;
		final Iterator<xScheduledTask> it = this.tasks.iterator();
		while(it.hasNext()) {
			final xScheduledTask task = it.next();
			if(task.taskNameEquals(name)) {
				it.remove();
				found = true;
			}
		}
		return found;
	}
	public boolean cancel(final xScheduledTask task) {
		if(task == null) return false;
		return this.tasks.remove(task);
	}



}
