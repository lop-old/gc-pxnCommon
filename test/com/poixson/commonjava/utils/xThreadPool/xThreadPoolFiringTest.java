package com.poixson.commonjava.utils.xThreadPool;

import org.junit.Test;

import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.Utils.threads.xThreadPool;
import com.poixson.commonjava.xLogger.xLogTest;


//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class xThreadPoolFiringTest {
	static final String TEST_NAME = "xThreadPoolFiring";
	static final String TEST_NAME_MAIN  = "xThreadPool Main";
	static final String TEST_NAME_SHORT = "xThreadPool Short";
	static final String TEST_NAME_LONG  = "xThreadPool Long";

	protected static final xTime MAX_RUN_TIME = xTime.get("10s");

	// main pool
	private static final int TASK_COUNT_MAIN    = 10000;
	// short pool
	private static final int THREAD_COUNT_SHORT = 1;
	private static final int TASK_COUNT_SHORT   = 10000;
	// long pool
	private static final int THREAD_COUNT_LONG  = 20;
	private static final int TASK_COUNT_LONG    = 10000;

	@SuppressWarnings("unused")
	private static final Keeper keeper = Keeper.get();



	public xThreadPoolFiringTest() {
	}



	// main pool
	@Test (timeout=30000)
	public void testPool1_Main() {
		xLogTest.testStart(TEST_NAME_MAIN);
		new xThreadQueuer(
				xThreadPool.getMainPool(),
				TASK_COUNT_MAIN
		);
		xLogTest.testPassed(TEST_NAME_MAIN);
	}
	// short pool
	@Test (timeout=30000)
	public void testPool2_Short() {
		xLogTest.testStart(TEST_NAME_SHORT);
		new xThreadQueuer(
				xThreadPool.get("short", THREAD_COUNT_SHORT),
				TASK_COUNT_SHORT
		);
		xLogTest.testPassed(TEST_NAME_SHORT);
	}
	// long pool
	@Test (timeout=30000)
	public void testPool3_Long() {
		xLogTest.testStart(TEST_NAME_LONG);
		new xThreadQueuer(
				xThreadPool.get("long", THREAD_COUNT_LONG),
				TASK_COUNT_LONG
		);
		xLogTest.testPassed(TEST_NAME_LONG);
	}



}
