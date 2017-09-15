package com.poixson.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.UUID;

import com.poixson.utils.byref.StringRef;
import com.poixson.utils.exceptions.RequiredArgumentException;


public final class StringUtils {
	private StringUtils() {}
	{ Keeper.add(new StringUtils()); }



	// object to string
	public static String toString(final Object obj) {
		// null
		if (obj == null) return null;
		// string
		if (obj instanceof String)
			return (String) obj;
		// boolean
		if (obj instanceof Boolean) {
			return
				((Boolean)obj).booleanValue()
				? "TRUE"
				: "false";
		}
		// int
		if (obj instanceof Integer)
			return ((Integer) obj).toString();
		// long
		if (obj instanceof Long)
			return ((Long) obj).toString();
		// double
		if (obj instanceof Double)
			return ((Double) obj).toString();
		// float
		if (obj instanceof Float)
			return ((Float) obj).toString();
		// exception
		if (obj instanceof Exception)
			return ExceptionToString((Exception) obj);
		// unknown object
		return obj.toString();
	}
	// exception to string
	public static String ExceptionToString(final Throwable e) {
		if (e == null) return null;
		final StringWriter writer = new StringWriter(256);
		e.printStackTrace(new PrintWriter(writer));
		return writer.toString().trim();
	}



	public static boolean isAlpha(final String str) {
		if (str == null) return false;
		int sz = str.length();
		for (int i = 0; i < sz; i++) {
			if ( ! Character.isLetter(str.charAt(i)) ) {
				return false;
			}
		}
		return true;
	}
	public static boolean isAlphaSpace(final String str) {
		if (str == null) return false;
		int sz = str.length();
		for (int i = 0; i < sz; i++) {
			final char chr = str.charAt(i);
			if ( ! Character.isLetter(chr) ) {
				if ( ! Character.isSpaceChar(chr) ) {
					return false;
				}
			}
		}
		return true;
	}
	public static boolean isAlphaNum(final String str) {
		if (str == null) return false;
		int sz = str.length();
		for (int i = 0; i < sz; i++) {
			final char chr = str.charAt(i);
			if ( ! Character.isLetterOrDigit(chr)) {
				return false;
			}
		}
		return true;
	}
	public static boolean isAlphaNumSpace(final String str) {
		if (str == null) return false;
		int sz = str.length();
		for (int i = 0; i < sz; i++) {
			final char chr = str.charAt(i);
			if ( ! Character.isLetterOrDigit(chr)) {
				if ( ! Character.isSpaceChar(chr) ) {
					return false;
				}
			}
		}
		return true;
	}



	public static String FormatMessage(final String format, final Object... args) {
		String msg = format;
		for (final Object obj : args) {
			msg = msg.replaceFirst(
				"\\{\\}",
				toString(obj)
			);
		}
		return msg;
	}



	// string equals
	public static boolean strEquals(final String a, final String b) {
		if (Utils.isEmpty(a)) return false;
		if (Utils.isEmpty(b)) return false;
		return a.equals(b);
	}
	public static boolean strEqualsIgnoreCase(final String a, final String b) {
		if (Utils.isEmpty(a)) return false;
		if (Utils.isEmpty(b)) return false;
		return a.equalsIgnoreCase(b);
	}



	public static String trimToNull(final String str) {
		if (str == null)
			return null;
		if (str.length() == 0)
			return null;
		final String result = str.trim();
		return (
			result.length() == 0
			? null
			: result
		);
	}



	// trim from string
	public static String trims(final String str, final String...strip) {
		if (Utils.isEmpty(str))  return null;
		if (Utils.isEmpty(strip)) return null;
		final int stripCount = strip.length;
		final int[] stripLen = new int[stripCount];
		for (int i = 0; i < stripCount; i++) {
			stripLen[i] = strip[i].length();
		}
		String out = str;
		boolean changed = true;
		outerloop:
		while (changed) {
			changed = false;
			innerloop:
			for (int index = 0; index < stripCount; index++) {
				if (stripLen[index] == 0) continue innerloop;
				while (out.startsWith(strip[index])) {
					out = out.substring(stripLen[index]);
					changed = true;
				}
				while (out.endsWith(strip[index])) {
					out = out.substring(0, out.length() - stripLen[index]);
					changed = true;
				}
			}
			if (out.length() == 0) {
				break outerloop;
			}
		}
		return out;
	}



	public static String removeFromStr(final String str, final String...strip) {
		if (Utils.isEmpty(strip)) return str;
		String dat = str;
		for (final String s : strip) {
			dat = dat.replace(s, "");
		}
		return dat;
	}



	// ensure starts with
	public static String ForceStarts(final String start, final String data) {
		if (data == null) return null;
		if (data.startsWith(start))
			return data;
		return
			(new StringBuilder())
				.append(start)
				.append(data)
				.toString();
	}
	// ensure ends with
	public static String ForceEnds(final String end, final String data) {
		if (data == null) return null;
		if (data.endsWith(end))
			return data;
		return
			(new StringBuilder())
				.append(data)
				.append(end)
				.toString();
	}



	public static String ForceUnique(final String match, final Set<String> existing) {
		if (Utils.isEmpty(match)) throw new RequiredArgumentException("match");
		if (existing == null)     throw new RequiredArgumentException("existing");
		// already unique
		if (existing.isEmpty())        return match;
		if (!existing.contains(match)) return match;
		int i = 0;
		while (true) {
			i++;
			final String dat =
				(new StringBuilder())
					.append(match)
					.append("_")
					.append(i)
					.toString();
			if (!existing.contains(dat)) {
				return dat;
			}
		}
	}



	// get first part
	public static String getFirstPart(final String delim, final String data) {
		final int pos = data.indexOf(delim);
		if (pos == -1) {
			return data;
		}
		return data.substring(0, pos);
	}
	// get next part
	public static String getNextPart(final String delim, final StringRef data) {
		final int pos = data.value.indexOf(delim);
		final String part;
		if (pos == -1) {
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
		if (pos == -1) {
			return data;
		}
		return data.substring(pos + delim.length());
	}



	// get first part
	public static String getFirstPart(final char[] delims, final String data) {
		int pos = Integer.MAX_VALUE;
		for (final char c : delims) {
			final int p = data.indexOf(c);
			if (p == -1) continue;
			if (pos > p) pos = p;
			if (pos == 0) break;
		}
		if (pos == Integer.MAX_VALUE) {
			return data;
		}
		return data.substring(0, pos);
	}
	// get next part
	public static String getNextPart(final char[] delims, final StringRef data) {
		int pos = Integer.MAX_VALUE;
		for (final char c : delims) {
			final int p = data.value.indexOf(c);
			if (p == -1) continue;
			if (pos > p) pos = p;
			if (pos == 0) break;
		}
		final String part;
		if (pos == Integer.MAX_VALUE) {
			return null;
		}
		part       = data.value.substring(0, pos);
		data.value = data.value.substring(pos + 1);
		return part;
	}
	// get last part
	public static String getLastPart(final char[] delims, final String data) {
		int pos = 0;
		final int len = data.length();
		for (final char c : delims) {
			final int p = data.lastIndexOf(c);
			if (p == -1)
				continue;
			if (pos < p) pos = p;
			if (pos == len)
				break;
		}
		if (pos == len) {
			return data;
		}
		return data.substring(pos + 1);
	}



	// replace with array
	public static String replaceWith(final String replaceWhat, final String[] withWhat, final String data) {
		if (Utils.isEmpty(replaceWhat)) return data;
		if (Utils.isEmpty(withWhat))    return data;
		if (Utils.isEmpty(data))        return data;
		final StringBuilder buf = new StringBuilder();
		final int count = withWhat.length;
		int currentPos = 0;
		for (int i = 0; i < count; i++) {
			final int thisPos = data.indexOf("?", currentPos);
			if (thisPos > 0) {
				buf.append(data.substring(currentPos, thisPos));
				buf.append(withWhat[i]);
				currentPos = thisPos + 1;
			}
		}
		if (data.length() > currentPos) {
			buf.append(
				data.substring(currentPos)
			);
		}
		return buf.toString();
	}



	// repeat string with deliminator
	public static String repeat(final int count, final String str) {
		return repeat(count, str, null);
	}
	public static String repeat(final int count, final String str, final String delim) {
		if (Utils.isEmpty(str)) throw new RequiredArgumentException("str");
		if (count < 1) return "";
		final StringBuilder buf = new StringBuilder();
		// repeat string
		if (Utils.isEmpty(delim)) {
			for (int i = 0; i < count; i++) {
				buf.append(str);
			}
		} else {
			// repeat string with delim
			boolean b = false;
			for (int i = 0; i < count; i++) {
				if (b) buf.append(delim);
				b = true;
				buf.append(str);
			}
		}
		return buf.toString();
	}
	public static String repeat(final int count, final char chr) {
		if (count < 1) return "";
		final StringBuilder buf = new StringBuilder();
		// repeat string
		for (int i = 0; i < count; i++) {
			buf.append(chr);
		}
		return buf.toString();
	}



	/**
	 * Generate a random string hash.
	 * @param length Number of characters to generate
	 * @return The generated hash string
	 */
	public static String RandomString(final int length) {
		if (length < 1) return null;
		final StringBuilder buf = new StringBuilder(length);
		while (buf.length() < length) {
			final String str = UUID.randomUUID().toString();
			if (str == null) throw new RequiredArgumentException("str");
			buf.append(str);
		}
		return
			buf.toString()
				.substring(
					0,
					NumberUtils.MinMax(length, 0, buf.length())
				);
	}



	// add strings with delimiter
	public static String addStrings(final String delim, final String...addThis) {
		return addArray(delim, addThis);
	}
	public static String addArray(final String delim, final String[] addThis) {
		if (addThis == null)     return null;
		if (addThis.length == 0) return "";
		final String dlm = (Utils.isEmpty(delim) ? null : delim);
		final StringBuilder buf = new StringBuilder();
		boolean b = false;
		for (final String line : addThis) {
			if (Utils.isEmpty(line)) continue;
			if (b && dlm != null) {
				buf.append(dlm);
			}
			buf.append(line);
			if (!b && buf.length() > 0) {
				b = true;
			}
		}
		return buf.toString();
	}



	public static String pad(final int width, final String text, final char padding) {
		if (width < 1) return null;
		final int count = width - text.length();
		if (count < 1) return text;
		return
			(new StringBuilder(width))
				.append( text                   )
				.append( repeat(count, padding) )
				.toString();
	}
	public static String padFront(final int width, final String text, final char padding) {
		if (width < 1) return null;
		final int count = width - text.length();
		if (count < 1) return text;
		return
			(new StringBuilder(width))
				.append( repeat(count, padding) )
				.append( text                   )
				.toString();
	}
	public static String padEnd(final int width, final String text, final char padding) {
		if (width < 1) return null;
		final int count = width - text.length();
		if (count < 1) return text;
		return
			(new StringBuilder(width))
				.append( text                   )
				.append( repeat(count, padding) )
				.toString();
	}
	public static String padCenter(final int width, final String text, final char padding) {
		if (width < 1) return null;
		if (Utils.isEmpty(text)) {
			return repeat(width, padding);
		}
		final double count = ( ((double) width) - ((double) text.length()) ) / 2.0;
		if (Math.ceil(count) < 1.0) return text;
		return
			(new StringBuilder(width))
				.append( repeat((int) Math.floor(count), padding) )
				.append( text                                     )
				.append( repeat((int) Math.ceil(count), padding)  )
				.toString();
	}



	public static String pad(final int width, final int value) {
		return
			pad(
				width,
				Integer.toString(value),
				'0'
			);
	}
	public static String padFront(final int width, final int value) {
		return
			padFront(
				width,
				Integer.toString(value),
				'0'
			);
	}
	public static String padEnd(final int width, final int value) {
		return
			padEnd(
				width,
				Integer.toString(value),
				'0'
			);
	}
	public static String padCenter(final int width, final int value) {
		return
			padCenter(
				width,
				Integer.toString(value),
				'0'
			);
	}



	public static String wildcardToRegex(final String wildcard) {
		final StringBuffer buf = new StringBuffer(wildcard.length());
		buf.append('^');
		final int len = wildcard.length();
		for (int i = 0; i < len; i++) {
			char c = wildcard.charAt(i);
			switch (c) {
			case '*':
				buf.append(".*");
				break;
			case '?':
				buf.append(".");
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
				buf.append('\\').append(c);
				break;
			default:
				buf.append(c);
				break;
			}
		}
		buf.append('$');
		return buf.toString();
	}



}
