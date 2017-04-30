package com.poixson.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public final class ReflectUtils {
	private ReflectUtils() {}



	public static void init() {
		Keeper.add(new ReflectUtils());
	}



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
//TODO: remove this if not needed
//			switch (args.length){
//			case 0:
//				return clss.getMethod(
//					methodName
//				);
//			case 1:
//				return clss.getMethod(
//					methodName,
//					args[0].getClass()
//				);
//			case 2:
//				return clss.getMethod(
//					methodName,
//					args[0].getClass(), args[1].getClass()
//				);
//			case 3:
//				return clss.getMethod(
//					methodName,
//					args[0].getClass(), args[1].getClass(), args[2].getClass()
//				);
//			case 4:
//				return clss.getMethod(
//					methodName,
//					args[0].getClass(), args[1].getClass(),
//					args[2].getClass(), args[3].getClass()
//				);
//			case 5:
//				return clss.getMethod(
//					methodName,
//					args[0].getClass(), args[1].getClass(), args[2].getClass(),
//					args[3].getClass(), args[4].getClass()
//				);
//			case 6:
//				return clss.getMethod(
//					methodName,
//					args[0].getClass(), args[1].getClass(), args[2].getClass(),
//					args[3].getClass(), args[4].getClass(), args[5].getClass()
//				);
//			default:
//				throw new IllegalArgumentException("Too many arguments");
//			}
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
					(Object[]) ArgsToClasses(args)
			);
//TODO: remove this if not needed
//			switch (args.length) {
//			case 0:
//				return method.invoke(
//						container
//				);
//			case 1:
//				return method.invoke(
//						container,
//						args[0]
//				);
//			case 2:
//				return method.invoke(
//						container,
//						args[0], args[1]
//				);
//			case 3:
//				return method.invoke(
//						container,
//						args[0], args[1], args[2]
//				);
//			case 4:
//				return method.invoke(
//						container,
//						args[0], args[1],
//						args[2], args[3]
//				);
//			case 5:
//				return method.invoke(
//						container,
//						args[0], args[1], args[2],
//						args[3], args[4]
//				);
//			case 6:
//				return method.invoke(
//						container,
//						args[0], args[1], args[2],
//						args[3], args[4], args[5]
//				);
//			default:
//				throw new IllegalArgumentException("Too many arguments");
//			}
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
		if (clss == null)        throw new NullPointerException();
		if (Utils.isEmpty(name)) throw new NullPointerException();
		final Field field;
		final String value;
		try {
			field = clss.getField(name);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new IllegalArgumentException("Invalid field: "+name, e);
		}
		try {
			value = (String) field.get(String.class);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalArgumentException("Failed to get field: "+name, e);
		}
		return value;
	}



}
