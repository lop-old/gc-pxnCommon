package com.poixson.commonjava.utils.xThreadPool;

import org.junit.Assert;
import org.junit.Test;

import com.poixson.commonjava.Failure;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.threads.xThreadPool;
import com.poixson.commonjava.xLogger.xLog;
import com.poixson.commonjava.xLogger.xLogTest;


public class xThreadPoolFiringTest {

	private static final int THREAD_COUNT_SHORT = 1;
	private static final int THREAD_COUNT_LONG  = 20;
	private static final int TASK_COUNT_MAIN  = 1000;
	private static final int TASK_COUNT_SHORT = 1000;
	private static final int TASK_COUNT_LONG  = 1000;

	private static volatile Thread mainThread = null;

	private final xThreadPool pool_main;
	private final xThreadPool pool_short;
	private final xThreadPool pool_long;

	private volatile xThreadQueuer queuer_main  = null;
	private volatile xThreadQueuer queuer_short = null;
	private volatile xThreadQueuer queuer_long  = null;



	public xThreadPoolFiringTest() {
		assertHasntFailed();
		log().info("Starting up thread pools..");
		this.pool_main = xThreadPool.get();
		this.pool_short = xThreadPool.get("short", THREAD_COUNT_SHORT);
		this.pool_long  = xThreadPool.get("long",  THREAD_COUNT_LONG);
		assertHasntFailed();
		// start main thread pool
		mainThread = new Thread() {
			@Override
			public void run() {
				xThreadPool.get().run();
			}
		};
		mainThread.start();
		assertHasntFailed();
		// start more thread pools
		this.pool_short.Start();
		this.pool_long.Start();
		utilsThread.Sleep(20);
		assertHasntFailed();
	}



	@Test
	public void StartFiringTest() {
		assertHasntFailed();
		// task producers
		this.queuer_main  = new xThreadQueuer(this.pool_main,  TASK_COUNT_MAIN);
		this.queuer_short = new xThreadQueuer(this.pool_short, TASK_COUNT_SHORT);
		this.queuer_long  = new xThreadQueuer(this.pool_long,  TASK_COUNT_LONG);
		assertHasntFailed();
		// start producers
		log().info("Starting task producers..");
		log().publish();
		xThreadQueuer.runAll();
		log().publish();
		// ensure all finished
		Assert.assertTrue(this.queuer_main.hasFinished());
		Assert.assertTrue(this.queuer_short.hasFinished());
		Assert.assertTrue(this.queuer_long.hasFinished());
		assertHasntFailed();
		log().info("xThreadPool Tests Passed!");
	}



	public static void assertHasntFailed() {
		Assert.assertFalse(
			"App Failed!",
			Failure.hasFailed()
		);
	}



	// logger
	public static xLog log() {
		return xLogTest.get();
	}



}
