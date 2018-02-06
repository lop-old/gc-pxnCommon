package com.poixson.exceptions;

import com.poixson.utils.StringUtils;


public class MismatchedVersionException extends Exception {
	private static final long serialVersionUID = 1L;

	public final String expectedVersion;
	public final String libraryVersion;



	public MismatchedVersionException(
			final String expectedVersion, final String libraryVersion) {
		super(
			StringUtils.ReplaceTags(
				"Expected version: {} Found version: {}",
				expectedVersion,
				libraryVersion
			)
		);
		this.expectedVersion = expectedVersion;
		this.libraryVersion  = libraryVersion;
	}



}
