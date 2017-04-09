package com.poixson.commonjava.Utils;


public class LockFileException extends RuntimeException {
	private static final long serialVersionUID = 1L;



	public LockFileException(final String filename) {
		super("File already locked! "+filename);
	}



}
