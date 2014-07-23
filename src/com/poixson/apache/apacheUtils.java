package com.poixson.apache;

import org.apache.commons.codec.binary.Base64;


public final class apacheUtils {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	private apacheUtils() {}



	// base64 encode
	public static String base64_encode(final String data) {
		if(utils.isEmpty(data)) return null;
		return base64_encode(data.getBytes());
	}
	public static String base64_encode(final byte[] data) {
		if(data == null || data.length == 0) return null;
		return Base64.encodeBase64String(data);
	}
	// base64 decode
	public static String base64_decode(final String data) {
		if(utils.isEmpty(data)) return null;
		return new String(Base64.decodeBase64(data));
	}



}
