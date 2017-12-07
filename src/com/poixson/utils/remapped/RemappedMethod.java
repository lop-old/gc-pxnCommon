package com.poixson.utils.remapped;

import java.lang.reflect.Method;

import com.poixson.utils.ReflectUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.xRunnable;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLog;


public class RemappedMethod extends xRunnable {

	public final Object container;
	public final Method method;
	public final Object[] args;



	public RemappedMethod(final Object container,
			final String methodName, final Object...args) {
		this(
			null,
			container,
			ReflectUtils.getMethodByName(container, methodName),
			args
		);
	}
	public RemappedMethod(final Object container,
			final Method methodName, final Object...args) {
		this(
			null,
			container,
			methodName,
			args
		);
	}
	public RemappedMethod(final String taskName, final Object container,
			final String methodName, final Object...args) {
		this(
			taskName,
			container,
			ReflectUtils.getMethodByName(container, methodName),
			args
		);
	}
	public RemappedMethod(final String taskName, final Object container,
			final Method method, final Object...args) {
		if (container == null) throw RequiredArgumentException.getNew("container");
		if (method == null)    throw RequiredArgumentException.getNew("method");
		this.container = container;
		this.method    = method;
		this.args      = args;
		// static or instance class
		this.setTaskName(
			Utils.isEmpty(taskName)
			? method.getName()
			: taskName
		);
	}



	// invoke stored method
	@Override
	public void run() {
		ReflectUtils.InvokeMethod(
			this.container,
			this.method,
			this.args
		);
	}



	// logger
	public static xLog log() {
		return Utils.log();
	}



}
