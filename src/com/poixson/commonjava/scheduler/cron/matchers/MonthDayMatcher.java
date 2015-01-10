package com.poixson.commonjava.scheduler.cron.matchers;


/**
 * A ValueMatcher whose rules are in a plain array of integer values. When asked
 * to validate a value, this ValueMatcher checks if it is in the array and, if
 * not, checks whether the last-day-of-month setting applies.
 * @author Paul Fernley
 */
public class MonthDayMatcher extends IntArrayValueMatcher {

	private static final int[] NumDays = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };



	/**
	 * Builds the ValueMatcher.
	 * @param integers
	 *            An ArrayList of Integer elements, one for every value accepted
	 *            by the matcher. The match() method will return true only if
	 *            its parameter will be one of this list or the
	 *            last-day-of-month setting applies.
	 */
	public MonthDayMatcher(final Integer[] integers) {
		super(integers);
	}



	/**
	 * Returns true if the given value is included in the matcher list or the
	 * last-day-of-month setting applies.
	 */
	public boolean match(final int value, final int month, final boolean isLeapYear) {
		if(super.match(value))
			return true;
		if(value > 27 && match(32) && isLastDay(value, month, isLeapYear))
			return true;
		return false;
	}



	public boolean isLastDay(final int value, final int month, final boolean isLeapYear) {
		if(isLeapYear && month == 2)
			return value == 29;
		return value == NumDays[month - 1];
	}



}
