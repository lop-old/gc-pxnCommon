package com.poixson.tools.byref;

import com.poixson.utils.exceptions.RequiredArgumentException;


public class DoubleRef {

	public volatile double value = 0;



	public DoubleRef(final double value) {
		this.value = value;
	}
	public DoubleRef() {}



	public void value(final double value) {
		this.value = value;
	}
	public void value(final Double value) {
		if (value == null) throw RequiredArgumentException.getNew("value");
		this.value = value.doubleValue();
	}
	public double value() {
		return this.value;
	}


	public void increment() {
		this.value++;
	}
	public void decrement() {
		this.value--;
	}



}
