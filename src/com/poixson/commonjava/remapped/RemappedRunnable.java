package com.poixson.commonjava.remapped;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.xLogger.xLog;


public class RemappedRunnable implements Runnable {

	protected final Object obj;
	protected final Method method;



	public static Thread Thread(final Object targetClass, final String methodName) {
		try {
			return new Thread(
				new RemappedRunnable(targetClass, methodName)
			);
		} catch (NoSuchMethodException e) {
			xLog.getRoot().trace(e);
		} catch (SecurityException e) {
			xLog.getRoot().trace(e);
		}
		return null;
	}
	public RemappedRunnable(final Object targetClass, final String methodName)
			throws NoSuchMethodException, SecurityException {
		if(targetClass == null)       throw new NullPointerException();
		if(utils.isEmpty(methodName)) throw new NullPointerException();
		this.obj = targetClass;
		final Class<?> clss = targetClass.getClass();
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
