package com.poixson.utils;

import org.junit.Test;

import junit.framework.TestCase;


public class StringUtilsTest extends TestCase {



	@Test
	public void testSplitByDelims() {
		final String[] result =
			StringUtils.SplitByDelims(
				"abc+def=ghi+=jkl",
				"=",
				"+="
			);
		final String[] expected =
			new String[] {
				"abc+def",
				"ghi",
				"jkl"
			};
		assertEquals(
			"Number of result in result array",
			expected.length,
			result.length
		);
		for (int i=0; i<expected.length; i++) {
			final String actual = result[i];
			final String expect = expected[i];
			assertEquals(
				expect,
				actual
			);
		}
	}



}

