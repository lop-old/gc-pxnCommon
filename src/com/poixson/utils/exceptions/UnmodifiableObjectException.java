package com.poixson.utils.exceptions;

import com.poixson.utils.byref.StringRef;


public class UnmodifiableObjectException extends UnsupportedOperationException {
	private static final long serialVersionUID = 1L;



	public static UnmodifiableObjectException get() {
		final int index = 1;
		final Exception eTemp = new Exception();
		final StackTraceElement[] trace = eTemp.getStackTrace();
		final String parentClassName =
			StringRef.get(trace[index].getClassName())
				.peekLastPart('.');
		final String parentMethodName = trace[index].getMethodName();
		final StringBuilder msg =
			(new StringBuilder())
				.append( "Object cannot be modified! " )
				.append( parentClassName               )
				.append( "->"                          )
				.append( parentMethodName              )
				.append( "()"                          );
		return new UnmodifiableObjectException(msg.toString());
	}
	public UnmodifiableObjectException(final String msg) {
		super(msg);
	}



}
