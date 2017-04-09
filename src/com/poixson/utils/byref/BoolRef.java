package com.poixson.utils.byref;

import com.poixson.utils.exceptions.RequiredArgumentException;


public class BoolRef {

	public volatile boolean value = false;



	public BoolRef(final boolean value) {
		this.value = value;
	}
	public BoolRef() {
	}



	public void value(final boolean value) {
		this.value = value;
	}
	public void value(final Boolean value) {
		if(value == null) throw new RequiredArgumentException("value");
		this.value = value.booleanValue();
	}
	public boolean value() {
		return this.value;
	}



	public boolean invert() {
		return this.value = (!this.value);
//		return this.value;
	}



}
