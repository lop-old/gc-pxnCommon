package com.poixson.exceptions;


public class IORuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;



	public IORuntimeException() {
		super();
	}
	public IORuntimeException(final String msg) {
		super(msg);
	}
	public IORuntimeException(final String msg, final Throwable e) {
		super(msg, e);
	}
	public IORuntimeException(final Throwable e) {
		super(e);
	}



}
