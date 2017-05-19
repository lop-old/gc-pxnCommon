package com.poixson.utils.xThreadPool;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import com.poixson.utils.ThreadUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.xClock;
import com.poixson.utils.xRunnable;


public class xThreadPoolFactory {
	private xThreadPoolFactory() {}

	public static final String MAIN_POOL_NAME = xThreadPool.MAIN_POOL_NAME;

	// thread pool instances
	protected static final ConcurrentMap<String, xThreadPool> pools =
			new ConcurrentHashMap<String, xThreadPool>();

	protected static final AtomicReference<xThreadPool> mainPool =
			new AtomicReference<xThreadPool>(null);

	// task hang monitor
	protected static final HangMonitorThread hangMonitor =
			new HangMonitorThread();
	protected static final AtomicReference<Thread> hangMonitorThread =
			new AtomicReference<Thread>(null);



	/**
	 * Get main thread queue
	 */
	public static xThreadPool getMainPool() {
		// existing instance
		{
			final xThreadPool pool = mainPool.get();
			if (pool != null)
				return pool;
		}
		// new instance
		{
			final xThreadPool pool = new xThreadPool(MAIN_POOL_NAME);
			final xThreadPool existing = pools.putIfAbsent(MAIN_POOL_NAME, pool);
			if (existing != null)
				return existing;
			if (!mainPool.compareAndSet(null, pool)) {
				final xThreadPool p = pools.get(MAIN_POOL_NAME);
				mainPool.set(p);
				return p;
			}
			return pool;
		}
	}
	/**
	 * Get thread queue by name
	 */
	public static xThreadPool get(final String poolName) {
		// default to main pool
		if (Utils.isEmpty(poolName))
			return getMainPool();
		if (MAIN_POOL_NAME.equalsIgnoreCase(poolName))
			return getMainPool();
		// use existing pool instance
		{
			final xThreadPool pool = pools.get(poolName);
			if (pool != null) {
				return pool;
			}
		}
		// new pool instance
		final xThreadPool pool;
		{
			pool = new xThreadPool(poolName);
			final xThreadPool existing =
				pools.putIfAbsent(poolName, pool);
			if (existing != null) {
				return existing;
			}
		}
		// start task hang monitor thread
		if (hangMonitorThread.get() == null) {
			final Thread thread = new Thread(hangMonitor);
			if (hangMonitorThread.compareAndSet(null, thread)) {
				thread.setDaemon(true);
				thread.start();
			}
		}
		return pool;
	}



	public static String[] getPoolNames() {
		return pools.keySet()
				.toArray(new String[0]);
	}



	protected static class HangMonitorThread extends xRunnable {

		private final xClock clock = xClock.get(true);

		public HangMonitorThread() {
			super("xThreadPoolHangMonitor");
		}

		@Override
		public void run() {
			final long currentTime = this.clock.getCurrentTime();
			final Iterator<xThreadPool> it =
					pools.values().iterator();
			while (it.hasNext()) {
				final xThreadPool pool = it.next();
				pool.checkTaskTimeouts(currentTime);
				ThreadUtils.Sleep(1000L);
			}
		}

	}



	/**
	 * Stop all thread pools (except main)
	 */
	public static void ShutdownAll() {
//TODO:
//		if (!Thread.currentThread().equals(mainThread)) {
//		getMainPool().runNow(new Runnable() {
//			@Override
//			public void run() {
//				ShutdownAll();
//			}
//		});
//		return;
		// run in main thread pool
		getMainPool().runLater(
			new xRunnable("xThreadPool-Shutdown") {
				@Override
				public void run() {
					// stop threads
					final Iterator<xThreadPool> it = pools.values().iterator();
					while (it.hasNext()) {
						final xThreadPool pool = it.next();
						pool.Stop();
					}
				}
			}
		);
	}
//TODO:
/*
		// wait for threads to stop
		{
			final Iterator<xThreadPool> it = pools.values().iterator();
			while (it.hasNext()) {
				final xThreadPool pool = it.next();
				if (pool.isMainPool())
					continue;
				try {
					synchronized(pool) {
						pool.wait();
					}
				} catch (InterruptedException e) {
					xLog.getRoot()
						.trace(e);
				}
				it.remove();
			}
		}
//TODO: after all threads have stopped
pool.running.set(false);
	}
	public static void Exit() {
		try {
			getMainPool().runLater(
					new RemappedRunnable(
							xThreadPool.class,
							"ExitNow"
					)
			);
		} catch (Exception e) {
			xLog.getRoot().trace(e);
			ExitNow(1);
		}
	}
	public static void ExitNow() {
		ExitNow(0);
	}
	public static void ExitNow(final int status) {
		// display threads still running
		utilsThread.displayStillRunning();
		System.out.println();
		System.out.println();
		System.exit(status);
	}
*/



}
