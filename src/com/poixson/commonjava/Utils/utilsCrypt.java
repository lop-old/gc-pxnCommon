package com.poixson.commonjava.Utils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.xLogger.xLog;


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
	public static String MD5(final String data) {
		return crypt(CRYPT_MD5, data);
	}
	// sha1
	public static String SHA1(final String data) {
		return crypt(CRYPT_SHA1, data);
	}
	// sha256
	public static String SHA256(final String data) {
		return crypt(CRYPT_SHA256, data);
	}
	// sha512
	public static String SHA512(final String data) {
		return crypt(CRYPT_SHA512, data);
	}


	// perform crypt
	public static String crypt(final String cryptMethod, final String data) {
		try {
			final MessageDigest md = MessageDigest.getInstance(cryptMethod);
			if(md == null) return null;
			md.update(data.getBytes());
			return toHex(md.digest());
		} catch (NoSuchAlgorithmException e) {
			log().trace(e);
		}
		return null;
	}
	// crypt with key
	public static String crypt(final String cryptMethod, final String key, final String data) {
		try {
			final Mac mac = Mac.getInstance(cryptMethod);;
			if(mac == null) return null;
			mac.init(new SecretKeySpec(key.getBytes(), cryptMethod));
			return toHex(
				mac.doFinal(
					data.getBytes()
				)
			);
		} catch (NoSuchAlgorithmException e) {
			log().trace(e);
		} catch (InvalidKeyException e) {
			log().trace(e);
		}
		return null;
	}


	// encrypted data checksum
	public static String hmacMD5(final String key, final String data) {
		return hmac(key, data, Hmac_MD5);
	}
	public static String hmacSHA1(final String key, final String data) {
		return hmac(key, data, Hmac_SHA1);
	}
	public static String hmacSHA256(final String key, final String data) {
		return hmac(key, data, Hmac_SHA256);
	}
	public static String hmac(final String key, final String data, final String cryptMethod) {
		try {
			final Mac mac = Mac.getInstance(cryptMethod);
			if(mac == null) return null;
			mac.init(new SecretKeySpec(key.getBytes(), cryptMethod));
			return toHex(
				mac.doFinal(
					data.getBytes()
				)
			);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return null;
	}


	// base64 encode
	public static String base64_encode(final String data) {
		if(utils.isEmpty(data)) return null;
		return base64_encode(data.getBytes());
	}
	public static String base64_encode(final byte[] data) {
		if(data == null || data.length == 0) return null;
		final BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encodeBuffer(data);
	}
	// base64 decode
	public static String base64_decode(final String data) {
		if(utils.isEmpty(data)) return null;
		try {
			final BASE64Decoder decoder = new BASE64Decoder();
			return new String( decoder.decodeBuffer(data) );
		} catch (IOException ignore) {}
		return null;
	}


	// hex encode
	public static String toHex(final String data) {
		return toHex(data.getBytes());
	}
	public static String toHex(final byte[] data) {
		if(data == null || data.length == 0) return null;
		final StringBuilder str = new StringBuilder(data.length * 2);
		final Formatter formatter = new Formatter(str);
		for(final byte b : data)
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
	public static byte[] fromHex(final String hex) {
		return fromHex(hex.toCharArray());
	}
	public static byte[] fromHex(final char[] hex) {
		if(hex == null || hex.length == 0) return null;
		final int length = hex.length / 2;
		byte[] out = new byte[length];
		for(int i=0; i<length; i++) {
			final int high = Character.digit(hex[i * 2], 16);
			final int low = Character.digit(hex[(i * 2) + 1], 16);
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
