package com.poixson.commonjava.Utils.byRef;

import com.poixson.commonjava.Utils.exceptions.RequiredArgumentException;


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
		this.value = val.booleanValue();
		if(value == null) throw new RequiredArgumentException("value");
	}
	public boolean value() {
		return this.value;
	}



	public boolean invert() {
		return this.value = (!this.value);
//		return this.value;
	}



}
