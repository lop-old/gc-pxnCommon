package com.poixson.exceptions;

import com.poixson.utils.StringUtils;

public class RequiredArgumentException extends NullPointerException {
	private static final long serialVersionUID = 1L;



	public RequiredArgumentException(final String argName) {
		super(
			StringUtils.ReplaceTags(
				"{} argument is required!",
				argName
			)
		);
	}



}
