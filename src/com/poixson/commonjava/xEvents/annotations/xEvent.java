package com.poixson.commonjava.EventListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface xEvent {


	public static enum Priority {
		HIGHEST,
		HIGH,
		NORMAL,
		LOW,
		LOWEST
	}


	Priority priority()       default Priority.NORMAL;
	boolean threaded()        default false;
	boolean filterHandled()   default true;
	boolean filterCancelled() default true;


}
