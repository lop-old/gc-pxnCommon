package com.poixson.utils;

import org.junit.Test;

import com.poixson.utils.byref.StringRef;

import junit.framework.TestCase;


public class StringRefTest extends TestCase {



	@Test
	public void testPeekFirstPart() {
		{
			final StringRef str = StringRef.get("abc-efg");
			assertEquals(
				"abc",
				str.PeekFirstPart('-')
			);
			assertEquals(
				"abc",
				str.PeekFirstPart("-")
			);
		}
		{
			final StringRef str = StringRef.get("abc-+=def");
			assertEquals(
				"abc",
				str.CutFirstPart("-", "-+=")
			);
			assertEquals("def", str.value);
		}
	}



}
