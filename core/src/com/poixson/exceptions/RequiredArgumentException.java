package com.poixson.exceptions;


public class RequiredArgumentException extends NullPointerException {
	private static final long serialVersionUID = 1L;



	public static RequiredArgumentException getNew(final String argName) {
		return new RequiredArgumentException(argName);
	}
	public RequiredArgumentException(final String argName) {
		super(
			(new StringBuilder())
				.append(argName)
				.append(" argument is required!")
				.toString()
		);
	}



}
