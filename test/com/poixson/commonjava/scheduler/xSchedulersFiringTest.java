package com.poixson.commonjava.scheduler;

import org.junit.Assert;
import org.junit.Test;

import com.poixson.commonjava.Failure;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.logger.xLogTest;
import com.poixson.commonjava.scheduler.triggers.ClockTest;
import com.poixson.commonjava.scheduler.triggers.CronTest;
import com.poixson.commonjava.scheduler.triggers.IntervalTest;
import com.poixson.commonjava.xLogger.xLog;


public class xSchedulersFiringTest {

	private final xScheduler sched;

	// trigger tests
	private final ClockTest    test_clock;
	private final CronTest     test_cron;
	private final IntervalTest test_interval;

	private int waitCount = 0;
	private static final String wait_sleep = "1s";
	private static final int max_wait_count = 10;



	public xSchedulersFiringTest() {
		assertHasntFailed();
		log().info("Starting xScheduler Firing Test..");
		this.sched = xScheduler.get();
		assertHasntFailed();
		// interval test
		this.test_clock    = new ClockTest   (this.sched);
		this.test_cron     = new CronTest    (this.sched);
		this.test_interval = new IntervalTest(this.sched);
		assertHasntFailed();
	}
	@Test
	public void StartTest() {
		assertHasntFailed();
		// start scheduler
		this.sched.Start();
		assertHasntFailed();
		// wait until finished
		for(this.waitCount=0; this.waitCount<max_wait_count; this.waitCount++) {
//TODO:
//			if( this.test_clock.hasFinished() &&
//				this.test_cron.hasFinished()  &&
if(
				this.test_interval.hasFinished() )
					break;
			utilsThread.Sleep(wait_sleep);
			log().fine("Waiting.. [ "+Integer.toString(this.waitCount)+" ]");
		}
		// ensure all have triggered
//		Assert.assertTrue(
//			"Clock task hasn't triggered!",
//			this.test_clock.hasFinished()
//		);
//		Assert.assertTrue(
//			"Cron task hasn't triggered!",
//			this.test_cron.hasFinished()
//		);
		Assert.assertTrue(
			"Interval task hasn't triggered!",
			this.test_interval.hasFinished()
		);
		// finished testing
		assertHasntFailed();
		log().info("xScheduler Firing Tests Passed!");
	}



	public static void assertHasntFailed() {
		Assert.assertFalse(Failure.hasFailed());
	}



	// logger
	public static xLog log() {
		return xLogTest.get();
	}



}
