package com.poixson.commonjava.Utils.byRef;

import com.poixson.commonjava.Utils.utils;


public class StringRef {

	public volatile String value = null;



	public StringRef(final String value) {
		this.value = value;
	}
	public StringRef() {
	}



	public void value(final String val) {
		this.value = val;
	}
	public String value() {
		return this.value;
	}



	public boolean isEmpty() {
		return utils.isEmpty(this.value);
	}
	public boolean notEmpty() {
		return utils.notEmpty(this.value);
	}



}
