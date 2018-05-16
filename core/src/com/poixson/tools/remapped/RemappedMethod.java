package com.poixson.tools.remapped;

import java.lang.reflect.Method;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.utils.ReflectUtils;
import com.poixson.utils.Utils;


public class RemappedMethod<V> extends xRunnable {

	public final Object container;
	public final Method method;
	public final Object[] args;

	protected volatile V result = null;
	protected volatile boolean done = false;



	public RemappedMethod(final Object container,
			final String methodName, final Object...args) {
		this(
			null,
			container,
			ReflectUtils.getMethodByName(container, methodName, args),
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
			ReflectUtils.getMethodByName(container, methodName, args),
			args
		);
	}
	public RemappedMethod(final String taskName, final Object container,
			final Method method, final Object...args) {
		if (container == null) throw new RequiredArgumentException("container");
		if (method == null)    throw new RequiredArgumentException("method");
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
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			this.result = (V)
				ReflectUtils.InvokeMethod(
					this.container,
					this.method,
					this.args
				);
		} finally {
			this.done = true;
		}
	}



	public V getResult() {
		if ( ! this.done )
			return null;
		return this.result;
	}
	public boolean isDone() {
		return this.done;
	}



}
