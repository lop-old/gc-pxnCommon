package com.poixson.commonjava.utils.utilsCrypt;

import org.junit.Assert;
import org.junit.Test;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsCrypt;


public class base64Test {

	private static final String TEST_DECODED = "This is the test data! This is the test data!";
	private static final String TEST_ENCODED = "VGhpcyBpcyB0aGUgdGVzdCBkYXRhISBUaGlzIGlzIHRoZSB0ZXN0IGRhdGEh";



	// base64 encode
	@Test
	public void EncodeBase64Test() {
		final String data = utilsCrypt.Base64Encode(TEST_DECODED);
		Assert.assertTrue(utils.notEmpty(data));
		Assert.assertEquals(TEST_ENCODED, data);
	}
	// base64 decode
	@Test
	public void DecodeBase64Test() {
		final String data = utilsCrypt.Base64Decode(TEST_ENCODED);
		Assert.assertTrue(utils.notEmpty(data));
		Assert.assertEquals(TEST_DECODED, data);
	}



}
