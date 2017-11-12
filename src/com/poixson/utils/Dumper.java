package com.poixson.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


public class Dumper {
	private Dumper() {}



	protected static class DumpContext {
		int maxDepth         = 0;
		int maxArrayElements = 0;
		int callCount        = 0;
		final Map<String, String> ignoreList = new HashMap<String, String>();
		final Map<Object, Integer> visited = new HashMap<Object, Integer>();
	}



	public static void print(final Object obj) {
		System.out.println(
			dump(obj)
		);
	}
	protected static String dump(final Object obj) {
		return dump(
			obj,
			0,
			0,
			null
		);
	}



	protected static String dump(final Object obj, final int maxDepth,
			final int maxArrayElements, final String[] ignoreList) {
		final DumpContext ctx = new DumpContext();
		ctx.maxDepth = maxDepth;
		ctx.maxArrayElements = maxArrayElements;
		if (ignoreList != null) {
			for (int i = 0; i < Array.getLength(ignoreList); i++) {
				final int colonIdx = ignoreList[i].indexOf(':');
				if (colonIdx == -1) {
					ignoreList[i] = ignoreList[i] + ":";
				}
				ctx.ignoreList.put(ignoreList[i], ignoreList[i]);
			}
		}
		return dump(obj, ctx);
	}



	protected static String dump(final Object obj, final DumpContext ctx) {
		if (obj == null) {
			return "<null>";
		}
		ctx.callCount++;
		final StringBuilder tabs = new StringBuilder();
		for (int k = 0; k < ctx.callCount; k++) {
			tabs.append("\t");
		}
		final StringBuilder buffer = new StringBuilder();
		Class<?> objClass = obj.getClass();
		String objSimpleName = getSimpleNameWithoutArrayQualifier(objClass);
		if (ctx.ignoreList.get(objSimpleName + ":") != null) {
			return "<Ignored>";
		}
		if (String.class.equals(objClass)) {
			buffer
				.append("(String): \n")
				.append( (String)obj );
		} else
		if (objClass.isArray()) {
			buffer
				.append( "\n"                         )
				.append( tabs.toString().substring(1) )
				.append( "[\n"                        );
			final int rowCount =
				ctx.maxArrayElements == 0
				? Array.getLength(obj)
				: Math.min(ctx.maxArrayElements, Array.getLength(obj));
			for (int i = 0; i < rowCount; i++) {
				buffer.append(tabs.toString());
				try {
					Object value = Array.get(obj, i);
					buffer.append(dumpValue(value, ctx));
				} catch (Exception e) {
					buffer.append(e.getMessage());
				}
				if (i < Array.getLength(obj) - 1) {
					buffer.append(",");
				}
				buffer.append("\n");
			}
			if (rowCount < Array.getLength(obj)) {
				final int rowCountMore = Array.getLength(obj) - rowCount;
				buffer
					.append( tabs.toString()             )
					.append( rowCountMore                )
					.append( " more array elements...\n" );
			}
			buffer
				.append( tabs.toString().substring(1) )
				.append( "]"                          );
		} else {
			buffer
				.append( "\n"                         )
				.append( tabs.toString().substring(1) )
				.append( "{\n"                        )
				.append( tabs.toString()              )
				.append( "hashCode: "                 )
				.append( obj.hashCode()               )
				.append( "\n"                         );
			while (objClass != null && objClass != Object.class) {
				final Field[] fields = objClass.getDeclaredFields();
				if (ctx.ignoreList.get(objClass.getSimpleName()) == null) {
					if (objClass != obj.getClass()) {
						buffer
							.append( tabs.toString().substring(1)   )
							.append( "  Inherited from superclass " )
							.append( objSimpleName                  )
							.append( ":\n"                          );
					}
					for (int i = 0; i < fields.length; i++) {
						final String fSimpleName = getSimpleNameWithoutArrayQualifier(fields[i].getType());
						final String fName = fields[i].getName();
						fields[i].setAccessible(true);
						buffer
							.append( tabs.toString() )
							.append( fName           )
							.append( "("             )
							.append( fSimpleName     )
							.append( ")="            );
						if (ctx.ignoreList.get(":" + fName) == null
								&& ctx.ignoreList.get(fSimpleName + ":" + fName) == null
								&& ctx.ignoreList.get(fSimpleName + ":") == null) {
							try {
								final Object value = fields[i].get(obj);
								buffer.append(dumpValue(value, ctx));
							} catch (Exception e) {
								buffer.append(e.getMessage());
							}
							buffer.append("\n");
						} else {
							buffer.append("<Ignored>\n");
						}
					}
					objClass = objClass.getSuperclass();
					objSimpleName = objClass.getSimpleName();
				} else {
					objClass = null;
					objSimpleName = "";
				}
			}
			buffer
				.append( tabs.toString().substring(1) )
				.append( "}"                          );
		}
		ctx.callCount--;
		return buffer.toString().trim();
	}



	protected static String dumpValue(final Object value, final DumpContext ctx) {
		if (value == null) {
			return "<null>";
		}
		if (value.getClass().isPrimitive()
				|| value.getClass() == java.lang.Short.class
				|| value.getClass() == java.lang.Long.class
				|| value.getClass() == java.lang.String.class
				|| value.getClass() == java.lang.Integer.class
				|| value.getClass() == java.lang.Float.class
				|| value.getClass() == java.lang.Byte.class
				|| value.getClass() == java.lang.Character.class
				|| value.getClass() == java.lang.Double.class
				|| value.getClass() == java.lang.Boolean.class
				|| value.getClass() == java.util.Date.class
				|| value.getClass().isEnum()) {
			return value.toString();
		}
		final Integer visitedIndex = ctx.visited.get(value);
		if (visitedIndex == null) {
			ctx.visited.put(value, Integer.valueOf(ctx.callCount));
			if (ctx.maxDepth == 0 || ctx.callCount < ctx.maxDepth) {
				return dump(value, ctx);
			}
			return "<Reached max recursion depth>";
		}
		return "<Previously visited - see hashCode " + value.hashCode() + ">";
	}



	private static String getSimpleNameWithoutArrayQualifier(final Class<?> clazz) {
		final String simpleName = clazz.getSimpleName();
		final int indexOfBracket = simpleName.indexOf('[');
		if (indexOfBracket != -1) {
			return simpleName.substring(0, indexOfBracket);
		}
		return simpleName;
	}



}
