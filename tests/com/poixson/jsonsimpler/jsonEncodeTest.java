/*
package com.poixson.jsonsimpler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.poixson.commonjava.xLogTest;
import com.poixson.commonjava.Utils.byRef.IntRef;
import com.poixson.commonjava.xLogger.xLog;


public class jsonEncodeTest {
	static final boolean DEBUG = true;

	static final String TEST_WRITE_STRING  = "jsonSimpler encode WriteString";
	static final String TEST_NAME_MINIFIED = "jsonSimpler encode Minified";
	static final String TEST_NAME_PRETTY   = "jsonSimpler encode Pretty";
	static final String QQ = "\"";
	static final String NL = "\n";



	@Test
	public void testWriteString() {
		xLogTest.testStart(TEST_WRITE_STRING);
		try {
			// primitives
			doTest("null",    false, null,  "null");
			doTest("String",  false, "abc", QQ+"abc"+QQ);
			doTest("double",  false, 1.1d,   "1.1");
			doTest("float",   false, 2.2f,  "2.2");
			doTest("int",     false, 0,     "0");
			doTest("int",     false, 3,     "3");
			doTest("long",    false, 4L,    "4");
			doTest("boolean", false, true,  "true");
			doTest("boolean", false, false, "false");
			doTest("char",    false, 'a',    "a");
			// List
			doTest(
					"List",
					false,
					Arrays.asList(2, 4),
					"[2,4]"
			);
			// Set
			{
				final Set<Boolean> set = new HashSet<Boolean>();
				set.add(Boolean.TRUE);
				set.add(Boolean.FALSE);
				doTest(
						"Set",
						false,
						set,
						"[true,false]",
						"[false,true]"
				);
			}
			// Map
			{
				final Map<String, Object> map = new HashMap<String, Object>();
				map.put("abc", 123);
				map.put("def", 456);
				doTest(
						"Map",
						false,
						map,
						"{abc:123,def:456}",
						"{def:456,abc:123}"
				);
			}
			// byte[]
			doTest(
					"byte[]",
					false,
					new byte[]{ 2, 4 },
					"[2,4]"
			);
			// short[]
			doTest(
					"short[]",
					false,
					new short[]{ 2, 4 },
					"[2,4]"
			);
			// int[]
			doTest(
					"int[]",
					false,
					new int[]{ 2, 4 },
					"[2,4]"
			);
			// long[]
			doTest(
					"long[]",
					false,
					new long[]{ 2L, 4L },
					"[2,4]"
			);
			// double[]
			doTest(
					"double[]",
					false,
					new double[]{ 2.2, 4.4 },
					"[2.2,4.4]"
			);
			// float[]
			doTest(
					"float[]",
					false,
					new float[]{ 2.2F, 4.4F },
					"[2.2,4.4]"
			);
			// boolean[]
			doTest(
					"boolean[]",
					false,
					new boolean[]{ true, false },
					"[true,false]"
			);
			// char[]
			doTest(
					"char[]",
					false,
					new char[]{ 'a', 'b' },
					"[a,b]"
			);
			// object[]
			{
				final Object a = new Object();
				final Object b = new Object();
				doTest(
						"Object[]",
						false,
						new Object[]{ a, b },
						'['+a.toString()+','+b.toString()+']'
				);
			}
		} catch (Exception e) {
			xLogTest.trace(e);
			throw new RuntimeException(e);
		} catch (Error e) {
			xLogTest.trace(e);
			throw new RuntimeException(e);
		}
		xLogTest.testPassed(TEST_WRITE_STRING);
	}



	@Test
	public void testMinifiedPretty() {
		try {
			final List<Object> array = new ArrayList<Object>();
			array.add("abc\u0010a/");
				final jsonArray subarray = new jsonArray();
				subarray.add("def");
				subarray.add(456);
			array.add(subarray);
			array.add(new Integer(123));
			array.add(new Double(222.123));
			array.add(new Boolean(true));
			final jsonMap map = new jsonMap();
			map.put("array", array);
			map.put("str", "Abcd");
			map.put("str", "1234");
			// test minified
			{
				final String testArray = "[\"abc"+'\u0010'+"a/\",[\"def\",456],123,222.123,true]";
				xLogTest.testStart(TEST_NAME_MINIFIED);
				doTest(
						"minified array",
						false,
						array,
						testArray
				);
				doTest(
						"minified map",
						false,
						map,
						"{array:"+testArray+",str:\"1234\"}",
						"{str:\"1234\",array:"+testArray+"}"
				);
				xLogTest.testPassed(TEST_NAME_MINIFIED);
			}
			// test pretty
			{
				xLogTest.testStart(TEST_NAME_PRETTY);
				final String NL = JSON.NEWLINE;
				final String IN = JSON.INDENT;
				// test pretty array
				doTest(
						"pretty array",
						true,
						array,
						"["+NL+
							IN+"\"abc"+'\u0010'+"a/\","+NL+
							IN+"["+NL+
								IN+IN+"\"def\","+NL+
								IN+IN+"456"+NL+
							IN+"],"+NL+
							IN+"123,"+NL+
							IN+"222.123,"+NL+
							IN+"true"+NL+
						"]"
				);
				// test pretty map
				final String testArray =
						IN+IN+"\"abc"+'\u0010'+"a/\","+NL+
						IN+IN+"["+NL+
							IN+IN+IN+"\"def\","+NL+
							IN+IN+IN+"456"+NL+
						IN+IN+"],"+NL+
						IN+IN+"123,"+NL+
						IN+IN+"222.123,"+NL+
						IN+IN+"true"+NL;
				doTest(
						"pretty map",
						true,
						map,
						"{"+NL+
							IN+"array: ["+NL+
								testArray+
							IN+"],"+
							IN+"str: \"1234\""+NL+
						"}",
						"{"+NL+
							IN+"str: \"1234\","+NL+
							IN+"array: ["+NL+
								testArray+
							IN+"]"+NL+
						"}"
				);
				xLogTest.testPassed(TEST_NAME_PRETTY);
			}
		} catch (Exception e) {
			xLogTest.trace(e);
			throw new RuntimeException(e);
		} catch (Error e) {
			xLogTest.trace(e);
			throw new RuntimeException(e);
		}
	}



	static void doTest(final String name, final boolean pretty,
			final Object value, final String... expected) {
		if(DEBUG) {
			xLog.getRoot().publish();
			xLogTest.publish("Testing: "+name);
		}
		final String result = JSON.ToString(
				value,
				(pretty ? new IntRef(0) : null),
				true
		);
		if(DEBUG)
			xLogTest.publish("Result: "+result);
		Assert.assertNotNull(result);
		for(final String expect : expected) {
			if(DEBUG)
				xLogTest.publish("Expect: "+expect);
			if(result.equals(expect))
				return;
		}
		xLogTest.publish("Failed to match result with expected!");
		Assert.assertTrue(false);
	}



/ *
	static void doTestEncodeArray(final boolean pretty, final String... expected) {
	static void doTestEncodeMap(final boolean pretty, final String... expected) {
		final jsonArray array = new jsonArray();
		array.add("abc\u0010a/");
		final jsonArray subarray = new jsonArray();
		subarray.add("def");
		subarray.add(456);
		array.add(subarray);
		array.add(new Integer(123));
		array.add(new Double(222.123));
		array.add(new Boolean(true));
//		final jsonMap map = new jsonMap();
//		obj.put("array", arr);
//		obj.put("str", "Abcd");
//		obj.put("str", "1234");
		// test array
		{
			xLogTest.publish("Test "+(pretty ? "pretty" : "minified")+" jsonArray: ");
			final String result = array.toString();
			xLogTest.publish("Expected: "+expectedArray);
			xLogTest.publish("Result:   "+result);
			Assert.assertEquals(expectedArray, result);
		}
		// test map
		{
			xLogTest.publish("Test "+(pretty ? "pretty" : "minified")+" jsonMap: ");
			final String result = array.toString();
			xLogTest.publish("Expected: "+expectedArray);
			xLogTest.publish("Result:   "+result);
			Assert.assertEquals(expectedArray, result);
		}
	}
	static void doTestDecode() {
	}
* /



}
*/