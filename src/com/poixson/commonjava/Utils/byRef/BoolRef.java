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
	public boolean value() {
		return this.value;
	}



}
