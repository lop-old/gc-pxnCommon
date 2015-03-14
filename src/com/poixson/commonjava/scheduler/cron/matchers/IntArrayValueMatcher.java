package com.poixson.commonjava.scheduler.cron.matchers;


/**
 * A ValueMatcher whose rules are in a plain array of integer values. When asked
 * to validate a value, this ValueMatcher checks if it is in the array.
 * @author Carlo Pelliccia
 */
public class IntArrayValueMatcher implements ValueMatcher {

	// accepted values
	private final int[] values;



	/**
	 * Builds the ValueMatcher.
	 * @param integers
	 *            An ArrayList of Integer elements, one for every value accepted
	 *            by the matcher. The match() method will return true only if
	 *            its parameter will be one of this list.
	 */
	public IntArrayValueMatcher(final Integer[] integers) {
		final int size = integers.length;
		this.values = new int[size];
		for(int i = 0; i < size; i++) {
			try {
				this.values[i] = integers[i].intValue();
			} catch (Exception e) {
				throw new IllegalArgumentException(e.getMessage());
			}
		}
	}



	/**
	 * Returns true if the given value is included in the matcher list.
	 */
	@Override
	public boolean match(final int value) {
		for(int i = 0; i < this.values.length; i++) {
			if(this.values[i] == value)
				return true;
		}
		return false;
	}



}
