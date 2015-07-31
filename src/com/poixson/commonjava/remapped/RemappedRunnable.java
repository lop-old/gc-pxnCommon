package com.poixson.commonjava.remapped;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.xLogger.xLog;


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
			xLog.getRoot().trace(e);
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
		if(targetClass == null)       throw new NullPointerException("targetClass argument is required!");
		if(utils.isEmpty(methodName)) throw new NullPointerException("methodName argument is required!");
		this.setTaskName( utils.isEmpty(taskName) ? methodName : taskName );
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
			xLog.getRoot().trace(e);
		} catch (IllegalArgumentException e) {
			xLog.getRoot().trace(e);
		} catch (InvocationTargetException e) {
			xLog.getRoot().trace(e);
		} catch (Exception e) {
			xLog.getRoot().trace(e);
		}
	}



}
