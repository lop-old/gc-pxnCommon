package com.poixson.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.tools.byref.StringRef;


class StringAdvUtils {
	StringAdvUtils() {}



	public static String[] BytesToStringArray(final byte[] bytes) {
		if (bytes == null)     return null;
		if (bytes.length == 0) return new String[0];
		final List<String> list = new ArrayList<String>();
		final int bytesSize = bytes.length;
		int last = 0;
		for (int i=0; i<bytesSize; i++) {
			if (bytes[i] == 0) {
				if (i - last <= 0) continue;
				list.add(
					new String(
						bytes,
						last,
						i - last,
						StringUtils.CHARSET_ASCII
					)
				);
				last = i+1;
			}
		}
		if (last+1 < bytesSize) {
			list.add(
				new String(
					bytes,
					last,
					bytesSize,
					StringUtils.CHARSET_ASCII
				)
			);
		}
		return list.toArray(new String[0]);
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



	// strip color tags
	public static String StripColorTags(final String line) {
		if (Utils.isEmpty(line))
			return line;
		final StringBuilder result = new StringBuilder(line);
		boolean changed = false;
		while (true) {
			final int posA = result.indexOf("@|");
			if (posA == -1) break;
			final int posB = result.indexOf(" ", posA);
			final int posC = result.indexOf("|@", posB);
			if (posB == -1) break;
			if (posC == -1) break;
			result.replace(posC, posC+2, "");
			result.replace(posA, posB+1, "");
			changed = true;
		}
		if (changed)
			return result.toString();
		return line;
	}
	public static String[] StripColorTags(final String[] lines) {
		if (Utils.isEmpty(lines))
			return lines;
		String[] result = new String[ lines.length ];
		for (int index=0; index<lines.length; index++) {
			result[index] =
				StripColorTags(
					result[index]
				);
		}
		return result;
	}



	// ------------------------------------------------------------------------------- //
	// replace {} tags



	// replace {} or {#} tags
	public static String ReplaceTags(final String msg, final Object... args) {
		if (Utils.isEmpty(msg))  return msg;
		if (Utils.isEmpty(args)) return msg;
		final StringBuilder result = new StringBuilder(msg);
		ARG_LOOP:
		for (int index=0; index<args.length; index++) {
			final Object obj = args[index];
			final String str = (
				obj == null
				? "<null>"
				: StringUtils.toString(obj)
			);
			// {#}
			{
				final String tag =
					(new StringBuilder())
						.append('{')
						.append(index + 1)
						.append('}')
						.toString();
				boolean found = false;
				REPLACE_LOOP:
				while (true) {
					final int pos = result.indexOf(tag);
					if (pos == -1)
						break REPLACE_LOOP;
					result.replace(
						pos,
						pos + tag.length(),
						str
					);
					found = true;
				} // end REPLACE_LOOP
				if (found)
					continue ARG_LOOP;
			}
			// {}
			{
				final int pos = result.indexOf("{}");
				if (pos >= 0) {
					result.replace(
						pos,
						pos + 2,
						str
					);
					continue ARG_LOOP;
				}
			}
			// append
			result
				.append(' ')
				.append(str);
		} // end ARG_LOOP
		return result.toString();
	}



	// replace {key} tags
	public static String ReplaceTags(final String msg, final Map<String, Object> args) {
		if (Utils.isEmpty(msg))  return msg;
		if (Utils.isEmpty(args)) return msg;
		final StringBuilder result = new StringBuilder(msg);
		ARG_LOOP:
		for (final String key : args.keySet()) {
			final Object obj = args.get(key);
			final String str = (
				obj == null
				? "<null>"
				: StringUtils.toString(obj)
			);
			// {key}
			{
				final String tag =
					(new StringBuilder())
						.append('{')
						.append(key)
						.append('}')
						.toString();
				boolean found = false;
				REPLACE_LOOP:
				while (true) {
					final int pos = result.indexOf(tag);
					if (pos == -1)
						break REPLACE_LOOP;
					result.replace(
						pos,
						pos + tag.length(),
						str
					);
					found = true;
				} // end REPLACE_LOOP
				if (found)
					continue ARG_LOOP;
			}
			// {}
			{
				final int pos = result.indexOf("{}");
				if (pos != -1) {
					result.replace(
						pos,
						pos + 2,
						str
					);
				}
			}
			// don't append
		} // end ARG_LOOP
		return result.toString();
	}



	// replace {} or {#} tags (in multiple lines)
	public static String[] ReplaceTags(final String[] msgs, final Object... args) {
		if (Utils.isEmpty(msgs)) return msgs;
		if (Utils.isEmpty(args)) return msgs;
		String[] result = Arrays.copyOf(msgs, msgs.length);
		final StringBuilder extras = new StringBuilder();
		ARG_LOOP:
		for (int argIndex=0; argIndex<args.length; argIndex++) {
			final String str = (
				args[argIndex] == null
				? "<null>"
				: StringUtils.toString(args[argIndex])
			);
			// {#} - all instances
			{
				final String tag =
					(new StringBuilder())
						.append('{')
						.append(argIndex + 1)
						.append('}')
						.toString();
				boolean found = false;
				LINE_LOOP:
				for (int lineIndex=0; lineIndex<msgs.length; lineIndex++) {
					if (Utils.isEmpty( result[lineIndex] ))
						continue LINE_LOOP;
					//REPLACE_LOOP:
					while (true) {
						final int pos = result[lineIndex].indexOf(tag);
						if (pos == -1)
							continue LINE_LOOP;
						result[lineIndex] =
							StringUtils.ReplaceStringRange(
								result[lineIndex],
								str,
								pos,
								pos + tag.length()
							);
						found = true;
					} // end REPLACE_LOOP
				} // end LINE_LOOP
				if (found)
					continue ARG_LOOP;
			}
			// {} - first found
			{
				LINE_LOOP:
				for (int lineIndex=0; lineIndex<msgs.length; lineIndex++) {
					if (Utils.isEmpty( result[lineIndex] ))
						continue LINE_LOOP;
					final int pos = result[lineIndex].indexOf("{}");
					if (pos == -1)
						continue LINE_LOOP;
					result[lineIndex] =
						StringUtils.ReplaceStringRange(
							result[lineIndex],
							str,
							pos,
							pos + 2
						);
					continue ARG_LOOP;
				} // end LINE_LOOP
			}
			// append to end
			{
				if ( extras.length() != 0 )
					extras.append(' ');
				extras.append(str);
			}
		} // end ARG_LOOP
		if ( extras.length() != 0 ) {
			if (result.length == 1) {
				result[0] =
					(new StringBuilder())
					.append(result[0])
					.append(' ')
					.append(extras)
					.toString();
				return result;
			}
			String[] newResult = new String[ result.length + 1 ];
			newResult[result.length] = extras.toString();
			return newResult;
		}
		return result;
	}



	// replace {key} tags (in multiple lines)
	public static String[] ReplaceTags(final String[] msgs, final Map<String, Object> args) {
		if (Utils.isEmpty(msgs)) return msgs;
		if (Utils.isEmpty(args)) return msgs;
		String[] result = Arrays.copyOf(msgs, msgs.length);
		ARG_LOOP:
		for (final String key : args.keySet()) {
			final Object obj = args.get(key);
			final String str = (
				obj == null
				? "<null>"
				: StringUtils.toString(obj)
			);
			// {key}
			{
				final String tag =
					(new StringBuilder())
						.append('{')
						.append(key)
						.append('}')
						.toString();
				boolean found = false;
				LINE_LOOP:
				for (int lineIndex=0; lineIndex<msgs.length; lineIndex++) {
					if (Utils.isEmpty( result[lineIndex] ))
						continue LINE_LOOP;
					//REPLACE_LOOP:
					while (true) {
						final int pos = result[lineIndex].indexOf(tag);
						if (pos == -1)
							continue LINE_LOOP;
						result[lineIndex] =
							StringUtils.ReplaceStringRange(
								result[lineIndex],
								str,
								pos,
								pos + tag.length()
							);
						found = true;
					} // end REPLACE_LOOP
				} // end LINE_LOOP
				if (found)
					continue ARG_LOOP;
			}
			// {}
			{
				LINE_LOOP:
				for (int lineIndex=0; lineIndex<msgs.length; lineIndex++) {
					if (Utils.isEmpty( result[lineIndex] ))
						continue LINE_LOOP;
					final int pos = result[lineIndex].indexOf("{}");
					if (pos != -1) {
						result[lineIndex] =
							StringUtils.ReplaceStringRange(
								result[lineIndex],
								str,
								pos,
								pos + 2
							);
						continue ARG_LOOP;
					}
				} // end LINE_LOOP
			}
		}
		return result;
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
			addStrings[index] = StringUtils.toString(obj);
			index++;
		}
		return MergeStrings(delim, addStrings);
	}
	public static String MergeObjects(final char delim, final Object... addThis) {
		if (Utils.isEmpty(addThis)) throw new RequiredArgumentException("addThis");
		String[] addStrings = new String[ addThis.length ];
		int index = 0;
		for (final Object obj : addThis) {
			addStrings[index] = StringUtils.toString(obj);
			index++;
		}
		return MergeStrings(delim, addStrings);
	}



	// ------------------------------------------------------------------------------- //
	// split string



	// split by many delims
	public static String[] SplitByDelims(final String string, final char...delims) {
		final List<String> list = new ArrayList<String>();
		StringRef str = StringRef.getNew(string);
		while (str.length() > 0) {
			final String part =
				str.cutFirstPart(delims);
			if (Utils.notEmpty(part)) {
				list.add(part);
			}
		}
		return list.toArray(new String[0]);
	}
	public static String[] SplitByDelims(final String string, final String...delims) {
		final List<String> list = new ArrayList<String>();
		StringRef str = StringRef.getNew(string);
		while (str.length() > 0) {
			final String part =
				str.cutFirstPart(delims);
			if (Utils.notEmpty(part)) {
				list.add(part);
			}
		}
		return list.toArray(new String[0]);
	}



	// ------------------------------------------------------------------------------- //
	// generate string



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



}
