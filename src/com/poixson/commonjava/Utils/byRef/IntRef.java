package com.poixson.commonjava.Utils.byRef;

import com.poixson.commonjava.Utils.exceptions.RequiredArgumentException;


public class IntRef {

	public volatile int value = 0;



	public IntRef(final int value) {
		this.value = value;
	}
	public IntRef() {
	}



	public void value(final int val) {
		this.value = val;
	}
	public void value(final Integer val) {
		this.value = val.intValue();
		if(value == null) throw new RequiredArgumentException("value");
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
