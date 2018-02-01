package com.poixson.utils;

import java.util.ArrayList;
import java.util.List;

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
