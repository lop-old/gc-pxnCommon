package com.poixson.commonjava.Utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class HistoryRND {

	private int min;
	private int max;
	private int last;
	private BlockingQueue<Integer> history = new LinkedBlockingQueue<Integer>();
	private int historySize;


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
		if(min == max) return min;
		if((max - min) == 1) {
			if(last == min) {
				last = max;
				return max;
			} else {
				last = min;
				return min;
			}
		}
		int number = 0;
		int i = 10;
		// find unique random number
		while(true) {
			// find a new random number
			number = utilsMath.getRandom(min, max);
			if(!history.contains(number)) break;
			// give up trying
			i--;
			if(i < 0) {
				if(number != last) break;
				if(i < 0-i) break;
			}
		}
		last = number;
		history.add(number);
		while (history.size() > historySize)
			history.remove();
		return number;
	}


}
