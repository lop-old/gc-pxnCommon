package com.poixson.commonjava.scheduler.cron;
/*
 * cron4j - A pure Java cron-like scheduler
 *
 * Copyright (C) 2007-2010 Carlo Pelliccia (www.sauronsoftware.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version
 * 2.1, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License 2.1 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License version 2.1 along with this program.
 * If not, see <http://www.gnu.org/licenses/>
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.scheduler.cron.matchers.AlwaysTrueValueMatcher;
import com.poixson.commonjava.scheduler.cron.matchers.IntArrayValueMatcher;
import com.poixson.commonjava.scheduler.cron.matchers.MonthDayMatcher;
import com.poixson.commonjava.scheduler.cron.matchers.ValueMatcher;


public class CronPattern {

	public final ValueMatcher matcher_minute;
	public final ValueMatcher matcher_hour;
	public final ValueMatcher matcher_monthday;
	public final ValueMatcher matcher_month;
	public final ValueMatcher matcher_weekday;

	protected final String original;



	public static CronPattern[] parse(final String pattern) {
		if(utils.isEmpty(pattern)) return null;
		final List<CronPattern> list = new ArrayList<CronPattern>();
		final StringTokenizer tokens = new StringTokenizer(pattern, "|");
		if(tokens.countTokens() < 1)
			throw new InvalidPatternException("invalid pattern: "+pattern);
		while(tokens.hasMoreTokens()) {
			final String part = tokens.nextToken();
			if(utils.isEmpty(part)) continue;
			final CronPattern pat = new CronPattern(part);
			list.add(pat);
		}
		return list.toArray(new CronPattern[0]);
	}
	/**
	 * Validates a string as a scheduling pattern.
	 * @param schedulingPattern The pattern to validate.
	 * @return true if the given string represents a valid scheduling pattern;
	 *         false otherwise.
	 */
	public static boolean validate(final String pattern) {
		try {
			new CronPattern(pattern);
		} catch (InvalidPatternException ignore) {
			return false;
		}
		return true;
	}



	// ==================================================



	/**
	 * Builds a SchedulingPattern parsing it from a string.
	 * @param pattern The pattern as a crontab-like string.
	 * @throws InvalidPatternException If the supplied string is not a valid pattern.
	 */
	public CronPattern(final String pattern) {
		this.original = pattern;
		final StringTokenizer token = new StringTokenizer(pattern, " \t");
		if(token.countTokens() != 5)
			throw new InvalidPatternException("invalid pattern [ "+token+" ]");
		// minute
		try {
			this.matcher_minute = buildValueMatcher(
					token.nextToken(),
					ValueParser.PARSER_MINUTE
			);
		} catch (Exception e) {
			throw new InvalidPatternException("invalid pattern "
					+"[ "+token+" ] Error parsing minutes field: "
					+e.getMessage());
		}
		// hour
		try {
			this.matcher_hour = buildValueMatcher(
					token.nextToken(),
					ValueParser.PARSER_HOUR
			);
		} catch (Exception e) {
			throw new InvalidPatternException("invalid pattern "
					+"[ "+token+" ] Error parsing hours field: "
					+e.getMessage());
		}
		// day of month
		try {
			this.matcher_monthday = buildValueMatcher(
					token.nextToken(),
					ValueParser.PARSER_MONTHDAY
			);
		} catch (Exception e) {
			throw new InvalidPatternException("invalid pattern "
					+"[ "+token+" ] Error parsing days of month field: "
					+e.getMessage());
		}
		// month
		try {
			this.matcher_month = buildValueMatcher(
					token.nextToken(),
					ValueParser.PARSER_MONTH
			);
		} catch (Exception e) {
			throw new InvalidPatternException("invalid pattern "
					+"[ "+token+" ] Error parsing months field: "
					+e.getMessage());
		}
		// day of week
		try {
			this.matcher_weekday = buildValueMatcher(
					token.nextToken(),
					ValueParser.PARSER_WEEKDAY
			);
		} catch (Exception e) {
			throw new InvalidPatternException("invalid pattern "
					+"[ "+token+" ] Error parsing days of week field: "
					+e.getMessage());
		}
	}



	/**
	 * A ValueMatcher utility builder.
	 * @param str The pattern part for the ValueMatcher creation.
	 * @param parser The parser used to parse the values.
	 * @return The requested ValueMatcher.
	 * @throws Exception If the supplied pattern part is not valid.
	 */
	private ValueMatcher buildValueMatcher(final String str, final ValueParser parser)
			throws Exception {
		if(str.equals("*"))
			return new AlwaysTrueValueMatcher();
		final List<Integer> values = new ArrayList<Integer>();
		final StringTokenizer token = new StringTokenizer(str, ",");
		while(token.hasMoreTokens()) {
			final String element = token.nextToken();
			final List<Integer> local;
			try {
				local = parseListElement(element, parser);
			} catch (Exception e) {
				throw new Exception("invalid field [ "+str+" ] "
						+"invalid element [ "+element+" ] "
						+e.getMessage());
			}
			final Iterator<Integer> it = local.iterator();
			while(it.hasNext()) {
				final Integer value = it.next();
				if(!values.contains(value))
					values.add(value);
			}
		}
		if(values.isEmpty())
			throw new Exception("invalid field: "+str);
		if(parser == ValueParser.PARSER_MONTHDAY)
			return new MonthDayMatcher(values.toArray(new Integer[0]));
		return new IntArrayValueMatcher(values.toArray(new Integer[0]));
	}



	/**
	 * Parses an element of a list of values of the pattern.
	 * @param str The element string.
	 * @param parser The parser used to parse the values.
	 * @return A list of integers representing the allowed values.
	 * @throws Exception If the supplied pattern part is not valid.
	 */
	private List<Integer> parseListElement(final String str, final ValueParser parser) throws Exception {
		final StringTokenizer token = new StringTokenizer(str, "/");
		final int size = token.countTokens();
		if(size < 1 || size > 2)
			throw new Exception("syntax error");
		final List<Integer> values;
		try {
			values = parseRange(token.nextToken(), parser);
		} catch (Exception e) {
			throw new Exception("invalid range, "+e.getMessage());
		}
		if(size == 2) {
			final String part = token.nextToken();
			int div;
			try {
				div = Integer.parseInt(part);
			} catch (NumberFormatException ignore) {
				throw new Exception("invalid divisor: "+part);
			}
			if(div < 1)
				throw new Exception("non positive divisor: "+div);
			final List<Integer> values2 = new ArrayList<Integer>();
			for(int i = 0; i < values.size(); i += div)
				values2.add(values.get(i));
			return values2;
		}
		return values;
	}



	/**
	 * Parses a range of values.
	 * @param str The range string.
	 * @param parser The parser used to parse the values.
	 * @return A list of integers representing the allowed values.
	 * @throws Exception If the supplied pattern part is not valid.
	 */
	private List<Integer> parseRange(final String str, final ValueParser parser) throws Exception {
		if(str.equals("*")) {
			final int min = parser.getMinValue();
			final int max = parser.getMaxValue();
			final List<Integer> values = new ArrayList<Integer>();
			for(int i = min; i <= max; i++)
				values.add(new Integer(i));
			return values;
		}
		final StringTokenizer token = new StringTokenizer(str, "-");
		final int size = token.countTokens();
		if(size < 1 || size > 2)
			throw new Exception("syntax error");
		final String str1 = token.nextToken();
		int v1;
		try {
			v1 = parser.parse(str1);
		} catch (Exception e) {
			throw new Exception("invalid value [ "+str1+" ] "+e.getMessage());
		}
		if(size == 1) {
			final List<Integer> values = new ArrayList<Integer>();
			values.add(new Integer(v1));
			return values;
		}
		final String str2 = token.nextToken();
		int v2;
		try {
			v2 = parser.parse(str2);
		} catch (Exception e) {
			throw new Exception("invalid value [ "+str2+" ] "+e.getMessage());
		}
		final List<Integer> values = new ArrayList<Integer>();
		if(v1 < v2) {
			for(int i = v1; i <= v2; i++)
				values.add(new Integer(i));
		} else if(v1 > v2) {
			final int min = parser.getMinValue();
			final int max = parser.getMaxValue();
			for(int i = v1; i <= max; i++)
				values.add(new Integer(i));
			for(int i = min; i <= v2; i++)
				values.add(new Integer(i));
		// v1 == v2
		} else {
			values.add(new Integer(v1));
		}
		return values;
	}



	/**
	 * This methods returns true if the given timestamp (expressed as a UNIX-era
	 * millis value) matches the pattern, according to the given time zone.
	 * @param timezone A time zone.
	 * @param millis The timestamp, as a UNIX-era millis value.
	 * @return true if the given timestamp matches the pattern.
	 */
	public boolean match(final TimeZone timezone, final long millis) {
		final GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeZone(timezone);
		cal.setTimeInMillis(millis);
		int minute   = cal.get(Calendar.MINUTE);
		int hour     = cal.get(Calendar.HOUR_OF_DAY);
		int monthday = cal.get(Calendar.DAY_OF_MONTH);
		int month    = cal.get(Calendar.MONTH) + 1;
		int weekday  = cal.get(Calendar.DAY_OF_WEEK) - 1;
		int year     = cal.get(Calendar.YEAR);
		boolean eval = true;
		if(!this.matcher_minute.match(minute)) eval = false;
		if(!this.matcher_hour.match(hour))     eval = false;
		if(this.matcher_monthday instanceof MonthDayMatcher) {
			if(!((MonthDayMatcher) this.matcher_monthday).match(monthday, month, cal.isLeapYear(year))) eval = false;
		} else {
			if(!this.matcher_monthday.match(monthday)) eval = false;
		}
		if(!this.matcher_month.match(month))     eval = false;
		if(!this.matcher_weekday.match(weekday)) eval = false;
		return eval;
	}



	/**
	 * This methods returns true if the given timestamp (expressed as a UNIX-era
	 * millis value) matches the pattern, according to the system default time zone.
	 * @param millis The timestamp, as a UNIX-era millis value.
	 * @return true if the given timestamp matches the pattern.
	 */
	public boolean match(final long millis) {
		return match(TimeZone.getDefault(), millis);
	}



	/**
	 * Returns the pattern as a string.
	 * @return The pattern as a string.
	 */
	@Override
	public String toString() {
		return this.original;
	}



}
