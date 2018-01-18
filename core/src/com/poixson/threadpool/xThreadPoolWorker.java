package com.poixson.threadpool;

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.poixson.logger.xLog;
import com.poixson.tools.CoolDown;


public class xThreadPoolWorker extends Thread {

	private final xThreadPool pool;

	// worker index
	private static final AtomicLong workerIndexCount = new AtomicLong(0L);
	private final long workerIndex;

	// worker state
	private final AtomicBoolean running = new AtomicBoolean(false);
	private volatile boolean active   = false;
	private volatile boolean stopping = false;



	public xThreadPoolWorker(final xThreadPool pool) {
		super(
			pool.group,
			(Runnable) null
		);
		this.pool = pool;
		this.workerIndex = workerIndexCount.incrementAndGet();
		// configure thread
		this.setName(pool.getName());
		this.setDaemon(false);
		this.setPriority(pool.priority);
	}



	public boolean isRunning() {
		return this.running.get();
	}
	public boolean isActive() {
		return this.active;
	}
	public boolean isStopping() {
		return this.stopping;
	}
	public void Stop() {
		this.stopping = true;
	}



	public long getWorkerIndex() {
		return this.workerIndex;
	}



	// worker run loop
	@Override
	public void run() {
		if (!this.running.compareAndSet(false, true))
			return;
		this.log()
			.finer("Started worker thread..");
		// inactive thread timer
		final CoolDown inactive =
			CoolDown.getNew(
				xThreadPool.INACTIVE_THREAD_TIMEOUT
			);
		inactive.resetRun();
		// worker run loop
		while (true) {
			// get task from queues
			xThreadPoolTask task = null;
			try {
				task = this.pool.getTaskToRun();
			} catch (InterruptedException e) {
				this.log()
					.trace(e);
				break;
			}
			// run the task
			if (task != null) {
				this.active = true;
				task.run();
				this.active = false;
				inactive.resetRun();
				continue;
			}
			// inactive thread
			// or stopping pool
			if (this.isStopping() || inactive.runAgain()) {
				// main pool
				if (this.pool.isMainPool()) {
					// always leave one main pool thread
					if (this.pool.workerCount.get() > 1) {
						if (this.pool.workerCount.decrementAndGet() > 0) {
							this.log()
								.finer(
									this.isStopping()
									? "Stopping pool thread.."
									: "Stopping inactive main thread.."
								);
							break;
						}
						this.pool.workerCount.incrementAndGet();
					}
				// other (not main pool)
				} else {
					this.pool.workerCount.decrementAndGet();
					this.log()
						.finer(
							this.isStopping()
							? "Stopping pool thread.."
							: "Stopping inactive thread.."
						);
					break;
				}
			}
			this.log()
				.detail("Idle thread..");
		}
		// remove stopped worker thread
		this.pool.workers
			.remove(this);
		this.running.set(false);
		this.pool.workerCount.decrementAndGet();
	}



	// logger
	private volatile SoftReference<xLog> _log = null;
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null) {
				return log;
			}
		}
		final xLog log =
			this.pool.log()
				.getWeak(
					Long.toString(this.workerIndex)
				);
		this._log = new SoftReference<xLog>(log);
		return log;
	}



}
