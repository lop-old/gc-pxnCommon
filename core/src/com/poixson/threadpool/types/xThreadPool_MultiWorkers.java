package com.poixson.threadpool.types;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.threadpool.xThreadPool;
import com.poixson.threadpool.xThreadPoolQueue;
import com.poixson.threadpool.xThreadPoolWorker;
import com.poixson.utils.NumberUtils;
import com.poixson.utils.ThreadUtils;


public abstract class xThreadPool_MultiWorkers extends xThreadPoolQueue {

	protected final CopyOnWriteArraySet<xThreadPoolWorker> workers =
			new CopyOnWriteArraySet<xThreadPoolWorker>();

	protected volatile int maxWorkers = DEFAULT_MAX_WORKERS;

	// stats/counts
	private final AtomicLong workerIndexCount = new AtomicLong(0L);



	protected xThreadPool_MultiWorkers(final String poolName) {
		super(poolName);
	}



	// ------------------------------------------------------------------------------- //
	// start/stop workers



	@Override
	public void stop() {
		super.stop();
		OUTER_LOOP:
		for (int i=0; i<3; i++) {
			if (i > 0) {
				ThreadUtils.Sleep(20L);
			}
			if (this.workers.isEmpty())
				break OUTER_LOOP;
			final Iterator<xThreadPoolWorker> it = this.workers.iterator();
			//INNER_LOOP:
			while (it.hasNext()) {
				final xThreadPoolWorker worker = it.next();
				worker.stop();
			}
		}
	}



//TODO: add this?
	@Override
	public void run() {
		throw new UnsupportedOperationException("Not supported");
	}



	@Override
	public void joinWorkers(final long timeout) {
		OUTER_LOOP:
		while (true) {
			if (this.workers.isEmpty())
				break OUTER_LOOP;
			final int count = this.workers.size();
			final Iterator<xThreadPoolWorker> it = this.workers.iterator();
			//INNER_LOOP:
			while (it.hasNext()) {
				final xThreadPoolWorker worker = it.next();
				try {
					if (timeout > 0L) {
						final long timot = (long)
							Math.ceil(
								( ((double)timeout) / ((double)count) )
							);
						worker.join(timot);
					} else {
						worker.join();
					}
				} catch (InterruptedException e) {
					this.log().trace(e);
				}
			} // end INNER_LOOP
			if (timeout > 0L)
				break OUTER_LOOP;
		} // end OUTER_LOOP
	}
	@Override
	public void joinWorkers() {
		this.joinWorkers(0L);
	}



	public void registerWorker(final xThreadPoolWorker worker) {
		if (worker == null) throw new RequiredArgumentException("worker");
		this.workers.add(worker);
//		this.runningCount.incrementAndGet();
//		this.running.set(true);
	}
	public void unregisterWorker(final xThreadPoolWorker worker) {
		if (worker == null) throw new RequiredArgumentException("worker");
		if (this.workers.remove(worker)) {
//			if (this.runningCount.decrementAndGet() <= 0) {
//				this.running.set(false);
//			}
		}
		// ensure it's stopping
		worker.stop();
	}



	@Override
	protected void startNewWorkerIfNeededAndAble() {
//TODO:
//use xThreadFactory
		
		
		
		
		
		
		
		
		
	}



	// ------------------------------------------------------------------------------- //
	// config



	@Override
	public xThreadPool setThreadPriority(final int priority) {
		if (super.setThreadPriority(priority) == null)
			return null;
		ThreadUtils.Sleep(5L);
		final Iterator<xThreadPoolWorker> it = this.workers.iterator();
		while (it.hasNext()) {
			final xThreadPoolWorker worker = it.next();
			worker.setPriority(priority);
		}
		return this;
	}



	// pool size
	public int getMaxWorkers() {
		return this.maxWorkers;
	}
	public void setMaxWorkers(final int maxWorkers) {
		this.maxWorkers = NumberUtils.MinMax(maxWorkers, 0, GLOBAL_MAX_WORKERS);
	}



	// force to run tasks in main pool
	@Override
	public boolean imposeMainPool() {
		return (this.maxWorkers <= 0);
	}
	@Override
	public void setImposeMainPool() {
		this.setMaxWorkers(0);
	}



	// ------------------------------------------------------------------------------- //
	// state



	@Override
	public boolean isSingleWorker() {
		return (this.maxWorkers <= 1);
	}



	@Override
	public xThreadPoolWorker getCurrentWorker() {
		if (this.workers.isEmpty())
			return null;
		final Thread current = Thread.currentThread();
		final Iterator<xThreadPoolWorker> it =
			this.workers.iterator();
		while (it.hasNext()) {
			final xThreadPoolWorker worker = it.next();
			if (worker.isThread(current))
				return worker;
		}
		return null;
	}



	@Override
	public boolean isRunning() {
		return (this.workers.size() > 0);
	}



	// ------------------------------------------------------------------------------- //
	// stats



	@Override
	public long getNextWorkerIndex() {
		return this.workerIndexCount
				.incrementAndGet();
	}



}
