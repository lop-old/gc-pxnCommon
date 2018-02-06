package com.poixson.utils.apache;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.List;

import com.poixson.utils.StringUtils;


// original: http://grepcode.com/file/repo1.maven.org/maven2/commons-lang/commons-lang/2.6/org/apache/commons/lang/StringUtils.java?av=f
public final class ApacheCommons {
	private ApacheCommons() {}



	public static String[] SplitByChars(final String value, final String delims) {
		return SplitWorker(
			value,
			delims,
			-1,
			false
		);
	}
	private static String[] SplitWorker(final String str, final String delims,
			final int max, final boolean preserveTokens) {
		if (str == null)
			return null;
		final int len = str.length();
		if (len == 0) {
			return new String[0];
		}
		final List<String> list = new ArrayList<String>();
		int sizePlusOne = 1;
		int i     = 0;
		int start = 0;
		boolean match     = false;
		boolean lastMatch = false;
		// Null separator means use whitespace
		if (delims == null) {
			while (i < len) {
				if (Character.isWhitespace(str.charAt(i))) {
					if (match || preserveTokens) {
						lastMatch = true;
						if (sizePlusOne++ == max) {
							i = len;
							lastMatch = false;
						}
						list.add(str.substring(start, i));
						match = false;
					}
					start = ++i;
					continue;
				}
				lastMatch = false;
				match = true;
				i++;
			}
		} else
		// Optimise 1 character case
		if (delims.length() == 1) {
			char sep = delims.charAt(0);
			while (i < len) {
				if (str.charAt(i) == sep) {
					if (match || preserveTokens) {
						lastMatch = true;
						if (sizePlusOne++ == max) {
							i = len;
							lastMatch = false;
						}
						list.add(str.substring(start, i));
						match = false;
					}
					start = ++i;
					continue;
				}
				lastMatch = false;
				match = true;
				i++;
			}
		// standard case
		} else {
			while (i < len) {
				if (delims.indexOf(str.charAt(i)) >= 0) {
					if (match || preserveTokens) {
						lastMatch = true;
						if (sizePlusOne++ == max) {
							i = len;
							lastMatch = false;
						}
						list.add(str.substring(start, i));
						match = false;
					}
					start = ++i;
					continue;
				}
				lastMatch = false;
				match = true;
				i++;
			}
		}
		if (match || (preserveTokens && lastMatch) ) {
			list.add(str.substring(start, i));
		}
		return list.toArray(new String[list.size()]);
	}



	public static String ReplaceEach(final String text,
			final String[] search, final String[] replace) {
		return ReplaceEach(
			text,
			search,
			replace,
			true,
			(search == null ? 0 : search.length)
		);
	}
	private static String ReplaceEach(final String text,
			final String[] search, final String[] replace,
			final boolean repeat, final int ttl) {
		if (text == null || text.length() == 0) {
			return text;
		}
		if (search == null || search.length == 0) {
			return text;
		}
		if (replace == null || replace.length == 0) {
			return text;
		}
		// if recursing, this shouldnt be less than 0
		if (ttl < 0) {
			throw new IllegalStateException(
				StringUtils.ReplaceTags(
					"TimeToLive of: {} is less than 0: {}",
					ttl,
					text
				)
			);
		}
		final int searchSize  = search.length;
		final int replaceSize = replace.length;
		// make sure lengths are ok, these need to be equal
		if (searchSize != replaceSize) {
			throw new IllegalArgumentException(
				StringUtils.ReplaceTags(
					"Search and Replace array lengths don't match: {} vs {}",
					searchSize,
					replaceSize
				)
			);
		}
		// keep track of which still have matches
		final boolean[] noMoreMatchesForReplaceIndex = new boolean[searchSize];
		// index on index that the match was found
		int textIndex    = -1;
		int replaceIndex = -1;
		int index        = -1;
		// index of replace array that will replace the search string found
		// NOTE: logic duplicated below START
		for (int i=0; i < searchSize; i++) {
			if (noMoreMatchesForReplaceIndex[i] || search[i] == null)
				continue;
			if (search[i].length() == 0 || replace[i] == null)
				continue;
			index = text.indexOf(search[i]);
			// see if we need to keep searching for this
			if (index == -1) {
				noMoreMatchesForReplaceIndex[i] = true;
			} else
			if (textIndex == -1 || index < textIndex) {
				textIndex = index;
				replaceIndex = i;
			}
		}
		// NOTE: logic mostly below END
		// no search strings found, we are done
		if (textIndex == -1) {
			return text;
		}
		int start = 0;
		// get a good guess on the size of the result buffer
		// so it doesnt have to double if it goes over a bit.
		int increase = 0;
		// count the replacement text elements that are larger
		// than their corresponding text being replaced.
		for (int i=0; i < search.length; i++) {
			if (search[i] == null || replace[i] == null) {
				continue;
			}
			int greater = replace[i].length() - search[i].length();
			if (greater > 0) {
				// assume 3 matches
				increase += (3 * greater);
			}
		}
		// have upper-bound at 20% increase, then let Java take over
		increase = Math.min(increase, text.length() / 5);
		final StringBuilder buf = new StringBuilder();
		while (textIndex != -1) {
			for (int i = start; i < textIndex; i++) {
				buf.append(text.charAt(i));
			}
			buf.append(replace[replaceIndex]);
			start = textIndex + search[replaceIndex].length();
		}
		textIndex    = -1;
		replaceIndex = -1;
		index        = -1;
		// find the next earliest match
		// NOTE: logic mostly duplicated above START
		for (int i=0; i < searchSize; i++) {
			if (noMoreMatchesForReplaceIndex[i] || search[i] == null)
				continue;
			if (search[i].length() == 0 || replace[i] == null)
				continue;
			index = text.indexOf(search[i], start);
			// see if we need to keep searching for this
			if (index == -1) {
				noMoreMatchesForReplaceIndex[i] = true;
			} else
			if (textIndex == -1 || index < textIndex) {
				textIndex = index;
				replaceIndex = i;
			}
		}
		// NOTE: logic duplicated above END
		int textLen = text.length();
		for (int i = start; i < textLen; i++) {
			buf.append(text.charAt(i));
		}
		// no repeat
		if (!repeat) {
			return buf.toString();
		}
		// repeat ttl times
		return ReplaceEach(
				buf.toString(),
				search,
				replace,
				repeat,
				ttl - 1
		);
	}



}
