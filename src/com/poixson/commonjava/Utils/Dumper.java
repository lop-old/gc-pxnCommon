package com.poixson.commonjava.Utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


public class Dumper {

	private static volatile Dumper instance = null;
	private static final Object lock = new Object();



	protected static Dumper get() {
		if(instance == null) {
			synchronized(lock) {
				if(instance == null)
					instance = new Dumper();
			}
		}
		return instance;
	}



	class DumpContext {
		int maxDepth = 0;
		int maxArrayElements = 0;
		int callCount = 0;
		final Map<String, String> ignoreList = new HashMap<String, String>();
		final Map<Object, Integer> visited = new HashMap<Object, Integer>();
	}



	public static String dump(final Object obj) {
		return dump(obj, 0, 0, null);
	}



	public static String dump(final Object obj, final int maxDepth, final int maxArrayElements, final String[] ignoreList) {
		final DumpContext ctx = Dumper.get().new DumpContext();
		ctx.maxDepth = maxDepth;
		ctx.maxArrayElements = maxArrayElements;
		if(ignoreList != null) {
			for(int i = 0; i < Array.getLength(ignoreList); i++) {
				final int colonIdx = ignoreList[i].indexOf(':');
				if(colonIdx == -1)
					ignoreList[i] = ignoreList[i] + ":";
				ctx.ignoreList.put(ignoreList[i], ignoreList[i]);
			}
		}
		return dump(obj, ctx);
	}



	protected static String dump(final Object obj, final DumpContext ctx) {
		if(obj == null)
			return "<null>";
		ctx.callCount++;
		final StringBuffer tabs = new StringBuffer();
		for(int k = 0; k < ctx.callCount; k++)
			tabs.append("\t");
		final StringBuffer buffer = new StringBuffer();
		Class<?> objClass = obj.getClass();
		String objSimpleName = getSimpleNameWithoutArrayQualifier(objClass);
		if(ctx.ignoreList.get(objSimpleName + ":") != null)
			return "<Ignored>";
		if(objClass.isArray()) {
			buffer.append("\n");
			buffer.append(tabs.toString().substring(1));
			buffer.append("[\n");
			final int rowCount;
			if(ctx.maxArrayElements == 0)
				rowCount = Array.getLength(obj);
			else
				rowCount = Math.min(ctx.maxArrayElements, Array.getLength(obj));
			for(int i = 0; i < rowCount; i++) {
				buffer.append(tabs.toString());
				try {
					Object value = Array.get(obj, i);
					buffer.append(dumpValue(value, ctx));
				} catch (Exception e) {
					buffer.append(e.getMessage());
				}
				if(i < Array.getLength(obj) - 1)
					buffer.append(",");
				buffer.append("\n");
			}
			if(rowCount < Array.getLength(obj)) {
				buffer.append(tabs.toString());
				buffer.append(Array.getLength(obj) - rowCount + " more array elements...");
				buffer.append("\n");
			}
			buffer.append(tabs.toString().substring(1));
			buffer.append("]");
		} else {
			buffer.append("\n");
			buffer.append(tabs.toString().substring(1));
			buffer.append("{\n");
			buffer.append(tabs.toString());
			buffer.append("hashCode: " + obj.hashCode());
			buffer.append("\n");
			while(objClass != null && objClass != Object.class) {
				final Field[] fields = objClass.getDeclaredFields();
				if(ctx.ignoreList.get(objClass.getSimpleName()) == null) {
					if(objClass != obj.getClass()) {
						buffer.append(tabs.toString().substring(1));
						buffer.append("  Inherited from superclass " + objSimpleName + ":\n");
					}
					for(int i = 0; i < fields.length; i++) {
						final String fSimpleName = getSimpleNameWithoutArrayQualifier(fields[i].getType());
						final String fName = fields[i].getName();
						fields[i].setAccessible(true);
						buffer.append(tabs.toString());
						buffer.append(fName).append("(").append(fSimpleName).append(")");
						buffer.append("=");
						if(ctx.ignoreList.get(":" + fName) == null &&
								ctx.ignoreList.get(fSimpleName + ":" + fName) == null &&
								ctx.ignoreList.get(fSimpleName + ":") == null) {
							try {
								final Object value = fields[i].get(obj);
								buffer.append(dumpValue(value, ctx));
							} catch (Exception e) {
								buffer.append(e.getMessage());
							}
							buffer.append("\n");
						} else {
							buffer.append("<Ignored>");
							buffer.append("\n");
						}
					}
					objClass = objClass.getSuperclass();
					objSimpleName = objClass.getSimpleName();
				} else {
					objClass = null;
					objSimpleName = "";
				}
			}
			buffer.append(tabs.toString().substring(1));
			buffer.append("}");
		}
		ctx.callCount--;
		return buffer.toString();
	}



	protected static String dumpValue(final Object value, final DumpContext ctx) {
		if(value == null)
			return "<null>";
		if(value.getClass().isPrimitive() ||
				value.getClass() == java.lang.Short.class ||
				value.getClass() == java.lang.Long.class ||
				value.getClass() == java.lang.String.class ||
				value.getClass() == java.lang.Integer.class ||
				value.getClass() == java.lang.Float.class ||
				value.getClass() == java.lang.Byte.class ||
				value.getClass() == java.lang.Character.class ||
				value.getClass() == java.lang.Double.class ||
				value.getClass() == java.lang.Boolean.class ||
				value.getClass() == java.util.Date.class ||
				value.getClass().isEnum()) {
			return value.toString();
		} else {
			final Integer visitedIndex = ctx.visited.get(value);
			if(visitedIndex == null) {
				ctx.visited.put(value, ctx.callCount);
				if(ctx.maxDepth == 0 || ctx.callCount < ctx.maxDepth)
					return dump(value, ctx);
				else
					return "<Reached max recursion depth>";
			} else {
				return "<Previously visited - see hashCode " + value.hashCode() + ">";
			}
		}
	}



	private static String getSimpleNameWithoutArrayQualifier(final Class<?> clazz) {
		final String simpleName = clazz.getSimpleName();
		final int indexOfBracket = simpleName.indexOf('[');
		if(indexOfBracket != -1)
			return simpleName.substring(0, indexOfBracket);
		return simpleName;
	}



}
