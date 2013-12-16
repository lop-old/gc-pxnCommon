package com.poixson.commonjava.Utils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.xLogger.xLog;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public final class utilsCrypt {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	private utilsCrypt() {}

	public static final String CRYPT_MD5    = "MD5";
	public static final String CRYPT_SHA1   = "SHA1";
	public static final String CRYPT_SHA256 = "SHA256";
	public static final String CRYPT_SHA512 = "SHA512";

	public static final String Hmac_MD5     = "Hmac"+CRYPT_MD5;
	public static final String Hmac_SHA1    = "Hmac"+CRYPT_SHA1;
	public static final String Hmac_SHA256  = "Hmac"+CRYPT_SHA256;


	// md5
	public static String MD5(String data) {
		return crypt(CRYPT_MD5, data);
	}
	// sha1
	public static String SHA1(String data) {
		return crypt(CRYPT_SHA1, data);
	}
	// sha256
	public static String SHA256(String data) {
		return crypt(CRYPT_SHA256, data);
	}
	// sha512
	public static String SHA512(String data) {
		return crypt(CRYPT_SHA512, data);
	}


	// perform crypt
	public static String crypt(String cryptMethod, String data) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(cryptMethod);
		} catch (NoSuchAlgorithmException e) {
			log().trace(e);
		}
		if(md == null) return null;
		md.update(data.getBytes());
		return toHex(md.digest());
	}
	// crypt with key
	public static String crypt(String cryptMethod, String key, String data) {
		Mac mac = null;
		try {
			SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), cryptMethod);
			mac = Mac.getInstance(cryptMethod);;
			mac.init(keySpec);
		} catch (NoSuchAlgorithmException e) {
			log().trace(e);
		} catch (InvalidKeyException e) {
			log().trace(e);
		}
		if(mac == null) return null;
		return toHex(
			mac.doFinal(
				data.getBytes()
			)
		);
	}


	// encrypted data checksum
	public static String hmacMD5(String key, String data) {
		return hmac(key, data, Hmac_MD5);
	}
	public static String hmacSHA1(String key, String data) {
		return hmac(key, data, Hmac_SHA1);
	}
	public static String hmacSHA256(String key, String data) {
		return hmac(key, data, Hmac_SHA256);
	}
	public static String hmac(String key, String data, String cryptMethod) {
		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), cryptMethod);
		Mac mac = null;
		try {
			mac = Mac.getInstance(cryptMethod);
			mac.init(keySpec);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		if(mac == null) return null;
		return toHex(
			mac.doFinal(
				data.getBytes()
			)
		);
	}


	// base64 encode
	public static String base64_encode(String data) {
		if(data == null || data.isEmpty()) return null;
		return base64_encode(data.getBytes());
	}
	public static String base64_encode(byte[] data) {
		if(data == null || data.length == 0) return null;
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encodeBuffer(data);
	}
	// base64 decode
	public static String base64_decode(String data) {
		if(data == null || data.isEmpty()) return null;
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			return new String( decoder.decodeBuffer(data) );
		} catch (IOException ignore) {}
		return null;
	}


	// hex encode
	public static String toHex(String data) {
		return toHex(data.getBytes());
	}
	public static String toHex(byte[] data) {
		if(data == null || data.length == 0) return null;
		StringBuilder str = new StringBuilder(data.length * 2);
		Formatter formatter = new Formatter(str);
		for(byte b : data)
			formatter.format("%02x", b);
		if(formatter != null) {
			try {
				formatter.close();
			} catch (Exception ignore) {}
		}
		return str.toString();
//		byte[] byteData = md.digest();
//		StringBuffer hexString = new StringBuffer();
//		for (int i = 0; i < byteData.length; i++) {
//			String hex = Integer.toHexString(0xFF & byteData[i]);
//			if (hex.length() == 1) {
//				hexString.append('0');
//			}
//			hexString.append(hex);
//		}
//		return hexString.toString();
	}
	// hex decode
	public static byte[] fromHex(String hex) {
		return fromHex(hex.toCharArray());
	}
	public static byte[] fromHex(char[] hex) {
		if(hex == null || hex.length == 0) return null;
		int length = hex.length / 2;
		byte[] out = new byte[length];
		for(int i=0; i<length; i++) {
			int high = Character.digit(hex[i * 2], 16);
			int low = Character.digit(hex[(i * 2) + 1], 16);
			int value = (high << 4) | low;
			if(value > 127)
				value -= 256;
			out[i] = (byte) value;
		}
		return out;
	}


	// logger
	public static xLog log() {
		return xVars.log();
	}


}
