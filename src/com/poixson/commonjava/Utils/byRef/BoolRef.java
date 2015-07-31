package com.poixson.commonjava.Utils.byRef;


public class BoolRef {

	public volatile boolean value = false;



	public BoolRef(final boolean value) {
		this.value = value;
	}
	public BoolRef() {
	}



	public void value(final boolean val) {
		this.value = val;
	}
	public void value(final Boolean val) {
		if(val == null) throw new NullPointerException("val argument is required!");
		this.value = val.booleanValue();
	}
	public boolean value() {
		return this.value;
	}



}
