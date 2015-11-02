package com.poixson.commonjava.utils.xThreadPool;

import org.junit.Assert;

import com.poixson.commonjava.Failure;
import com.poixson.commonjava.Utils.CoolDown;
import com.poixson.commonjava.Utils.utilsNumbers;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.exceptions.RequiredArgumentException;
import com.poixson.commonjava.Utils.threads.xThreadPool;
import com.poixson.commonjava.xLogger.xLogTest;


class xThreadQueuer {



	public xThreadQueuer(final xThreadPool pool, final int taskCount) {
		if(pool == null)  throw new RequiredArgumentException("pool");
		if(taskCount < 1) throw new IllegalArgumentException();
		assertHasntFailed();
		// start thread pool
		pool.Start();
		utilsThread.Sleep(10L);
		assertHasntFailed();
		// timeout
		final CoolDown cool = CoolDown.get(xThreadPoolFiringTest.MAX_RUN_TIME);
		cool.resetRun();
		// queue tasks
		final xThreadRunnable run = new xThreadRunnable(taskCount);
		for(int i=0; i<taskCount; i++) {
			Assert.assertFalse(
					"Tasks took to long to complete!",
					cool.runAgain()
			);
			pool.runLater(run);
		}
		assertHasntFailed();
		// wait for tasks to finish
		while(!run.hasFinished()) {
			Assert.assertFalse(
					"Tasks took to long to complete!",
					cool.runAgain()
			);
			utilsThread.Sleep(10L);
		}
		final long since = cool.getTimeSince();
		xLogTest.publish("Finished tasks in "+Long.toString(since)+
				"ms to run "+Long.toString(taskCount)+" tasks  "+
				utilsNumbers.FormatDecimal( "0.000", ((double)since) / ((double)taskCount))+"ms per task  "+
				utilsNumbers.FormatDecimal( "0.0",   ((double)taskCount) / ((double)since))+" tasks per ms."
		);
		// stop thread pool
		final int count = pool.getThreadCount();
		xLogTest.publish("Active threads: "+Integer.toString(pool.getActiveCount())+
				" ["+Integer.toString(count)+"]");
		pool.Stop();
		assertHasntFailed();
		// wait for pool to stop
		if(count > 0 && !pool.isMainPool()) {
			while(pool.isRunning()) {
				xLogTest.publish("Waiting for pool to stop..");
				utilsThread.Sleep(100L);
				xLogTest.publish("Active threads: "+Integer.toString(pool.getActiveCount())+
						" ["+Integer.toString(pool.getThreadCount())+"]");
			}
		}
		assertHasntFailed();
	}



	public static void assertHasntFailed() {
		Assert.assertFalse(
			"Failed!",
			Failure.hasFailed()
		);
	}



}
