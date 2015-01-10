package com.poixson.commonjava.scheduler.cron;


/**
 * This kind of exception is thrown if an invalid scheduling pattern is
 * encountered by the scheduler.
 * @author Carlo Pelliccia
 */
public class InvalidPatternException extends RuntimeException {
	private static final long serialVersionUID = 1L;



	public InvalidPatternException() {
	}
	public InvalidPatternException(final String message) {
		super(message);
	}



}
