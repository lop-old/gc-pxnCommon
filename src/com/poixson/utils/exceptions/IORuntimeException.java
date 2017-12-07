package com.poixson.utils.exceptions;


public class IORuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;



	public static IORuntimeException getNew() {
		return new IORuntimeException();
	}
	public static IORuntimeException getNew(final String msg) {
		return new IORuntimeException(msg);
	}
	public static IORuntimeException getNew(final String msg, final Throwable e) {
		return new IORuntimeException(msg, e);
	}
	public static IORuntimeException getNew(final Throwable e) {
		return new IORuntimeException(e);
	}



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
