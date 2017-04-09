package com.poixson.utils.exceptions;


public class RequiredArgumentException extends NullPointerException {
	private static final long serialVersionUID = 1L;



	public RequiredArgumentException(final String argName) {
		super( (new StringBuilder())
				.append(argName)
				.append(" argument is required!")
				.toString()
		);
	}



}
