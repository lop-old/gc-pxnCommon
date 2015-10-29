package com.poixson.commonjava.utils;

import org.junit.Assert;
import org.junit.Test;

import com.poixson.commonjava.Utils.CoolDown;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.xLogger.xLogTest;


public class CoolDownTest {
	static final String TEST_NAME = "CoolDown";

	static final long TEST_SLEEP_TIME   = 500L;
	static final long DEVIATION_ALLOWED = 50L;



	public CoolDownTest() {
	}



	@Test (timeout=2000)
	public void testCoolDownDelay() {
		xLogTest.testStart(TEST_NAME);
		try {
			final CoolDown cool = CoolDown.get("1s");
			cool.resetRun();
			xLogTest.publish("Sleeping "+Long.toString(TEST_SLEEP_TIME)+"ms..");
			utilsThread.Sleep(TEST_SLEEP_TIME);
			// check time values
			final long since = cool.getTimeSince();
			final long until = cool.getTimeUntil();
			Assert.assertFalse(
					"Time since sleep started is less than expected!",
					since < TEST_SLEEP_TIME
					);
			xLogTest.publish("Time Since: "+Long.toString(since));
			this.verifyTime(since);
			xLogTest.publish("Time Until: "+Long.toString(until));
			this.verifyTime(until);
		} catch (Exception e) {
			xLogTest.trace(e);
			throw new RuntimeException(e);
		} catch (Error e) {
			xLogTest.trace(e);
			throw new RuntimeException(e);
		}
		xLogTest.testPassed(TEST_NAME);
	}



	private void verifyTime(final long value) {
		Assert.assertNotEquals(-1L, value);
		final long val = value - TEST_SLEEP_TIME;
		final long v = (val < 0L ? val * -1L : val);
		Assert.assertFalse("Time result out of range! result: "+Long.toString(value), v > DEVIATION_ALLOWED);
	}



}
