package com.poixson.commonjava.scheduler.cron.matchers;


/**
 * This ValueMatcher always returns true!
 * @author Carlo Pelliccia
 */
public class AlwaysTrueValueMatcher implements ValueMatcher {


	@Override
	public boolean match(int value) {
		return true;
	}


}
