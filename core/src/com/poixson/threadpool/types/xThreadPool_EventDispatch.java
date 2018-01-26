package com.poixson.threadpool.types;

import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import com.poixson.threadpool.xThreadPool;
import com.poixson.threadpool.xThreadPoolTask;
import com.poixson.threadpool.xThreadPoolWorker;


public class xThreadPool_EventDispatch extends xThreadPool_SingleWorker {

	public static final String DISPATCH_POOL_NAME = "event-dispatch";

	private final static AtomicReference<xThreadPool_EventDispatch> instance =
			new AtomicReference<xThreadPool_EventDispatch>(null);



	public static xThreadPool_EventDispatch get() {
		// existing instance
		{
			final xThreadPool_EventDispatch pool = instance.get();
			if (pool != null)
				return pool;
		}
		// new instance
		{
			final xThreadPool_EventDispatch pool = new xThreadPool_EventDispatch();
			if (!instance.compareAndSet(null, pool))
				return instance.get();
			return pool;
		}
	}



	protected xThreadPool_EventDispatch() {
		super(DISPATCH_POOL_NAME);
		new DispatchWorker(this);
	}



	@Override
	public void run() {
		throw new UnsupportedOperationException();
	}



	@Override
	protected void startNewWorkerIfNeededAndAble() {
		if (this.worker.get() == null) {
			final DispatchWorker worker = new DispatchWorker(this);
			this.worker.compareAndSet(null, worker);
		}
		SwingUtilities.invokeLater(
			this.worker.get()
		);
	}



	// ------------------------------------------------------------------------------- //
	// worker class



	protected class DispatchWorker extends xThreadPoolWorker {

		public DispatchWorker(final xThreadPool pool) {
			super(pool);
			pool.registerWorker(this);
		}

		@Override
		public void start() {}

		@Override
		protected void configureThread(final Thread thread) {}

		@Override
		public Thread getThread() {
			return this.thread.get();
		}

		@Override
		public void run() {
			if ( ! this.running.get() )
				this.running.set(true);
			{
				final Thread thread = this.thread.get();
				if (thread == null) {
					if ( this.thread.compareAndSet(null, Thread.currentThread()) ) {
						this.configureThread(thread);
					}
				} else {
					if ( ! thread.equals(Thread.currentThread()) )
						throw new IllegalStateException("Invalid thread state!");
				}
			}
			// get task from queues
			try {
				final xThreadPoolTask<?> task =
					this.pool.grabNextTask();
				// run the task
				if (task != null) {
					this.runTask(task);
					this.runCount.incrementAndGet();
					SwingUtilities.invokeLater(this);
					return;
				}
			} catch (InterruptedException e) {
				this.log()
					.trace(e);
				return;
			}
			// idle
			this.log()
				.detail("Idle thread..");
			if (this.stopping)
				this.running.set(false);
		}

	}



	// ------------------------------------------------------------------------------- //
	// which thread



	@Override
	public boolean isMainPool() {
		return true;
	}
	@Override
	public boolean isEventDispatchPool() {
		return false;
	}



}
