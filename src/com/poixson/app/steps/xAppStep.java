package com.poixson.app.steps;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface xAppStep {

	public enum StepType {STARTUP, SHUTDOWN};

	StepType type();
	int priority() default 100;
	String title() default "";

}
