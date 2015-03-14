package com.poixson.commonjava.scheduler;

import org.junit.Assert;
import org.junit.Test;

import com.poixson.commonjava.Failure;
import com.poixson.commonjava.Utils.CoolDown;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xClock;
import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.Utils.threads.xThreadPool;
import com.poixson.commonjava.scheduler.cron.CronPredictor;
import com.poixson.commonjava.scheduler.triggers.CronTest;
import com.poixson.commonjava.scheduler.triggers.IntervalTest;
import com.poixson.commonjava.xLogger.xLog;
import com.poixson.commonjava.xLogger.xLogTest;


public class xSchedulersFiringTest {

	private static final boolean QUICK_CRON_TEST = true;

	private static volatile Thread mainThread = null;
	private final xScheduler sched;

	// trigger tests
	private final CronTest     test_cron;
	private final IntervalTest test_interval;

	private static final CoolDown maxRunTime = CoolDown.get("20s");
	private static final xTime wait_sleep = xTime.get("1s");



	public xSchedulersFiringTest() {
		assertHasntFailed();
		log().info("Starting xScheduler Firing Test..");
		this.sched = xScheduler.get();
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

		// cron test
		{
			this.test_cron = null;
			final CronPredictor predictor = new CronPredictor("* * * * *");
			final long now = xClock.get().millis();
			final long untilA = (long) Math.ceil( ((double)predictor.untilNextMatching(now)) / 1000.0 );
			final long untilB = 60 - (((long) Math.floor(((double)now)/1000.0) ) % 60);
			Assert.assertEquals(
				"CronPredictor calculated wrong value!"+
						"  calculated: "+Long.toString(untilA)+
						"  actual: "+Long.toString(untilB),
				untilA,
				untilB
			);
			log().info("CronPredictor successfully calculated next trigger!");
		}
		if(!QUICK_CRON_TEST)
			this.test_cron = new CronTest(this.sched);
		// interval test
		this.test_interval = new IntervalTest(this.sched);

		utilsThread.Sleep(20L);
		assertHasntFailed();
		// reset cooldown
		maxRunTime.reset();
		maxRunTime.runAgain();
	}
	@Test
	public void StartFiringTest() {
		assertHasntFailed();
		// start scheduler
		this.sched.Start();
		log().publish();
		assertHasntFailed();
		// wait until finished
		log().info("Waiting for schedulers..");
		boolean allfinished = true;
		while(true) {
			allfinished = true;
			if(this.test_cron     != null && !this.test_cron.hasFinished()    ) allfinished = false;
			if(this.test_interval != null && !this.test_interval.hasFinished()) allfinished = false;
			if(allfinished) break;
			// max run timeout
			Assert.assertFalse(
				"Run Timeout!",
				maxRunTime.runAgain()
			);
			utilsThread.Sleep(wait_sleep);
		}
		// ensure all have triggered
		if(this.test_cron != null) {
			Assert.assertTrue(
				"Cron task hasn't triggered!",
				this.test_cron.hasFinished()
			);
		}
		if(this.test_interval != null) {
			Assert.assertTrue(
				"Interval task hasn't triggered!",
				this.test_interval.hasFinished()
			);
		}
		assertHasntFailed();
		log().info("xScheduler Firing Tests Passed!");
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
