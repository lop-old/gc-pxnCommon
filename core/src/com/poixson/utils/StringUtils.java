package com.poixson.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.tools.Keeper;


public class StringUtils extends StringAdvUtils {
	StringUtils() {}
	{ Keeper.add(new StringUtils()); }



	public static final Charset CHARSET_UTF8  = StandardCharsets.UTF_8;
	public static final Charset CHARSET_ASCII = StandardCharsets.US_ASCII;
	public static final Charset DEFAULT_CHARSET = CHARSET_UTF8;



	// ------------------------------------------------------------------------------- //
	// convert string



	// decode string
	public static String decode(final String raw) {
		return decode(
			raw,
			null,
			null
		);
	}
	public static String decodeDef(final String raw, final String defaultStr) {
		return
			decode(
				raw,
				defaultStr,
				null
			);
	}
	public static String decodeCh(final String raw, final String charset) {
		return
			decode(
				raw,
				null,
				charset
			);
	}
	public static String decode(final String raw, final String defaultStr, final String charset) {
		if (charset == null) {
			return
				decode(
					raw,
					defaultStr,
					DEFAULT_CHARSET.name()
				);
		}
		try {
			return URLDecoder.decode(raw, charset);
		} catch (UnsupportedEncodingException ignore) {}
		return defaultStr;
	}



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



	// ------------------------------------------------------------------------------- //
	// check value



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



	// string equals
	public static boolean StrEqualsExact(final String a, final String b) {
		if (a == null && b == null)
			return true;
		if (a == null || b == null)
			return false;
		return a.equals(b);
	}
	public static boolean StrEqualsExactIgnoreCase(final String a, final String b) {
		if (a == null && b == null)
			return true;
		if (a == null || b == null)
			return false;
		return a.equalsIgnoreCase(b);
	}
	public static boolean StrEquals(final String a, final String b) {
		final boolean aEmpty = Utils.isEmpty(a);
		final boolean bEmpty = Utils.isEmpty(b);
		if (aEmpty && bEmpty) return true;
		if (aEmpty || bEmpty) return false;
		return a.equals(b);
	}
	public static boolean StrEqualsIgnoreCase(final String a, final String b) {
		final boolean aEmpty = Utils.isEmpty(a);
		final boolean bEmpty = Utils.isEmpty(b);
		if (aEmpty && bEmpty) return true;
		if (aEmpty || bEmpty) return false;
		return a.equalsIgnoreCase(b);
	}



	// ------------------------------------------------------------------------------- //
	// trim



	public static String TrimToNull(final String str, final String...strip) {
		final String result =
			doTrim(
				true, true,
				str,
				strip
			);
		return (
			Utils.isEmpty(result)
			? null
			: result
		);
	}



	// trim from string
	public static String Trim(final String str, final char...strip) {
		return
			doTrim(
				true, true,
				str,
				strip
			);
	}
	public static String TrimFront(final String str, final char...strip) {
		return
			doTrim(
				true, false,
				str,
				strip
			);
	}
	public static String TrimEnd(final String str, final char...strip) {
		return
			doTrim(
				false, true,
				str,
				strip
			);
	}



	public static String Trim(final String str, final String...strip) {
		return
			doTrim(
				true, true,
				str,
				strip
			);
	}
	public static String TrimFront(final String str, final String...strip) {
		return
			doTrim(
				true, false,
				str,
				strip
			);
	}
	public static String TrimEnd(final String str, final String...strip) {
		return
			doTrim(
				false, true,
				str,
				strip
			);
	}



//TODO: this could use further optimizations
//track trim length rather than modifying the string every loop
	private static String doTrim(
			final boolean trimFront, final boolean trimEnd,
			final String str, final char...strip) {
		if (!trimFront && !trimEnd) return str;
		if (Utils.isEmpty(str))     return str;
		if (Utils.isEmpty(strip))   return str;
		final int stripCount = strip.length;
		String out = str;
		int size = str.length();
		boolean changed = true;
		while (changed) {
			changed = false;
			for (int index = 0; index < stripCount; index++) {
				if (trimFront) {
					while (out.charAt(0) == strip[index]) {
						out = out.substring(1);
						size--;
						changed = true;
					}
				}
				if (trimEnd) {
					while (out.charAt(size - 1) == strip[index]) {
						size--;
						out = out.substring(0, size);
						changed = true;
					}
				}
			}
			if (size <= 0)
				break;
		}
		return out;
	}
	private static String doTrim(
			final boolean trimFront, final boolean trimEnd,
			final String str, final String...strip) {
		if (!trimFront && !trimEnd) return str;
		if (Utils.isEmpty(str))     return str;
		if (Utils.isEmpty(strip))   return str;
		final int stripCount = strip.length;
		final int[] stripLen = new int[stripCount];
		for (int i = 0; i < stripCount; i++) {
			stripLen[i] = strip[i].length();
		}
		String out = str;
		boolean changed = true;
		OUTER_LOOP:
		while (changed) {
			changed = false;
			INNER_LOOP:
			for (int index = 0; index < stripCount; index++) {
				if (stripLen[index] == 0) continue INNER_LOOP;
				if (trimFront) {
					while (out.startsWith(strip[index])) {
						out = out.substring(stripLen[index]);
						changed = true;
					}
				}
				if (trimEnd) {
					while (out.endsWith(strip[index])) {
						out = out.substring(0, out.length() - stripLen[index]);
						changed = true;
					}
				}
			}
			if (out.length() == 0) {
				break OUTER_LOOP;
			}
		}
		return out;
	}



	// ------------------------------------------------------------------------------- //
	// modify string



	public static String RemoveFromStr(final String str, final String...strip) {
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



	// ------------------------------------------------------------------------------- //
	// find position



	// index of (many delims)
	public static int IndexOf(final String string, final int fromIndex, final char...delims) {
		if (Utils.isEmpty(string))
			return -1;
		int pos = Integer.MAX_VALUE;
		for (final char delim : delims) {
			final int p = string.indexOf(delim, fromIndex);
			// delim not found
			if (p == -1) continue;
			// earlier delim
			if (p < pos) {
				pos = p;
				if (p == 0)
					return 0;
			}
		}
		return (
			pos == Integer.MAX_VALUE
			? -1
			: pos
		);
	}
	public static int IndexOf(final String string, final char...delims) {
		return IndexOf(string, 0, delims);
	}
	public static int LastIndexOf(final String string, final int fromIndex, final char...delims) {
//TODO:
		throw new UnsupportedOperationException("UNFINISHED");
	}
	public static int LastIndexOf(final String string, final char...delims) {
		return LastIndexOf(string, 0, delims);
	}



	public static int IndexOf(final String string, final int fromIndex, final String...delims) {
		if (Utils.isEmpty(string))
			return -1;
		int pos = Integer.MAX_VALUE;
		for (final String delim : delims) {
			if (Utils.isEmpty(delim)) continue;
			final int p = string.indexOf(delim, fromIndex);
			// delim not found
			if (p == -1) continue;
			// earlier delim
			if (p < pos) {
				pos = p;
				if (p == 0)
					return 0;
			}
		}
		return (
			pos == Integer.MAX_VALUE
			? -1
			: pos
		);
	}
	public static int IndexOf(final String string, final String...delims) {
		return IndexOf(string, 0, delims);
	}
	public static int LastIndexOf(final String string, final int fromIndex, final String...delims) {
//TODO:
		throw new UnsupportedOperationException("UNFINISHED");
	}
	public static int LastIndexOf(final String string, final String...delims) {
		return LastIndexOf(string, 0, delims);
	}



	// last index of (many delims)
	public static int IndexOfLast(final String string, final char...delims) {
		if (Utils.isEmpty(string))
			return -1;
		int pos = Integer.MIN_VALUE;
		for (final char delim : delims) {
			final int p = string.lastIndexOf(delim);
			// delim not found
			if (p == -1) continue;
			// later delim
			if (p > pos) {
				pos = p;
			}
		}
		return (
			pos == Integer.MIN_VALUE
			? -1
			: pos
		);
	}
	public static int IndexOfLast(final String string, final String...delims) {
		if (Utils.isEmpty(string))
			return -1;
		int pos = Integer.MIN_VALUE;
		for (final String delim : delims) {
			if (Utils.isEmpty(delim)) continue;
			final int p = string.lastIndexOf(delim);
			// delim not found
			if (p == -1) continue;
			// later delim
			if (p > pos) {
				pos = p;
			}
		}
		return (
			pos == Integer.MIN_VALUE
			? -1
			: pos
		);
	}


	// get first part (many delims)
	public static String PeekFirstPart(final String string, final char...delims) {
		if (Utils.isEmpty(string))
			return string;
		int pos = Integer.MAX_VALUE;
		// find earliest delim
		for (final char delim : delims) {
			final int p = string.indexOf(delim);
			// delim not found
			if (p == -1) continue;
			// earlier delim
			if (p < pos) {
				pos = p;
				if (p == 0) break;
			}
		}
		return (
			pos == Integer.MAX_VALUE
			? string
			: string.substring(0, pos)
		);
	}
	public static String PeekFirstPart(final String string, final String...delims) {
		if (Utils.isEmpty(string))
			return string;
		int pos = Integer.MAX_VALUE;
		// find earliest delim
		for (final String delim : delims) {
			if (Utils.isEmpty(delim)) continue;
			final int p = string.indexOf(delim);
			// delim not found
			if (p == -1) continue;
			// earlier delim
			if (p < pos) {
				pos = p;
				if (p == 0) break;
			}
		}
		return (
			pos == Integer.MAX_VALUE
			? string
			: string.substring(0, pos)
		);
	}



	// get last part (many delims)
	public static String PeekLastPart(final String string, final char...delims) {
		if (Utils.isEmpty(string))
			return string;
		int pos = Integer.MIN_VALUE;
		// find latest delim
		for (final char delim : delims) {
			final int p = string.lastIndexOf(delim);
			// delim not found
			if (p == -1) continue;
			// later delim
			if (p > pos) {
				pos = p;
			}
		}
		return (
			pos == Integer.MIN_VALUE
			? string
			: string.substring(pos + 1)
		);
	}
	public static String PeekLastPart(final String string, final String...delims) {
		if (Utils.isEmpty(string))
			return string;
		int pos = Integer.MIN_VALUE;
		int delimSize = 0;
		// find latest delim
		for (final String delim : delims) {
			if (Utils.isEmpty(delim)) continue;
			final int p = string.lastIndexOf(delim);
			// delim not found
			if (p == -1) continue;
			// longer delim
			if (p == pos) {
				if (delim.length() > delimSize) {
					delimSize = delim.length();
				}
				continue;
			}
			// later delim
			if (p+delimSize > pos+delim.length()) {
				pos = p;
				delimSize = delim.length();
			}
		}
		return (
			pos == Integer.MIN_VALUE
			? string
			: string.substring(pos + delimSize)
		);
	}



	// find longest line
	public static int FindLongestLine(final String[] lines) {
		if (Utils.isEmpty(lines))
			return -1;
		int len = 0;
		for (final String line : lines) {
			if (line == null) continue;
			if (line.length() > len)
				len = line.length();
		}
		return len;
	}



	// ------------------------------------------------------------------------------- //
	// replace within string



	// replace range
	public static String ReplaceStringRange(
			final String str, final String chunk,
			final int startPos, final int endPos) {
		if (str == null) throw new RequiredArgumentException("str");
		if (str.length() == 0)
			return chunk;
		final StringBuilder result = new StringBuilder();
		if (startPos > 0) {
			result.append(
				str.substring(0, startPos)
			);
		}
		if (Utils.notEmpty(chunk)) {
			result.append(chunk);
		}
		if (endPos < str.length()) {
			result.append(
				str.substring(endPos)
			);
		}
		return result.toString();
	}



	// replace with array
	public static String ReplaceWith(final String replaceWhat, final String[] withWhat, final String data) {
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



	// ------------------------------------------------------------------------------- //
	// generate string



	// repeat string with deliminator
	public static String Repeat(final int count, final String str) {
		return Repeat(count, str, null);
	}
	public static String Repeat(final int count, final String str, final String delim) {
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
	public static String Repeat(final int count, final char chr) {
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



	// generate regex from string with wildcard *
	public static String WildcardToRegex(final String wildcard) {
		final StringBuilder buf = new StringBuilder(wildcard.length());
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



	// ------------------------------------------------------------------------------- //
	// build string



	// add strings with delimiter
	public static String MergeStrings(final String delim, final String... addThis) {
		if (Utils.isEmpty(addThis)) throw new RequiredArgumentException("addThis");
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
	public static String MergeStrings(final char delim, final String... addThis) {
		if (Utils.isEmpty(addThis)) throw new RequiredArgumentException("addThis");
		final StringBuilder buf = new StringBuilder();
		boolean first = true;
		for (final String line : addThis) {
			if (Utils.isEmpty(line)) continue;
			if (!first)
				buf.append(delim);
			buf.append(line);
			if (first) {
				if (buf.length() > 0)
					first = false;
			}
		}
		return buf.toString();
	}



	// add objects to string with delimiter
	public static String MergeObjects(final String delim, final Object... addThis) {
		if (Utils.isEmpty(addThis)) throw new RequiredArgumentException("addThis");
		String[] addStrings = new String[ addThis.length ];
		int index = 0;
		for (final Object obj : addThis) {
			addStrings[index] = toString(obj);
			index++;
		}
		return MergeStrings(delim, addStrings);
	}
	public static String MergeObjects(final char delim, final Object... addThis) {
		if (Utils.isEmpty(addThis)) throw new RequiredArgumentException("addThis");
		String[] addStrings = new String[ addThis.length ];
		int index = 0;
		for (final Object obj : addThis) {
			addStrings[index] = toString(obj);
			index++;
		}
		return MergeStrings(delim, addStrings);
	}



	// ------------------------------------------------------------------------------- //
	// pad string



	public static String Pad(final int width, final String text, final char padding) {
		if (width < 1) return null;
		final int count = width - text.length();
		if (count < 1) return text;
		return
			(new StringBuilder(width))
				.append( text                   )
				.append( Repeat(count, padding) )
				.toString();
	}
	public static String PadFront(final int width, final String text, final char padding) {
		if (width < 1) return null;
		final int count = width - text.length();
		if (count < 1) return text;
		return
			(new StringBuilder(width))
				.append( Repeat(count, padding) )
				.append( text                   )
				.toString();
	}
	public static String PadEnd(final int width, final String text, final char padding) {
		if (width < 1) return null;
		final int count = width - text.length();
		if (count < 1) return text;
		return
			(new StringBuilder(width))
				.append( text                   )
				.append( Repeat(count, padding) )
				.toString();
	}
	public static String PadCenter(final int width, final String text, final char padding) {
		if (width < 1) return null;
		if (Utils.isEmpty(text)) {
			return Repeat(width, padding);
		}
		final double count = ( ((double) width) - ((double) text.length()) ) / 2.0;
		if (Math.ceil(count) < 1.0) return text;
		return
			(new StringBuilder(width))
				.append( Repeat((int) Math.floor(count), padding) )
				.append( text                                     )
				.append( Repeat((int) Math.ceil(count), padding)  )
				.toString();
	}



	public static String Pad(final int width, final int value) {
		return Pad(       width, Integer.toString(value), '0' );
	}
	public static String PadFront(final int width, final int value) {
		return PadFront(  width, Integer.toString(value), '0' );
	}
	public static String PadEnd(final int width, final int value) {
		return PadEnd(    width, Integer.toString(value), '0' );
	}
	public static String PadCenter(final int width, final int value) {
		return PadCenter( width, Integer.toString(value), '0' );
	}



}
