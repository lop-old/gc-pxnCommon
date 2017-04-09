package com.poixson.utils.exceptions;


public class LockFileException extends RuntimeException {
	private static final long serialVersionUID = 1L;



	public LockFileException(final String filename) {
		super("File already locked! "+filename);
	}



}
