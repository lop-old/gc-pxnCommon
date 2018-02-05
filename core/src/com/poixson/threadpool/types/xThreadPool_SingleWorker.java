package com.poixson.threadpool.types;

import java.util.concurrent.atomic.AtomicReference;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.threadpool.xThreadPool;
import com.poixson.threadpool.xThreadPoolQueue;
import com.poixson.threadpool.xThreadPoolWorker;
import com.poixson.utils.StringUtils;


public abstract class xThreadPool_SingleWorker extends xThreadPoolQueue {

	protected final AtomicReference<xThreadPoolWorker> worker =
			new AtomicReference<xThreadPoolWorker>(null);



	protected xThreadPool_SingleWorker(final String poolName) {
		super(poolName);
	}



	// ------------------------------------------------------------------------------- //
	// start/stop workers



	@Override
	public void run() {
		// existing worker
		if (this.worker.get() != null)
			throw new RuntimeException("Single thread pool is already registered");
		// new worker
		final xThreadPoolWorker worker = new xThreadPoolWorker(this);
		worker.run();
	}
	@Override
	protected void startNewWorkerIfNeededAndAble() {
		// existing worker
		if (this.worker.get() != null)
			return;
		// new worker
		final xThreadPoolWorker worker = new xThreadPoolWorker(this);
		worker.startAndWait();
		final xThreadPoolWorker existing = this.worker.get();
		if ( ! worker.equals(existing) ) {
			worker.stop();
			throw new RuntimeException(
				StringUtils.MergeObjects(
					' ',
					"Cannot start, invalid worker registered!",
					worker.getWorkerIndex(),
					(
						existing == null
						? "-null-"
						: Long.valueOf( existing.getWorkerIndex() )
					)
				)
			);
		}
	}



	@Override
	public void joinWorkers(final long timeout) {
		final xThreadPoolWorker worker = this.worker.get();
		if (worker == null)
			return;
		try {
			worker.join(timeout);
		} catch (InterruptedException e) {
			this.log().trace(e);
		}
	}
	@Override
	public void joinWorkers() {
		this.joinWorkers(0L);
	}



	@Override
	public void registerWorker(final xThreadPoolWorker worker) {
		if (worker == null) throw new RequiredArgumentException("worker");
		if ( ! this.worker.compareAndSet(null, worker) )
			throw new RuntimeException("Single thread pool is already registered");
//		this.runningCount.incrementAndGet();
//		this.running.set(true);
	}
	@Override
	public void unregisterWorker(final xThreadPoolWorker worker) {
		if (worker == null) throw new RequiredArgumentException("worker");
		if ( ! this.worker.compareAndSet(this.getWorker(), null) )
			throw new RuntimeException("Cannot unregister this worker, not owned by pool!");
//		if (this.runningCount.decrementAndGet() <= 0) {
//			this.running.set(false);
//		}
		// ensure it's stopping
		worker.stop();
	}



	// ------------------------------------------------------------------------------- //
	// config



	@Override
	public xThreadPool setThreadPriority(final int priority) {
		if (super.setThreadPriority(priority) == null)
			return null;
		final xThreadPoolWorker worker = this.worker.get();
		if (worker != null) {
			worker.setPriority(priority);
		}
		return this;
	}



	// ------------------------------------------------------------------------------- //



	public xThreadPoolWorker getWorker() {
		return this.worker.get();
	}
	@Override
	public boolean isCurrentThread() {
		final xThreadPoolWorker worker = this.getWorker();
		if (worker == null)
			return false;
		return worker.isCurrentThread();
	}



	// ------------------------------------------------------------------------------- //
	// pool state



	@Override
	public xThreadPoolWorker getCurrentWorker() {
		final xThreadPoolWorker worker = this.worker.get();
		if (worker == null)
			return null;
		return (
			worker.isCurrentThread()
			? worker
			: null
		);
	}



	@Override
	public boolean isRunning() {
		return (this.worker.get() != null);
	}



	// ------------------------------------------------------------------------------- //
	// stats



	public long getNextWorkerIndex() {
		return 1L;
	}



}
