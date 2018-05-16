package com.poixson.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.poixson.exceptions.RequiredArgumentException;


public final class ReflectUtils {
	private ReflectUtils() {}



	public static String getClassName(final Class<?> clss) {
		return clss.getSimpleName();
	}



	public static Method getMethodByName(final Object container,
			final String methodName, final Object...args) {
		if (container == null)         throw new IllegalArgumentException("container");
		if (Utils.isEmpty(methodName)) throw new IllegalArgumentException("methodName");
		final Class<?> clss = (
			container instanceof Class
			? (Class<?>) container
			: container.getClass()
		);
		if (clss == null) {
			return null;
		}
		try {
			return clss.getMethod(
					methodName,
					ArgsToClasses(args)
			);
		} catch (NoSuchMethodException
				| SecurityException e) {
			throw new IllegalArgumentException("Invalid method: "+methodName, e);
		}
	}
	public static Object InvokeMethod(final Object container,
			final String methodName, final Object...args) {
		return InvokeMethod(
			container,
			getMethodByName(container, methodName, args),
			args
		);
	}
	public static Object InvokeMethod(final Object container,
			final Method method, final Object...args) {
		if (container == null) throw new IllegalArgumentException("container");
		if (method == null)    throw new IllegalArgumentException("method");
		try {
			return method.invoke(
					container,
					args
			);
		} catch (IllegalAccessException
				| IllegalArgumentException
				| InvocationTargetException e) {
			throw new IllegalArgumentException("Failed to call method: "+method.getName(), e);
		}
	}



	public static Class<?>[] ArgsToClasses(final Object...args) {
		Class<?>[] classes = new Class[args.length];
		for (int i=0; i<args.length; i++) {
			classes[i] = args[i].getClass();
		}
		return classes;
	}



	public static String getStaticString(final Class<?> clss, final String name) {
		if (clss == null)        throw new RequiredArgumentException("clss");
		if (Utils.isEmpty(name)) throw new RequiredArgumentException("name");
		final Field field;
		final String value;
		try {
			field = clss.getField(name);
			final Object o = field.get(null);
			value = (String) o;
		} catch (NoSuchFieldException | SecurityException e) {
			throw new IllegalArgumentException("Invalid field: "+name, e);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalArgumentException("Failed to get field: "+name, e);
		}
		return value;
	}



}
