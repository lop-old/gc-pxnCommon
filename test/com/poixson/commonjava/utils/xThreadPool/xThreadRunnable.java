package com.poixson.commonjava.utils.xThreadPool;

import java.util.concurrent.atomic.AtomicInteger;

import com.poixson.commonjava.Utils.threads.xThreadPool;
import com.poixson.commonjava.xLogger.xLogTest;


public class xThreadRunnable implements Runnable {

	private final int taskCount;
	private final AtomicInteger count = new AtomicInteger(0);

	private volatile boolean finished = false;



	public xThreadRunnable(final int taskCount) {
		this.taskCount = taskCount;
	}



	@Override
	public void run() {
		final int c = this.count.incrementAndGet();
		if(c >= this.taskCount)
			this.finished = true;
		if(xThreadPool.DETAILED_LOGGING)
			xLogTest.publish("Tick "+Integer.toString(this.count.get()));
	}



	public boolean hasFinished() {
		return this.finished;
	}



}
