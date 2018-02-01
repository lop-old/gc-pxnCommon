package com.poixson.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.poixson.tools.byref.StringRef;


public final class StringAdvUtils {
	private StringAdvUtils() {}



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



	// ------------------------------------------------------------------------------- //
	// replace {} tags



	// replace {} or {#} tags
	public static String ReplaceTags(final String msg, final Object... args) {
		if (Utils.isEmpty(msg))
			return msg;
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
		if (Utils.isEmpty(msg))
			return msg;
		final StringBuilder result = new StringBuilder(msg);
		final List<String> extras = new ArrayList<String>(0);
		//ARG_LOOP:
		for (final String key : args.keySet()) {
			final Object obj = args.get(key);
			final String str = (
				obj == null
				? "<null>"
				: StringUtils.toString(obj)
			);
			// {key}
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
			// append
			if (!found)
				extras.add(str);
		} // end ARG_LOOP
		// check for {} tags
		if (extras.size() > 0) {
			final Iterator<String> it = extras.iterator();
			ARG_LOOP:
			while (it.hasNext()) {
				final String arg = it.next();
				final String str = (
					arg == null
					? "<null>"
					: StringUtils.toString(arg)
				);
				// {}
				final int pos = result.indexOf("{}");
				if (pos == -1)
					break ARG_LOOP;
				result.replace(
					pos,
					pos + 2,
					str
				);
				it.remove();
			} // end ARG_LOOP
		}
		// append the rest
		if (extras.size() > 0) {
			for (final String str : extras) {
				result
					.append(' ')
					.append(str);
			}
		}
		return result.toString();
	}



	// replace {} or {#} tags (in multiple lines)
	public static String[] ReplaceTags(final String[] msgs, final Object... args) {
		if (Utils.isEmpty(msgs))
			return msgs;
		String[] result = Arrays.copyOf(msgs, msgs.length);
		final List<String> extras = new ArrayList<String>(0);
		//ARG_LOOP:
		for (int argIndex=0; argIndex<args.length; argIndex++) {
			final String str = (
				args[argIndex] == null
				? "<null>"
				: StringUtils.toString(args[argIndex])
			);
			// prepare {#} tag
			final String tagA =
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
				// {#}
				REPLACE_LOOP:
				while (true) {
					final int pos = result[lineIndex].indexOf(tagA);
					if (pos == -1)
						break REPLACE_LOOP;
					result[lineIndex] =
						StringUtils.ReplaceInString(
							result[lineIndex],
							str,
							pos,
							pos + tagA.length()
						);
					found = true;
				} // end REPLACE_LOOP
				if (found)
					continue LINE_LOOP;
			} // end LINE_LOOP
			// append
			if (!found)
				extras.add(str);
		} // end ARG_LOOP
		// check for {} tags
		if (extras.size() > 0) {
			int startFromLine = 0;
			final Iterator<String> it = extras.iterator();
			//ARG_LOOP:
			while (it.hasNext()) {
				final String arg = it.next();
				final String str = (
					arg == null
					? "<null>"
					: StringUtils.toString(arg)
				);
				LINE_LOOP:
				for (int lineIndex=startFromLine; lineIndex<result.length; lineIndex++) {
					if (Utils.isEmpty( result[lineIndex] )) {
						if (lineIndex > startFromLine)
							startFromLine = lineIndex;
						continue LINE_LOOP;
					}
					// {}
					final int pos = result[lineIndex].indexOf("{}");
					if (pos == -1) {
						if (lineIndex > startFromLine)
							startFromLine = lineIndex;
						continue LINE_LOOP;
					}
					result[lineIndex] =
						StringUtils.ReplaceInString(
							result[lineIndex],
							str,
							pos,
							pos + 2
						);
					it.remove();
					break LINE_LOOP;
				} // end LINE_LOOP
			} // end ARG_LOOP
		}
		// append the rest
		if (extras.size() > 0) {
			final int extraSize  = extras.size();
			final int resultSize = result.length;
			result = Arrays.copyOf(
				result,
				result.length + extraSize
			);
			for (int index=extraSize-1; index>=0; index--) {
				result[ resultSize + index ] = extras.get(index);
			}
		}
		return result;
	}



	// replace {key} tags (in multiple lines)
	public static String[] ReplaceTags(final String[] msgs, final Map<String, Object> args) {
		if (Utils.isEmpty(msgs))
			return msgs;
		String[] result = Arrays.copyOf(msgs, msgs.length);
		final List<String> extras = new ArrayList<String>(0);
		//ARG_LOOP:
		for (final String key : args.keySet()) {
			final Object obj = args.get(key);
			final String str = (
				obj == null
				? "<null>"
				: StringUtils.toString(obj)
			);
			// prepare {key} tag
			final String tagA =
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
				// {key}
				REPLACE_LOOP:
				while (true) {
					final int pos = result[lineIndex].indexOf(tagA);
					if (pos == -1)
						break REPLACE_LOOP;
					result[lineIndex] =
						StringUtils.ReplaceInString(
							result[lineIndex],
							str,
							pos,
							pos + tagA.length()
						);
					found = true;
				} // end REPLACE_LOOP
				if (found)
					continue LINE_LOOP;
			} // end LINE_LOOP
			// append
			if (!found)
				extras.add(str);
		} // end ARG_LOOP
		// check for {} tags
		if (extras.size() > 0) {
			return
				ReplaceTags(
					result,
					extras.toArray()
				);
		}
		return result;
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



}
