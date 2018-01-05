package com.poixson.tools.byref;

import com.poixson.exceptions.RequiredArgumentException;


public class IntRef {

	public volatile int value = 0;



	public IntRef(final int value) {
		this.value = value;
	}
	public IntRef() {}



	public void value(final int value) {
		this.value = value;
	}
	public void value(final Integer value) {
		if (value == null) throw RequiredArgumentException.getNew("value");
		this.value = value.intValue();
	}
	public int value() {
		return this.value;
	}



	public void increment() {
		this.value++;
	}
	public void decrement() {
		this.value--;
	}



}
