package com.poixson.app;

import java.lang.reflect.Method;

import com.poixson.app.xAppStep.StepType;
import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.xLogRoot;
import com.poixson.tools.remapped.RunnableNamed;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class xAppStepDAO implements RunnableNamed {

	public final StepType type;
	public final int      stepValue;
	public final String   name;
	public final String   title;

	public final xApp     app;
	public final Object   container;
	public final Method   method;
	public final xAppStep anno;



	public xAppStepDAO(final xApp app,
			final Object container, final Method method, final xAppStep anno) {
		if (app       == null) throw new RequiredArgumentException("app");
		if (container == null) throw new RequiredArgumentException("container");
		if (method    == null) throw new RequiredArgumentException("method");
		if (anno      == null) throw new RequiredArgumentException("annotation");
		this.type = anno.type();
		this.stepValue = Math.abs(anno.priority());
		this.app       = app;
		this.container = container;
		this.method    = method;
		this.anno      = anno;
		{
			String name = method.getName();
			name = StringUtils.Trim(
				name,
				"_",
				"startup",
				"start",
				"shutdown",
				"stop"
			);
			this.name =
				Utils.isEmpty(name)
				? StringUtils.Trim(this.method.getName(), "_")
				: name;
		}
		this.title = (
			Utils.isEmpty(anno.title())
			? this.name
			: anno.title()
		);
	}



	public boolean isType(final StepType type) {
		if (type == null)
			return false;
		return type.equals(this.type);
	}



	public boolean isStepValue(final int stepValue) {
		return (this.stepValue == stepValue);
	}



	public void invoke() throws ReflectiveOperationException, RuntimeException {
		xLogRoot.get()
			.fine("Invoking step {}: {}", this.priority, this.name);
		try {
			this.method.invoke(this.container, this.app);
		} catch (Exception e) {
			Failure.fail(e);
		}
	}
	@Override
	public void run() {
		try {
			this.invoke();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}



	@Override
	public String getTaskName() {
		return this.name;
	}
	@Override
	public void setTaskName(final String name) {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean taskNameEquals(final String name) {
		if (Utils.isEmpty(name))
			return false;
		return name.equals(this.getTaskName());
	}



}
