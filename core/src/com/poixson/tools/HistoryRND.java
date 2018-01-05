package com.poixson.tools;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.poixson.utils.NumberUtils;
import com.poixson.utils.Utils;


public class HistoryRND {

	public static final int DEFAULT_MAX_TRIES = 10;

	private int minValue;
	private int maxValue;

	private final Random gen = new Random(Utils.getSystemMillis());

	private final RollingList<Integer> history;
	private final AtomicInteger index = new AtomicInteger(0);

	private int maxTries = Integer.MIN_VALUE;
	private int lastValue = Integer.MIN_VALUE;



	public HistoryRND(final int minValue, final int maxValue) {
		this(
			minValue,
			maxValue,
			NumberUtils.MinMax(
				(int)(
					((double)(maxValue - minValue)) / 1.5
				),
				2,
				1000
			)
		);
	}
	public HistoryRND(final int minValue, final int maxValue, final int historySize) {
		if (minValue > maxValue) throw new IllegalArgumentException("minValue must be lower than maxValue!");
		this.minValue = minValue;
		this.maxValue = maxValue;
		final int histSize =
			NumberUtils.MinMax(
				historySize,
				2,
				(maxValue - minValue) + 1
			);
		this.history = new RollingList<Integer>(histSize);
	}



	// random number (unique history)
	public synchronized int rnd() {
		final int index = this.index.incrementAndGet();
		final int maxValue  = this.getMaxValue();
		final int minValue  = this.getMinValue();
		if (minValue == maxValue)
			return minValue;
		if ((maxValue - minValue) == 1) {
			if (this.lastValue == minValue) {
				this.lastValue = maxValue;
				return maxValue;
			}
			this.lastValue = minValue;
			return minValue;
		}
		// find unique random number
		int tries = 0;
		final int maxTries = this.getMaxTries();
		while (true) {
			// get a new random number
			this.gen.setSeed( Utils.getSystemMillis() + ((long)tries) + ((long)index) );
			final int number = this.gen.nextInt(maxValue - minValue) + minValue;
			if ( ! this.history.contains(new Integer(number)) ) {
				this.lastValue = number;
				this.history.add(new Integer(number));
				return number;
			}
			// give up trying
			tries++;
			if (tries >= maxTries) {
				if (number != this.lastValue) {
					this.lastValue = number;
					return number;
				}
				if (tries > maxTries) {
					return number;
				}
			}
		}
	}



	// min value
	public HistoryRND setMinValue(final int minValue) {
		this.minValue = minValue;
		return this;
	}
	public int getMinValue() {
		return this.minValue;
	}



	// max value
	public HistoryRND setMaxValue(final int maxValue) {
		this.maxValue = maxValue;
		return this;
	}
	public int getMaxValue() {
		return this.maxValue;
	}



	// max tries to find a unique number
	public HistoryRND setMaxTries(final int maxTries) {
		this.maxTries = maxTries;
		return this;
	}
	public int getMaxTries() {
		final int maxTries = this.maxTries;
		return (
			maxTries == Integer.MIN_VALUE
			? DEFAULT_MAX_TRIES
			: maxTries
		);
	}



}
