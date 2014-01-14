package com.poixson.commonjava.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
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


	// empty string
	public static boolean isEmpty(final String value) {
		return (value == null || value.length() == 0);
	}
	public static boolean isNotEmpty(final String value) {
		return (value != null && value.length() > 0);
	}


	// object to string
	public static String toString(final Object obj) {
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
	// exception to string
	public static String ExceptionToString(final Throwable e) {
		if(e == null) return null;
		final StringWriter writer = new StringWriter(256);
		e.printStackTrace(new PrintWriter(writer));
		return writer.toString().trim();
	}


	// string equals
	public static boolean strEquals(final String a, final String b) {
		if(isEmpty(a)) return false;
		if(isEmpty(b)) return false;
		return a.equals(b);
	}
	public static boolean strEqualsIgnoreCase(final String a, final String b) {
		if(isEmpty(a)) return false;
		if(isEmpty(b)) return false;
		return a.equalsIgnoreCase(b);
	}


	// trim from string
	public static String trim(final String str, final String data) {
		if(isEmpty(str))  return null;
		if(isEmpty(data)) return null;
		final int size = str.length();
		String out = data;
		while(out.startsWith(str))
			out = out.substring(size);
		while(out.endsWith(str))
			out = out.substring(0, 0-size);
		return out;
	}


	// replace with array
	public static String replaceWith(final String replaceWhat, final String[] withWhat, final String data) {
		if(isEmpty(replaceWhat)) throw new NullPointerException("replaceWhat cannot be null");
		if(withWhat == null || withWhat.length == 0) return null;
		if(isEmpty(data)) return null;
		final StringBuilder out = new StringBuilder();
		final int count = withWhat.length;
		int currentPos = 0;
		for(int i=0; i<count; i++) {
			final int thisPos = data.indexOf("?", currentPos);
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
	public static String repeat(final String delim, final String str, final int repeat) {
		if(isEmpty(delim)) {
			final StringBuilder out = new StringBuilder();
			for(int i=0; i<repeat; i++)
				out.append(str);
			return out.toString();
		}
		if(isEmpty(str)) return null;
		if(repeat < 1) return null;
		final StringBuilder out = new StringBuilder();
		for(int i=0; i<repeat; i++) {
			if(out.length() > 0)
				out.append(delim);
			out.append(str);
		}
		return out.toString();
	}


	// generate a random string
	public static String RandomString(final int length) {
		if(length == 0) return "";
		if(length <  0) return null;
		final StringBuilder buf = new StringBuilder();
		while(buf.length() < length) {
			final String str = UUID.randomUUID().toString();
			if(str == null) throw new NullPointerException();
			buf.append(str);
		}
		return buf.toString().substring( 0, utilsMath.MinMax(length, 0, buf.length()) );
	}


	// add strings with delimiter
//	public static String add(final String baseString, final String addThis, final String delim) {
//		if(addThis.isEmpty())    return baseString;
//		if(baseString.isEmpty()) return addThis;
//		return baseString + delim + addThis;
//	}
	public static String add(final String delim, final String...addThis) {
		return addArray(null, addThis, delim);
	}
	public static String addList(final String baseString, final List<String> addThis, final String delim) {
		return addArray(baseString, (String[]) addThis.toArray(new String[0]), delim);
	}
	public static String addArray(final String baseString, final String[] addThis, final String delim) {
		final StringBuilder str = new StringBuilder(
			baseString == null ? null : baseString
		);
		if(addThis == null || addThis.length == 0) return str.toString();
		final String d = (isEmpty(delim) ? null : delim);
		for(final String line : addThis) {
			if(isEmpty(line)) continue;
			if(str.length() > 0 && delim != null)
				str.append(delim);
			str.append(line);
		}
		return str.toString();
	}


	// logger
	public static xLog log() {
		return xVars.log();
	}


}
