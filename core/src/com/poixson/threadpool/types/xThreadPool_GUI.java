package com.poixson.threadpool.types;

import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import com.poixson.threadpool.xThreadPool;
import com.poixson.threadpool.xThreadPoolTask;
import com.poixson.threadpool.xThreadPoolWorker;


public class xThreadPool_GUI extends xThreadPool_SingleWorker {

	public static final String DISPATCH_POOL_NAME = "gui";

	private final static AtomicReference<xThreadPool_GUI> instance =
			new AtomicReference<xThreadPool_GUI>(null);



	public static xThreadPool_GUI get() {
		// existing instance
		{
			final xThreadPool_GUI pool = instance.get();
			if (pool != null)
				return pool;
		}
		// new instance
		{
			final xThreadPool_GUI pool = new xThreadPool_GUI();
			if (instance.compareAndSet(null, pool))
				return pool;
			return instance.get();
		}
	}



	protected xThreadPool_GUI() {
		super(DISPATCH_POOL_NAME);
	}



	@Override
	public void run() {
		throw new UnsupportedOperationException();
	}



	@Override
	protected void startNewWorkerIfNeededAndAble() {
		xThreadPoolWorker worker = this.worker.get();
		if (worker == null) {
			try {
				worker = new xThreadPool_GUI_Worker(this);
				if ( ! this.worker.compareAndSet(null, worker) )
					worker = this.worker.get();
			} catch (RuntimeException ignore) {}
		}
		if (worker == null) throw new NullPointerException();
		SwingUtilities.invokeLater(
			this.worker.get()
		);
	}



	// ------------------------------------------------------------------------------- //
	// worker class



	protected class xThreadPool_GUI_Worker extends xThreadPoolWorker {

		public xThreadPool_GUI_Worker(final xThreadPool pool) {
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
				this.log().trace(e);
				return;
			}
			// idle
			this.log().detail("Idle thread..");
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
