package com.poixson.utils.remapped;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.poixson.utils.Utils;
import com.poixson.utils.xRunnable;
import com.poixson.utils.exceptions.RequiredArgumentException;


public class RemappedRunnable extends xRunnable {

	protected final Object obj;
	protected final Method method;



	public static RemappedRunnable get(final Object targetClass, final String methodName) {
		return get(null, targetClass, methodName);
	}
	public static RemappedRunnable get(final String taskName,
			final Object targetClass, final String methodName) {
		try {
			return new RemappedRunnable(
					taskName,
					targetClass,
					methodName
			);
		} catch (Exception e) {
			log().trace(e);
		}
		return null;
	}



	public RemappedRunnable(final Object targetClass, final String methodName)
			throws NoSuchMethodException, SecurityException {
		this(null, targetClass, methodName);
	}
	public RemappedRunnable(final String taskName,
			final Object targetClass, final String methodName)
			throws NoSuchMethodException, SecurityException {
		if(targetClass == null)       throw new RequiredArgumentException("targetClass");
		if(Utils.isEmpty(methodName)) throw new RequiredArgumentException("methodName");
		this.setTaskName( Utils.isEmpty(taskName) ? methodName : taskName );
		this.obj = targetClass;
		// static or instance class
		final Class<?> clss =
				(targetClass instanceof Class)
				? (Class<?>) targetClass
				: targetClass.getClass();
		if(clss == null) throw new RuntimeException();
		// find method to call
		this.method = clss.getMethod(methodName);
		if(this.method == null) throw new NoSuchMethodException();
	}



	@Override
	public void run() {
		try {
			this.method.invoke(this.obj);
		} catch (IllegalAccessException e) {
			log().trace(e);
		} catch (IllegalArgumentException e) {
			log().trace(e);
		} catch (InvocationTargetException e) {
			log().trace(e);
		} catch (Exception e) {
			log().trace(e);
		}
	}



	// logger
	public static xLog log() {
		return xLog.getRoot();
	}



}
