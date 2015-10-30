package com.poixson.commonjava.Utils;

import java.util.concurrent.TimeUnit;


public class xTimeUnmodifiable extends xTime {



	// new object
	protected xTimeUnmodifiable(final xTime time) {
		super(time.getMS());
	}
	// clone object
	@Override
	public xTime clone() {
		return super.clone();
	}
	public static xTimeUnmodifiable cast(final xTime time) {
		return new xTimeUnmodifiable(time);
	}



	// reset
	@Override
	public void reset() {
	}
	// set value
	@Override
	public xTime set(final long value, final TimeUnit unit) {
		return this;
	}
	@Override
	public xTime set(final String val) {
		return this;
	}
	@Override
	public xTime set(final xTime time) {
		return this;
	}
	// add time
	@Override
	public void add(final long val, final TimeUnit unit) {
	}
	@Override
	public void add(final String val) {
	}
	@Override
	public void add(final xTime time) {
	}



}
