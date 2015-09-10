package com.poixson.commonjava.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.UUID;

import com.poixson.commonjava.Utils.byRef.StringRef;
import com.poixson.commonjava.xLogger.xLog;


public final class utilsString {



	public static void init() {
		Keeper.add(new utilsString());
	}
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



	public static String FormatMessage(final String format, final Object... args) {
		String msg = format;
		for(final Object obj : args) {
			msg = msg.replaceFirst(
					"\\{\\}",
					toString(obj)
			);
		}
		return msg;
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



	public static String remove(final String str, final String...strip) {
		if(utils.isEmpty(strip)) return str;
		String dat = str;
		for(final String s : strip)
			dat = dat.replace(s, "");
		return dat;
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



	public static String ensureUnique(final String match, final Set<String> existing) {
		if(utils.isEmpty(match)) throw new NullPointerException("match argument is required!");
		if(existing == null)     throw new NullPointerException("existing argument is required!");
		// already unique
		if(existing.isEmpty() || !existing.contains(match))
			return match;
		int i = 0;
		while(true) {
			i++;
			final String dat = match+"_"+Integer.toString(i);
			if(!existing.contains(dat))
				return dat;
		}
	}



	// get first part
	public static String getFirstPart(final String delim, final String data) {
		final int pos = data.indexOf(delim);
		if(pos == -1)
			return data;
		return data.substring(0, pos);
	}
	// get next part
	public static String getNextPart(final String delim, final StringRef data) {
		final int pos = data.value.indexOf(delim);
		final String part;
		if(pos == -1) {
			part       = data.value;
			data.value = "";
		} else {
			part       = data.value.substring(0, pos);
			data.value = data.value.substring(pos + delim.length());
		}
		return part;
	}
	// get last part
	public static String getLastPart(final String delim, final String data) {
		final int pos = data.lastIndexOf(delim);
		if(pos == -1)
			return data;
		return data.substring(pos + delim.length());
	}



	// get first part
	public static String getFirstPart(final char[] delims, final String data) {
		int pos = Integer.MAX_VALUE;
		for(final char c : delims) {
			final int p = data.indexOf(c);
			if(p == -1)
				continue;
			if(pos > p) pos = p;
			if(pos == 0)
				break;
		}
		if(pos == Integer.MAX_VALUE)
			return data;
		return data.substring(0, pos);
	}
	// get next part
	public static String getNextPart(final char[] delims, final StringRef data) {
		int pos = Integer.MAX_VALUE;
		for(final char c : delims) {
			final int p = data.value.indexOf(c);
			if(p == -1)
				continue;
			if(pos > p) pos = p;
			if(pos == 0)
				break;
		}
		final String part;
		if(pos == Integer.MAX_VALUE) {
			return null;
//			part       = data.value;
//			data.value = "";
		} else {
			part       = data.value.substring(0, pos);
			data.value = data.value.substring(pos + 1);
		}
		return part;
	}
	// get last part
	public static String getLastPart(final char[] delims, final String data) {
		int pos = 0;
		final int len = data.length();
		for(final char c : delims) {
			final int p = data.indexOf(c);
			if(p == -1)
				continue;
			if(pos < p) pos = p;
			if(pos == len)
				break;
		}
		if(pos == len)
			return data;
		return data.substring(pos + 1);
	}



	// replace with array
	public static String replaceWith(final String replaceWhat, final String[] withWhat, final String data) {
		if(utils.isEmpty(replaceWhat)) return data;
		if(utils.isEmpty(withWhat))    return data;
		if(utils.isEmpty(data))        return data;
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
		if(utils.isEmpty(str)) throw new NullPointerException("str argument is required!");
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



	/**
	 * Generate a random string hash.
	 * @param length Number of characters to generate
	 * @return The generated hash string
	 */
	public static String RandomString(final int length) {
		if(length < 1) return null;
		final StringBuilder buf = new StringBuilder(length);
		while(buf.length() < length) {
			final String str = UUID.randomUUID().toString();
			if(str == null) throw new NullPointerException("str argument is required!");
			buf.append(str);
		}
		return buf.toString()
			.substring(
				0,
				utilsNumbers.MinMax(length, 0, buf.length())
			);
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
	public static String padEnd(final int width, final String text, final char padding) {
		if(width < 1) return null;
		final int count = width - text.length();
		if(count < 1) return text;
		return (new StringBuilder(width))
			.append(text)
			.append(repeat(count, padding))
			.toString();
	}
	public static String padCenter(final int width, final String text, final char padding) {
		if(width < 1) return null;
		if(utils.isEmpty(text))
			return repeat(width, padding);
		final double count = ( ((double) width) - ((double) text.length()) ) / 2.0;
		if(Math.ceil(count) < 1.0) return text;
		return (new StringBuilder(width))
			.append(repeat((int) Math.floor(count), padding))
			.append(text)
			.append(repeat((int) Math.ceil(count), padding))
			.toString();
	}



	public static String pad(final int width, final int value) {
		return pad(width, Integer.toString(value), '0');
	}
	public static String padFront(final int width, final int value) {
		return padFront(width, Integer.toString(value), '0');
	}
	public static String padEnd(final int width, final int value) {
		return padEnd(width, Integer.toString(value), '0');
	}
	public static String padCenter(final int width, final int value) {
		return padCenter(width, Integer.toString(value), '0');
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
				str.append('\\');
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
		return utils.log();
	}



}
