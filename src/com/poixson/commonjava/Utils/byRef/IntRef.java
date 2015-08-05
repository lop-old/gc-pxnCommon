package com.poixson.commonjava.Utils.byRef;


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
		if(val == null) throw new NullPointerException("val argument is required!");
		this.value = val.intValue();
	}
	public int value() {
		return this.value;
	}



}
