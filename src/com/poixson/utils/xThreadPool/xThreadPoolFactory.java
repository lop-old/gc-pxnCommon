package com.poixson.utils.xThreadPool;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

	protected static final xThreadPool mainPool = getMainPool();

	// task hang monitor
	protected static final HangMonitorThread hangMonitor =
			new HangMonitorThread();
	protected static volatile Thread hangMonitorThread = null;



	/**
	 * Get main thread queue
	 */
	public static xThreadPool getMainPool() {
		return get(null);
	}
	/**
	 * Get thread queue by name
	 */
	public static xThreadPool get(final String poolName) {
		// default to main pool
		if (Utils.isEmpty(poolName)) {
			if (mainPool != null)
				return mainPool;
			return get(MAIN_POOL_NAME);
		}
		if (MAIN_POOL_NAME.equalsIgnoreCase(poolName)) {
			if (mainPool != null)
				return mainPool;
			if ( ! MAIN_POOL_NAME.equals(poolName)) {
				return get(MAIN_POOL_NAME);
			}
		}
		// use existing pool instance
		{
			final xThreadPool pool = pools.get(poolName);
			if (pool != null) {
				return pool;
			}
		}
		// new pool instance
		synchronized(pools) {
			if (pools.containsKey(poolName)) {
				return pools.get(poolName);
			}
			// start task hang monitor thread
			if (hangMonitorThread == null) {
				hangMonitorThread = new Thread(hangMonitor);
				hangMonitorThread.setDaemon(true);
				hangMonitorThread.start();
			}
			// create new pool instance
			final xThreadPool pool = new xThreadPool(poolName);
			pools.put(
				poolName,
				pool
			);
			return pool;
		}
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
					synchronized(pools) {
						// stop threads
						final Iterator<xThreadPool> it = pools.values().iterator();
						while (it.hasNext()) {
							final xThreadPool pool = it.next();
							pool.Stop();
						}
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
