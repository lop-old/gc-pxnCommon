package com.poixson.exceptions;


public class CreateDefaultYmlFileException extends Exception {
	private static final long serialVersionUID = 1L;



	public CreateDefaultYmlFileException(final String fileName) {
		super(fileName);
	}
	public CreateDefaultYmlFileException(final String fileName, final Throwable e) {
		super(fileName, e);
	}



}
