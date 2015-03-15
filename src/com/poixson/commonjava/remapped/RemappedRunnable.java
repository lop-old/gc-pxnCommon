package com.poixson.commonjava.remapped;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.xLogger.xLog;


public class RemappedRunnable implements Runnable {

	protected final Object obj;
	protected final Method method;



	public static RemappedRunnable get(final Object targetClass, final String methodName) {
		try {
			final RemappedRunnable mapped =
					new RemappedRunnable(targetClass, methodName);
			return mapped;
		} catch (Exception e) {
			xLog.getRoot().trace(e);
		}
		return null;
	}
	public static xRunnable get(final String taskName,
			final Object targetClass, final String methodName) {
		return xRunnable.cast(
				taskName,
				RemappedRunnable.get(targetClass, methodName)
		);
	}
	public static Thread getThread(final Object targetClass, final String methodName) {
		return new Thread(
			get(targetClass, methodName)
		);
	}
	public RemappedRunnable(final Object targetClass, final String methodName)
			throws NoSuchMethodException, SecurityException {
		if(targetClass == null)       throw new NullPointerException();
		if(utils.isEmpty(methodName)) throw new NullPointerException();
		this.obj = targetClass;
		final Class<?> clss = targetClass.getClass();
		// find method to call
		this.method = clss.getMethod(methodName);
		if(this.method == null)
			throw new NoSuchMethodException();
		xLog.getRoot().finest("New Runnable created for: "+clss.getName()+"::"+methodName+"()");
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
