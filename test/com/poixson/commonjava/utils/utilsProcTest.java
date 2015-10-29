package com.poixson.commonjava.utils;

import org.junit.Assert;
import org.junit.Test;

import com.poixson.commonjava.Utils.utilsProc;
import com.poixson.commonjava.xLogger.xLogTest;


public class utilsProcTest {
	static final String TEST_GET_PID = "getPid()";



	@Test
	public void testGetPid() {
		xLogTest.testStart(TEST_GET_PID);
		final int pid = utilsProc.getPid();
		Assert.assertTrue("Failed to get pid! "+Integer.toString(pid), pid > 0);
		xLogTest.publish("Found PID: "+Integer.toString(pid));
		xLogTest.testPassed(TEST_GET_PID);
	}



//	@Test
//	public void testLockInstance() {
//		final String LOCK_FILE = "test.lock";
//		// get lock
//		{
//			final boolean result = utilsProc.lockInstance(LOCK_FILE);
//			Assert.assertTrue("Failed to lock file: "+LOCK_FILE, result);
//		}
//		// expect fail
//		{
//			final boolean result = utilsProc.lockInstance(LOCK_FILE);
//			Assert.assertFalse("Expected fail returned true: "+LOCK_FILE, result);
//		}
//	}



}
