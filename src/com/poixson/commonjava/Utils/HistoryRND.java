package com.poixson.commonjava.Utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class HistoryRND {

	private final int min;
	private final int max;
	private volatile int last;
	private final BlockingQueue<Integer> history = new LinkedBlockingQueue<Integer>();
	private final int historySize;



	public HistoryRND(final int min, final int max) {
		this(min, max, (max - min) / 2);
	}
	public HistoryRND(final int min, final int max, final int historySize) {
		if(min > max) throw new IllegalArgumentException("min must be lower than max!");
		this.min = min;
		this.max = max;
		this.historySize = utilsMath.MinMax(historySize, 2, (max-min)-1);
		this.last = min - 1;
	}



	// random number (unique history)
	public synchronized int RND() {
		if(this.min == this.max)
			return this.min;
		if((this.max - this.min) == 1) {
			if(this.last == this.min) {
				this.last = this.max;
				return this.max;
			}
			this.last = this.min;
			return this.min;
		}
		int number = 0;
		int i = 10;
		// find unique random number
		while(true) {
			// find a new random number
			number = utilsMath.getRandom(this.min, this.max);
			if(!this.history.contains(new Integer(number))) break;
			// give up trying
			i--;
			if(i < 0) {
				if(number != this.last) break;
				if(i < 0-i) break;
			}
		}
		this.last = number;
		this.history.add(new Integer(number));
		while (this.history.size() > this.historySize)
			this.history.remove();
		return number;
	}



}
