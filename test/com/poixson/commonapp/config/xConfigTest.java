package com.poixson.commonapp.config;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.poixson.commonjava.xLogger.xLogTest;


public class xConfigTest {



	@Test
	public void testLoad() throws xConfigException {
		final TestFileConfig config = (TestFileConfig) xConfig.Load(
				xLogTest.get(),
				"testresources/",
				"TestFile.yml",
				TestFileConfig.class,
				null
		);
		// test natives
		Assert.assertNotNull("Failed to load test config file!", config);
		Assert.assertEquals("else", config.something);
		Assert.assertEquals(true,   config.bool);
		Assert.assertEquals(11,     config.number);
		Assert.assertEquals("ok",   config.missing);
		// test set
		try {
			final Set<String> dataset = config.getSet(
					String.class,
					"more"
			);
			Assert.assertEquals(3, dataset.size());
			Assert.assertTrue(dataset.contains("A"));
			Assert.assertTrue(dataset.contains("B"));
			Assert.assertTrue(dataset.contains("C"));
		} catch (xConfigException e) {
			throw new RuntimeException(e);
		}
		// test list
		try {
			final List<String> datalist = config.getList(
					String.class,
					"more"
			);
			Assert.assertEquals(3, datalist.size());
			final Iterator<String> it = datalist.iterator();
			Assert.assertEquals("A", it.next());
			Assert.assertEquals("B", it.next());
			Assert.assertEquals("C", it.next());
		} catch (xConfigException e) {
			throw new RuntimeException(e);
		}
		// test map
		try {
			final Map<String, Integer> datamap = config.getMap(
					String.class,
					Integer.class,
					"andMore"
			);
			Assert.assertEquals(3, datamap.size());
			Assert.assertEquals(1, datamap.get("A").intValue());
			Assert.assertEquals(2, datamap.get("B").intValue());
			Assert.assertEquals(3, datamap.get("C").intValue());
		} catch (xConfigException e) {
			throw new RuntimeException(e);
		}
	}



}
