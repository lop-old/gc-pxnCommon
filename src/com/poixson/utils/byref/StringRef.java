package com.poixson.utils.byref;

import com.poixson.utils.Utils;


public class StringRef {

	public volatile String value = null;



	public StringRef(final String value) {
		this.value = value;
	}
	public StringRef() {}



	public void value(final String val) {
		this.value = val;
	}
	public String value() {
		return this.value;
	}



	public boolean isEmpty() {
		return Utils.isEmpty(this.value);
	}
	public boolean notEmpty() {
		return Utils.notEmpty(this.value);
	}



}
