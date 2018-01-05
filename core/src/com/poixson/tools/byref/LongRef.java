package com.poixson.tools.byref;

import com.poixson.utils.exceptions.RequiredArgumentException;


public class LongRef {

	public volatile long value = 0;



	public LongRef(final long value) {
		this.value = value;
	}
	public LongRef() {}



	public void value(final long value) {
		this.value = value;
	}
	public void value(final Long value) {
		if (value == null) throw RequiredArgumentException.getNew("value");
		this.value = value.longValue();
	}
	public long value() {
		return this.value;
	}


	public void increment() {
		this.value++;
	}
	public void decrement() {
		this.value--;
	}



}
