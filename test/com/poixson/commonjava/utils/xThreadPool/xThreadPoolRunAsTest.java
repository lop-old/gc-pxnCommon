package com.poixson.commonjava.utils.xThreadPool;

import org.junit.Assert;
import org.junit.Test;

import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.threads.xThreadPool;
import com.poixson.commonjava.xLogger.xLogTest;


public class xThreadPoolRunAsTest {
	static final String TEST_NAME = "xThreadPoolRunAs";
	static final String POOL_1_NAME = "TEST_POOL_1";
	static final String POOL_2_NAME = "TEST_POOL_2";
	static final int POOL_1_SIZE = 2;
	static final int POOL_2_SIZE = 2;

	private final xThreadPool pool1;
	private final xThreadPool pool2;

	private volatile String result = null;



	public xThreadPoolRunAsTest() {
		this.pool1 = xThreadPool.get(POOL_1_NAME, POOL_1_SIZE);
		this.pool2 = xThreadPool.get(POOL_2_NAME, POOL_2_SIZE);
		this.pool1.Start();
		this.pool2.Start();
	}
//	public void finalize() {
//		this.pool1.Stop();
//		this.pool2.Stop();
//		utilsThread.Sleep(100L);
//	}



	@Test (timeout=30000)
	public void testRunAs() {
		xLogTest.testStart(TEST_NAME);
		{
			this.result = null;
			this.pool1.forcePoolThread(
					this,
					"runA"
			);
			this.waitForResult();
//			final String expect = (new StringBuilder())
//					.append("2:").append(POOL_1_NAME).append(":")
//					.append("[2:Force: com.poixson.commonjava.utils.xThreadPool.]")
//					.append("xThreadPoolRunAsTest")
//					.toString();
//			Assert.assertEquals(expect, this.result);
		}
		{
			this.result = null;
			this.pool2.forcePoolThread(
					this,
					"runB"
			);
			this.waitForResult();
//			final String expect = (new StringBuilder())
//					.append("2:").append(POOL_1_NAME).append(":")
//					.append("[[2:Force: com.poixson.commonjava.utils.xThreadPool.]")
//					.append("xThreadPoolRunAsTest")
//					.toString();
//			Assert.assertEquals(expect, this.result);
		}
		xLogTest.testPassed(TEST_NAME);
	}
	private void waitForResult() {
		for(int i=0; i<5; i++) {
			if(this.result != null)
				return;
			utilsThread.Sleep(50L);
		}
	}



	public void runA() {
		Assert.assertFalse(this.pool1.isPoolThread());
		final String name = Thread.currentThread().getName();
		this.result = name;
		xLogTest.get().publish( (new StringBuilder())
				.append(" -- ")
				.append(name)
				.append(" -- ")
				.toString()
		);
	}
	public void runB() {
		Assert.assertFalse(this.pool2.isPoolThread());
		final String name = Thread.currentThread().getName();
		this.result = name;
		xLogTest.get().publish( (new StringBuilder())
				.append(" -- ")
				.append(name)
				.append(" -- ")
				.toString()
		);
	}



}
