package com.poixson.commonjava.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.xLogger.xLog;


public final class utilsString {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	private utilsString() {}


	// object to string
	public static String toString(Object obj) {
		// null
		if(obj == null)
			return null;
		// string
		if(obj instanceof String)
			return (String) obj;
		// boolean
		if(obj instanceof Boolean) {
			if( ((boolean) obj) == true)
				return "TRUE";
			else
				return "false";
		}
		// int
		if(obj instanceof Integer)
			return Integer.toString((Integer) obj);
		// long
		if(obj instanceof Long)
			return Long.toString((Long) obj);
		// double
		if(obj instanceof Double)
			return Double.toString((Double) obj);
		// float
		if(obj instanceof Float)
			return Float.toString((Float) obj);
		// exception
		if(obj instanceof Exception)
			return ExceptionToString((Exception) obj);
		// unknown object
		return obj.toString();
	}


	// string equals
	public static boolean strEquals(String a, String b) {
		if(a == null || a.isEmpty()) return false;
		if(b == null || b.isEmpty()) return false;
		return a.equals(b);
	}
	public static boolean strEqualsIgnoreCase(String a, String b) {
		if(a == null || a.isEmpty()) return false;
		if(b == null || b.isEmpty()) return false;
		return a.equalsIgnoreCase(b);
	}


	// trim from string
	public static String trim(String str, String data) {
		if(str  == null || str.isEmpty())  return null;
		if(data == null || data.isEmpty()) return null;
		int size = str.length();
		while(data.startsWith(str))
			data = data.substring(size);
		while(data.endsWith(str))
			data = data.substring(0, 0-size);
		return data;
	}


	// replace with array
	public static String replaceWith(String replaceWhat, String[] withWhat, String data) {
		if(replaceWhat == null || replaceWhat.isEmpty()) throw new NullPointerException("replaceWhat cannot be null");
		if(withWhat == null || withWhat.length == 0) return null;
		if(data == null || data.isEmpty()) return null;
		StringBuilder out = new StringBuilder();
		int count = withWhat.length;
		int currentPos = 0;
		for(int i=0; i<count; i++) {
			int thisPos = data.indexOf("?", currentPos);
			if(thisPos > 0) {
				out.append(data.substring(currentPos, thisPos))
					.append(withWhat[i]);
				currentPos = thisPos+1;
			}
		}
		if(data.length() > currentPos)
			out.append(data.substring(currentPos));
		return out.toString();
	}


	// repeat string with deliminator
	public static String repeat(String delim, String str, int repeat) {
		if(delim == null || delim.isEmpty()) {
			StringBuilder out = new StringBuilder();
			for(int i=0; i<repeat; i++)
				out.append(str);
			return out.toString();
		}
		if(str == null || str.isEmpty()) return null;
		if(repeat < 1) return null;
		StringBuilder out = new StringBuilder();
		for(int i=0; i<repeat; i++) {
			if(out.length() > 0)
				out.append(delim);
			out.append(str);
		}
		return out.toString();
	}


	// exception to string
	public static String ExceptionToString(Throwable e) {
		if(e == null) return null;
		StringWriter writer = new StringWriter(256);
		e.printStackTrace(new PrintWriter(writer));
		return writer.toString().trim();
	}


	// md5
	public static String MD5(String str) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			log().trace(e);
		}
		md.update(str.getBytes());
		byte[] byteData = md.digest();
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			String hex = Integer.toHexString(0xFF & byteData[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}


	// generate a random string
	public static String RandomString(int length) {
		if(length == 0) return "";
		if(length <  0) return null;
		String str = "";
		while(str.length() < length) {
			String s = UUID.randomUUID().toString();
			if(s == null) throw new NullPointerException();
			str += s;
		}
		return str.substring( 0, utilsMath.MinMax(length, 0, str.length()) );
	}


	// add strings with delimiter
//	public static String add(String baseString, String addThis, String delim) {
//		if(addThis.isEmpty())    return baseString;
//		if(baseString.isEmpty()) return addThis;
//		return baseString + delim + addThis;
//	}
	public static String add(String delim, String...addThis) {
		return addArray(null, addThis, delim);
	}
	public static String addList(String baseString, List<String> addThis, String delim) {
		return addArray(baseString, (String[]) addThis.toArray(new String[0]), delim);
	}
	public static String addArray(String baseString, String[] addThis, String delim) {
		if(baseString == null) baseString = "";
		if(delim == null || delim.isEmpty()) delim = null;
		StringBuilder string = new StringBuilder(baseString);
		for(String line : addThis) {
			if(line == null || line.isEmpty()) continue;
			if(string.length() != 0 && delim != null)
				string.append(delim);
			string.append(line);
		}
		return string.toString();
	}


	// logger
	public static xLog log() {
		return xVars.log();
	}


}
