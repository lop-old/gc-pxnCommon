package com.poixson.commonjava.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

import com.poixson.commonjava.xLogger.xLog;


public final class utilsString {
	private utilsString() {}



	// object to string
	@SuppressWarnings("boxing")
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
			return "false";
		}
		// int
		if(obj instanceof Integer)
			return ((Integer) obj).toString();
		// long
		if(obj instanceof Long)
			return ((Long) obj).toString();
		// double
		if(obj instanceof Double)
			return ((Double) obj).toString();
		// float
		if(obj instanceof Float)
			return ((Float) obj).toString();
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
		if(utils.isEmpty(a)) return false;
		if(utils.isEmpty(b)) return false;
		return a.equals(b);
	}
	public static boolean strEqualsIgnoreCase(final String a, final String b) {
		if(utils.isEmpty(a)) return false;
		if(utils.isEmpty(b)) return false;
		return a.equalsIgnoreCase(b);
	}



	// trim from string
	public static String trims(final String str, final String...strip) {
		if(utils.isEmpty(str))  return null;
		if(utils.isEmpty(strip)) return null;
		final int stripCount = strip.length;
		final int[] stripLen = new int[stripCount];
		for(int i = 0; i < stripCount; i++)
			stripLen[i] = strip[i].length();
		String out = str;
		boolean changed = true;
		outerloop:
		while(changed) {
			changed = false;
			innerloop:
			for(int index = 0; index < stripCount; index++) {
				if(stripLen[index] == 0) continue innerloop;
				while(out.startsWith(strip[index])) {
					out = out.substring(stripLen[index]);
					changed = true;
				}
				while(out.endsWith(strip[index])) {
					out = out.substring(0, out.length() - stripLen[index]);
					changed = true;
				}
			}
			if(out.length() == 0)
				break outerloop;
		}
		return out;
	}



	// ensure starts with
	public static String ensureStarts(final String start, final String data) {
		if(data == null)
			return null;
		if(data.startsWith(start))
			return data;
		return (new StringBuilder())
			.append(start)
			.append(data)
			.toString();
	}
	// ensure ends with
	public static String ensureEnds(final String end, final String data) {
		if(data == null)
			return null;
		if(data.endsWith(end))
			return data;
		return (new StringBuilder())
			.append(data)
			.append(end)
			.toString();
	}



	// replace with array
	public static String replaceWith(final String replaceWhat, final String[] withWhat, final String data) {
		if(utils.isEmpty(replaceWhat)) throw new NullPointerException("replaceWhat cannot be null");
		if(utils.isEmpty(withWhat))    throw new NullPointerException("withWhat cannot be null");
		if(utils.isEmpty(data)) return null;
		final StringBuilder out = new StringBuilder();
		final int count = withWhat.length;
		int currentPos = 0;
		for(int i = 0; i < count; i++) {
			final int thisPos = data.indexOf("?", currentPos);
			if(thisPos > 0) {
				out.append(data.substring(currentPos, thisPos))
					.append(withWhat[i]);
				currentPos = thisPos + 1;
			}
		}
		if(data.length() > currentPos)
			out.append(data.substring(currentPos));
		return out.toString();
	}



	// repeat string with deliminator
	public static String repeat(final int count, final String str) {
		return repeat(count, str, null);
	}
	public static String repeat(final int count, final String str, final String delim) {
		if(utils.isEmpty(str)) throw new NullPointerException("str cannot be null");
		if(count < 1) return "";
		final StringBuilder out = new StringBuilder();
		// repeat string
		if(utils.isEmpty(delim)) {
			for(int i = 0; i < count; i++)
				out.append(str);
		} else {
			// repeat string with delim
			boolean b = false;
			for(int i = 0; i < count; i++) {
				if(b) out.append(delim);
				b = true;
				out.append(str);
			}
		}
		return out.toString();
	}



	public static String repeat(final int count, final char chr) {
		if(count < 1) return "";
		final StringBuilder out = new StringBuilder();
		// repeat string
		for(int i = 0; i < count; i++)
			out.append(chr);
		return out.toString();
	}



	@SuppressWarnings("boxing")
	public static String center(final String text, final int width, final char padding) {
		if(width < 1) return null;
		if(text == null || text.isEmpty())
			return repeat(width, padding);
		if(width < text.length())
			return text.substring(width);
		final double space = ((double) (width - text.length())) / 2;
		return (new StringBuilder())
			.append(repeat((int) Math.floor(space), padding))
			.append(text)
			.append(repeat((int) Math.ceil(space), padding))
			.toString();
	}



	// generate a random string
	public static String RandomString(final int length) {
		//if(length == 0) return "";
		//if(length <  0) return null;
		if(length < 1) return null;
		final StringBuilder buf = new StringBuilder(length);
		while(buf.length() < length) {
			final String str = UUID.randomUUID().toString();
			if(str == null) throw new NullPointerException();
			buf.append(str);
		}
		return buf.toString().substring( 0, utilsMath.MinMax(length, 0, buf.length()) );
	}



	// add strings with delimiter
	public static String addStrings(final String delim, final String...addThis) {
		return addArray(delim, addThis);
	}
	public static String addArray(final String delim, final String[] addThis) {
		if(utils.isEmpty(addThis)) return null;
		final String dlm = (utils.isEmpty(delim) ? null : delim);
		final StringBuilder str = new StringBuilder();
		boolean b = false;
		for(final String line : addThis) {
			if(utils.isEmpty(line)) continue;
			if(b && dlm != null)
				str.append(dlm);
			str.append(line);
			if(!b && str.length() > 0)
				b = true;
		}
		return str.toString();
	}



	public static String pad(final int width, final String text, final char padding) {
		if(width < 1) return null;
		final int count = width - text.length();
		if(count < 1) return text;
		return (new StringBuilder(width))
			.append(text)
			.append(repeat(count, padding))
			.toString();
	}
	public static String padFront(final int width, final String text, final char padding) {
		if(width < 1) return null;
		final int count = width - text.length();
		if(count < 1) return text;
		return (new StringBuilder(width))
			.append(repeat(count, padding))
			.append(text)
			.toString();
	}
	public static String padCenter(final int width, final String text, final char padding) {
		if(width < 1) return null;
		final double count = (width - text.length()) / 2.0;
		if(Math.ceil(count) < 1.0) return text;
		return (new StringBuilder(width))
			.append(repeat((int) Math.floor(count), padding))
			.append(text)
			.append(repeat((int) Math.ceil(count), padding))
			.toString();
	}



	public static String wildcardToRegex(String wildcard) {
		final StringBuffer str = new StringBuffer(wildcard.length());
		str.append('^');
		final int len = wildcard.length();
		for(int i = 0; i < len; i++) {
			char c = wildcard.charAt(i);
			switch(c) {
			case '*':
				str.append(".*");
				break;
			case '?':
				str.append(".");
				break;
			case '(':
			case ')':
			case '[':
			case ']':
			case '$':
			case '^':
			case '.':
			case '{':
			case '}':
			case '|':
			case '\\':
				str.append("\\");
				str.append(c);
				break;
			default:
				str.append(c);
				break;
			}
		}
		str.append('$');
		return str.toString();
	}



	// logger
	public static xLog log() {
		return xLog.getRoot();
	}



}
