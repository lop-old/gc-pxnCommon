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
				str.peekFirstPart('-')
			);
			assertEquals(
				"abc",
				str.peekFirstPart("-")
			);
		}
		{
			final StringRef str = StringRef.get("abc-+=def");
			assertEquals(
				"abc",
				str.cutFirstPart("-", "-+=")
			);
			assertEquals("def", str.value);
		}
	}



}
