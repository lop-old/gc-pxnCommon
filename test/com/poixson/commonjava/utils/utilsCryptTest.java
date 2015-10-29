package com.poixson.commonjava.utils;

import org.junit.Assert;
import org.junit.Test;

import com.poixson.commonjava.xLogTest;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsCrypt;


public class utilsCryptTest {
	static final String TEST_NAME_ENCODE = "Base64 Encode";
	static final String TEST_NAME_DECODE = "Base64 Decode";

	static final String TEST_DECODED = "This is the test data! This is the test data!";
	static final String TEST_ENCODED = "VGhpcyBpcyB0aGUgdGVzdCBkYXRhISBUaGlzIGlzIHRoZSB0ZXN0IGRhdGEh";



	// base64 encode
	@Test
	public void testEncodeBase64() {
		xLogTest.testStart(TEST_NAME_ENCODE);
		try {
			xLogTest.publish("Input:  "+TEST_DECODED);
			final String data = utilsCrypt.Base64Encode(TEST_DECODED);
			xLogTest.publish("Output: "+data);
			Assert.assertTrue(utils.notEmpty(data));
			Assert.assertEquals(TEST_ENCODED, data);
		} catch (Exception e) {
			xLogTest.trace(e);
			throw new RuntimeException(e);
		} catch (Error e) {
			xLogTest.trace(e);
			throw new RuntimeException(e);
		}
		xLogTest.testPassed(TEST_NAME_ENCODE);
	}
	// base64 decode
	@Test
	public void testDecodeBase64() {
		xLogTest.testStart(TEST_NAME_DECODE);
		try {
			xLogTest.publish("Input:  "+TEST_ENCODED);
			final String data = utilsCrypt.Base64Decode(TEST_ENCODED);
			xLogTest.publish("Output: "+data);
			Assert.assertTrue(utils.notEmpty(data));
			Assert.assertEquals(TEST_DECODED, data);
		} catch (Exception e) {
			xLogTest.trace(e);
			throw new RuntimeException(e);
		} catch (Error e) {
			xLogTest.trace(e);
			throw new RuntimeException(e);
		}
		xLogTest.testPassed(TEST_NAME_DECODE);
	}



}
