package com.poixson.commonjava.Utils.byRef;


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



}
