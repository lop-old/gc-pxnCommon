package com.poixson.app;

import java.lang.reflect.Method;

import com.poixson.app.xAppStep.StepType;
import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.xLog;
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
		this.type = anno.Type();
		this.stepValue = (
			StepType.STARTUP.equals(anno.Type())
			? anno.StepValue()
			: 0 - Math.abs( anno.StepValue() )
		);
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
		{
			final String title = anno.Title();
			this.title = (
				Utils.isEmpty(title)
				? this.name
				: title
			);
		}
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
		final String stepStr =
			StringUtils.MergeStrings(
				'-',
				( this.stepValue > xApp.STATE_OFF ? "startup" : "shutdown" ),
				Integer.toString(this.stepValue),
				( Utils.isEmpty(this.title) ? this.name : this.title )
			);
		final xLog log = this.app.log()
				.getWeak(stepStr);
		log.finer("Invoking step @|white,bold {}|@: {}", this.stepValue, this.name);
		final Thread currentThread = Thread.currentThread();
		final String originalThreadName = currentThread.getName();
		currentThread.setName(stepStr);
		try {
			// ()
			this.method.invoke(this.container);
		} catch (IllegalArgumentException ignore1) {
			try {
				// (log)
				this.method.invoke(this.container, log);
			} catch (IllegalArgumentException ignore2) {
				try {
					// (app)
					this.method.invoke(this.container, this.app);
				} catch (IllegalArgumentException ignore3) {
					// (app, log)
					this.method.invoke(this.container, this.app, log);
				}
			}
		} catch (Exception e) {
			Failure.fail(
				e,
				StringUtils.ReplaceTags(
					"Exception in {} step {}",
					(this.stepValue > 0 ? "startup" : "shutdown"),
					this.stepValue
				)
			);
		} finally {
			currentThread.setName(originalThreadName);
		}
	}
	@Override
	public void run() {
		if (Failure.hasFailed()) return;
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
