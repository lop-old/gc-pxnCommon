package com.poixson.commonjava.utils;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsDirFile;


public class utilsDirFileTest {



	@Test
	public void testCWD() {
		final String result = utilsDirFile.cwd();
		Assert.assertTrue(utils.notEmpty(result));
	}



	@Test
	public void testListContents() {
		final File[] results = utilsDirFile.listContents(
				new File(utilsDirFile.cwd())
		);
		Assert.assertTrue(utils.notEmpty(results));
	}



	@Test
	public void testBuildFilePath() {
		final String expected = "a/b/c/d.txt";
		{
			final String result = utilsDirFile.buildFilePath(
					"a/b/c",
					"d",
					"txt"
			);
			Assert.assertEquals(expected, result);
		}
		{
			final String result = utilsDirFile.buildFilePath(
					"a/b/c/",
					"d",
					".txt"
			);
			Assert.assertEquals(expected, result);
		}
	}



	@Test
	public void testMergePaths() {
		{
			final String result = utilsDirFile.mergePaths(
					"a/b",
					"c",
					"d.txt"
			);
			Assert.assertEquals("a/b/c/d.txt/", result);
		}
		{
			final String result = utilsDirFile.mergePaths(
					"a/b/c"
			);
			Assert.assertEquals("a/b/c/", result);
		}
		{
			final String result = utilsDirFile.mergePaths(
					"///"
			);
			Assert.assertTrue("Didn't get expected null result!", result == null);
		}
	}



}
