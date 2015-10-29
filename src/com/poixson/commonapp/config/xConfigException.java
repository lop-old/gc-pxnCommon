package com.poixson.commonapp.config;


public class xConfigException extends Exception {
	private static final long serialVersionUID = 1L;



	public xConfigException(final Throwable e, final String path) {
		super("Path: "+path, e);
	}
	public xConfigException(final String msg, final Throwable e) {
		super(msg, e);
	}
	public xConfigException(final Throwable e) {
		super(e);
	}
	public xConfigException(final String msg) {
		super(msg);
	}



}
