package com.poixson.commonjava.Utils.byRef;


public class stringRef {

	public volatile String value = null;



	public stringRef(final String value) {
		this.value = value;
	}
	public stringRef() {
	}



	public void value(final String value) {
		this.value = value;
	}
	public String value() {
		return this.value;
	}



}
