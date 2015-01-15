package com.poixson.commonjava.utils.xThreadPool;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.junit.Assert;

import com.poixson.commonjava.Utils.CoolDown;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xThreadPool;
import com.poixson.commonjava.xLogger.xLog;
import com.poixson.commonjava.xLogger.xLogTest;


class xThreadQueuer {

	private static final Set<xThreadQueuer> instances = new CopyOnWriteArraySet<xThreadQueuer>();
	private static volatile boolean allFinished = false;
	private static final CoolDown maxRunTime = CoolDown.get("10s");

	private final xThreadPool pool;
	private final int maxTaskCount;
	private volatile int currentTaskCount = 0;
	private volatile boolean finished = false;



	public xThreadQueuer(final xThreadPool pool, final int maxTaskCount) {
		this.pool = pool;
		this.maxTaskCount = maxTaskCount;
		this.currentTaskCount = 0;
		this.logLocal().info("Testing with "+
				(pool.getMaxThreads() == 0
					? "MAIN thread"
					: Integer.toString(pool.getMaxThreads())+" threads"
				)+" and "+
				Integer.toString(maxTaskCount)+" tasks");
		// reset cooldown
		maxRunTime.reset();
		maxRunTime.runAgain();
		this.finished = false;
		instances.add(this);
	}



	public boolean hasFinished() {
		return this.finished;
	}
	public static boolean allFinished() {
		return allFinished;
	}



	public static void runAll() {
		// reset cooldown
		maxRunTime.reset();
		maxRunTime.runAgain();
		boolean allfinished;
		boolean hasadded;
		while(true) {
			allfinished = true;
			hasadded = false;
			// run the instances
			final Iterator<xThreadQueuer> it = instances.iterator();
			while(it.hasNext()) {
				final xThreadQueuer queuer = it.next();
				if(!queuer.hasFinished()) {
					// run instance
					if(queuer.queueMore())
						hasadded = true;
					allfinished = false;
				}
			}
			if(allfinished)
				break;
			// max run timeout
			Assert.assertFalse(
				"Run Timeout!",
				maxRunTime.runAgain()
			);
			if(!hasadded)
				utilsThread.Sleep(10L);
		}
		xLogTest.get().info("Finished xThreadPool Tests");
		allFinished = true;
	}



	public boolean queueMore() {
		if(this.maxTaskCount <= this.currentTaskCount) {
			while(!this.pool.isEmpty())
				utilsThread.Sleep(10L);
			this.logLocal().info("Finished testing pool");
			this.finished = true;
			return false;
		}
		if(Math.min(1, this.pool.getMaxThreads()) + 5 <= this.pool.getActiveCount())
			return false;
		// queue another task
		this.pool.runLater(
			new Runnable() {
				@Override
				public void run() {
					if(xThreadPool.DETAILED_LOGGING)
						logLocal().warning("TICK");
				}
			}
		);
		this.currentTaskCount++;
		return true;
	}



	// logger
	public xLog logLocal() {
		return xLogTest.get()
				.get(this.pool.getName());
	}



}
