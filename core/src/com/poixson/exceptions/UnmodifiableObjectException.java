package com.poixson.exceptions;

import java.util.Arrays;
import java.util.Iterator;

import com.poixson.tools.byref.StringRef;
import com.poixson.utils.StringUtils;


public class UnmodifiableObjectException extends UnsupportedOperationException {
	private static final long serialVersionUID = 1L;



	private static String getMsg() {
		final Exception eTemp = new Exception();
		final StackTraceElement[] trace = eTemp.getStackTrace();
		final Iterator<StackTraceElement> it =
				Arrays.asList(trace).iterator();
		// find calling class
		while (it.hasNext()) {
			final StackTraceElement e = it.next();
			final String className = e.getClassName();
			if (!className.endsWith("UnmodifiableObjectException")) {
				final String parentClassName =
					StringRef.getNew(className)
						.peekLastPart('.');
				final String parentMethodName =
					StringUtils.Trim(e.getMethodName(), '<', '>');
				return
					(new StringBuilder())
						.append( "Object cannot be modified! " )
						.append( parentClassName               )
						.append( "->"                          )
						.append( parentMethodName              )
						.append( "()"                          )
						.toString();
			}
		}
		return null;
	}



	public UnmodifiableObjectException() {
		super(getMsg());
	}
	public UnmodifiableObjectException(final String msg) {
		super(msg);
	}



}
