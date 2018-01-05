package com.poixson.app.steps;

import java.lang.reflect.Method;

import com.poixson.app.xApp;
import com.poixson.app.steps.xAppStep.StepType;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.exceptions.RequiredArgumentException;


public class xAppStepDAO {

	public final StepType type;
	public final int      priority;
	public final String   name;
	public final String   title;

	public final xApp     app;
	public final Method   method;
	public final xAppStep anno;



	public xAppStepDAO(final xApp app, final Method method, final xAppStep anno) {
		if (app    == null) throw RequiredArgumentException.getNew("app");
		if (method == null) throw RequiredArgumentException.getNew("method");
		if (anno   == null) throw RequiredArgumentException.getNew("annotation");
		this.type = anno.type();
		this.priority = Math.abs(anno.priority());
		this.app    = app;
		this.method = method;
		this.anno   = anno;
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
	public boolean isPriority(final byte priority) {
		return (this.priority == priority);
	}



	public void invoke() throws ReflectiveOperationException, RuntimeException {
		this.method.invoke(this.app);
	}



}
