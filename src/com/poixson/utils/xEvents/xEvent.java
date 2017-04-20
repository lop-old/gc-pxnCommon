package com.poixson.utils.xEvents;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.poixson.utils.xEvents.xEventListener.ListenerPriority;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface xEvent {


	ListenerPriority priority() default ListenerPriority.NORMAL;
//	boolean async()             default false;
	boolean filterHandled()     default true;
	boolean filterCancelled()   default true;


}
