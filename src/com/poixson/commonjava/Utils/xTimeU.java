package com.poixson.commonjava.Utils;

import java.util.concurrent.TimeUnit;


public final class xTimeU {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	private xTimeU() {}

	public static final TimeUnit MS = TimeUnit.MILLISECONDS;
	public static final TimeUnit S  = TimeUnit.SECONDS;
	public static final TimeUnit M  = TimeUnit.MINUTES;
	public static final TimeUnit H  = TimeUnit.HOURS;
	public static final TimeUnit D  = TimeUnit.DAYS;

}
