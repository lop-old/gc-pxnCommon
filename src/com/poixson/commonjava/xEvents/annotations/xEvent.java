package com.poixson.commonjava.xEvents.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.poixson.commonjava.xEvents.xEventListener.ListenerPriority;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface xEvent {


	ListenerPriority priority() default ListenerPriority.NORMAL;
//	boolean async()             default false;
	boolean filterHandled()     default true;
	boolean filterCancelled()   default true;


}
