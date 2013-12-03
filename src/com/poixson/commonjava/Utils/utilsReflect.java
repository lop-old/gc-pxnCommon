package com.poixson.commonjava.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.xLogger.xLog;


public final class utilsReflect {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	private utilsReflect() {}


	// utilsReflect.invoke(new TestClass(), "func", "arg");
	public static Object invoke(Object clss, String methodName, Object... params) {
		Object object = null;
		Method method;
		try {
			method = clss.getClass().getMethod(methodName, getParemeterClasses(params));
			object = method.invoke(clss, params);
		} catch (NoSuchMethodException | SecurityException
				| InvocationTargetException | IllegalAccessException e) {
			log().trace(e);
		}
		return object;
	}
	@SuppressWarnings("rawtypes")
	private static Class[] getParemeterClasses(Object... params) {
		Class[] classes = new Class[params.length];
		for(int i = 0; i < classes.length; i++)
			classes[i] = params[i].getClass();
		return classes;
	}


	// logger
	public static xLog log() {
		return xVars.getLog();
	}


}
