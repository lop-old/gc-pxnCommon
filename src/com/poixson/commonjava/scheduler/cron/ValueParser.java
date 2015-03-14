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


public class ValueParser {

	public static final ValueParser PARSER_MINUTE   = new MinuteValueParser();
	public static final ValueParser PARSER_HOUR     = new HourValueParser();
	public static final ValueParser PARSER_MONTHDAY = new DayOfMonthValueParser();
	public static final ValueParser PARSER_MONTH    = new MonthValueParser();
	public static final ValueParser PARSER_WEEKDAY  = new DayOfWeekValueParser();

	protected final int minValue;
	protected final int maxValue;



	/**
	 * Builds the value parser.
	 * @param minValue The minimum allowed value.
	 * @param maxValue The maximum allowed value.
	 */
	public ValueParser(final int minValue, final int maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
	}



	public int parse(final String str) throws Exception {
		final int i;
		try {
			i = Integer.parseInt(str);
		} catch (NumberFormatException ignore) {
			throw new Exception("invalid integer value");
		}
		if(i < this.minValue || i > this.maxValue)
			throw new Exception("value out of range: "+str);
		return i;
	}



	public int getMinValue() {
		return this.minValue;
	}
	public int getMaxValue() {
		return this.maxValue;
	}



	// ==================================================



	public static class MinuteValueParser extends ValueParser {
		public MinuteValueParser() {
			super(0, 59);
		}
	}
	public static class HourValueParser extends ValueParser {
		public HourValueParser() {
			super(0, 23);
		}
	}
	public static class DayOfMonthValueParser extends ValueParser {
		public DayOfMonthValueParser() {
			super(1, 31);
		}
		/**
		 * Added to support last-day-of-month.
		 * 
		 * @param value
		 *            The value to be parsed
		 * @return the integer day of the month or 32 for last day of the month
		 * @throws Exception
		 *             if the input value is invalid
		 */
		public int parse(final String str) throws Exception {
			if(str.equalsIgnoreCase("L"))
				return 32;
			return super.parse(str);
		}
	}
	public static class MonthValueParser extends ValueParser {
		private static final String[] ALIASES = { "jan", "feb", "mar", "apr", "may",
				"jun", "jul", "aug", "sep", "oct", "nov", "dec" };
		public MonthValueParser() {
			super(1, 12);
		}
		public int parse(final String str) throws Exception {
			// try as a simple value
			try {
				return super.parse(str);
			} catch (Exception ignore) {}
			// try as an alias
			return parseAlias(str, ALIASES, 1);
		}
	}
	public static class DayOfWeekValueParser extends ValueParser {
		private static final String[] ALIASES = { "sun", "mon", "tue", "wed", "thu", "fri", "sat" };
		public DayOfWeekValueParser() {
			super(0, 7);
		}
		public int parse(final String str) throws Exception {
			// try as a simple value
			try {
				return super.parse(str) % 7;
			} catch (Exception ignore) {}
			// try as an alias
			return parseAlias(str, ALIASES, 0);
		}
	}



	/**
	 * This utility method changes an alias to an int value.
	 * @param value The value.
	 * @param aliases The aliases list.
	 * @param offset The offset appplied to the aliases list indices.
	 * @return The parsed value.
	 * @throws Exception If the expressed values doesn't match any alias.
	 */
	private static int parseAlias(final String str, final String[] aliases,
			final int offset) throws Exception {
		for(int i = 0; i < aliases.length; i++) {
			if(aliases[i].equalsIgnoreCase(str))
				return offset+i;
		}
		throw new Exception("invalid alias: "+str);
	}



}
