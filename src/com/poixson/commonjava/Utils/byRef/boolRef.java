package com.poixson.commonjava.Utils.byRef;


public class boolRef {

	public volatile boolean value = false;



	public boolRef(final boolean value) {
		this.value = value;
	}
	public boolRef() {
	}



	public void value(final boolean val) {
		this.value = val;
	}
	public boolean value() {
		return this.value;
	}



}
