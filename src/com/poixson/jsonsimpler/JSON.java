/*
package com.poixson.jsonsimpler;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.poixson.commonjava.Utils.utilsObject;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.byRef.IntRef;
import com.poixson.commonjava.xLogger.xLog;


public interface JSON {
	static final String NEWLINE    = "\n";
	static final String INDENT     = "  ";
	static final String NULL_VALUE = "null";



	public void load(final String data);



	public static String ToString(final Object value,
			final IntRef indent, final boolean first) {
		final StringWriter out = new StringWriter();
		try {
			WriteString(out, value, indent);
		} catch (IOException e) {
			xLog.getRoot().trace(e);
			throw new RuntimeException(e);
		}
		return out.toString();
	}
	static void WriteString(final Writer out, final Object value,
			final IntRef indent) throws IOException {
		WriteString(
				out,
				value,
				indent,
				false
		);
	}
	static void WriteString(final Writer out, final Object value,
			final IntRef indent, final boolean writeIndent) throws IOException {
		// null
		if(value == null) {
			if(writeIndent)
				WriteIndent(out, indent);
			out.write("null");
			return;
		}
		// String
		if(value instanceof String) {
			if(writeIndent)
				WriteIndent(out, indent);
			out.write('\"');
			out.write(value.toString());
			out.write('\"');
			return;
		}
		// Byte
		if(value instanceof Byte) {
			if(writeIndent)
				WriteIndent(out, indent);
			out.write(value.toString());
			return;
		}
		// Short
		if(value instanceof Short) {
			if(writeIndent)
				WriteIndent(out, indent);
			out.write(value.toString());
			return;
		}
		// Integer
		if(value instanceof Integer) {
			if(writeIndent)
				WriteIndent(out, indent);
			out.write(value.toString());
			return;
		}
		// Long
		if(value instanceof Long) {
			if(writeIndent)
				WriteIndent(out, indent);
			out.write(value.toString());
			return;
		}
		// Double
		if(value instanceof Double) {
			final Double d = (Double) value;
			if( d.isInfinite() || d.isNaN()) {
				out.write(NULL_VALUE);
				return;
			}
			if(writeIndent)
				WriteIndent(out, indent);
			out.write(value.toString());
			return;
		}
		// Float
		if(value instanceof Float) {
			final Float f = (Float) value;
			if(f.isInfinite() || f.isNaN()) {
				out.write(NULL_VALUE);
				return;
			}
			if(writeIndent)
				WriteIndent(out, indent);
			out.write(value.toString());
			return;
		}
		// Boolean
		if(value instanceof Boolean) {
			if(writeIndent)
				WriteIndent(out, indent);
			out.write(value.toString());
			return;
		}
		// Character
		if(value instanceof Character) {
			if(writeIndent)
				WriteIndent(out, indent);
			out.write(value.toString());
			return;
		}
		// Number
		if(value instanceof Number) {
			if(writeIndent)
				WriteIndent(out, indent);
			out.write(value.toString());
			return;
		}
		// List
		if(value instanceof List) {
			final List<Object> list = utilsObject.castList(
					Object.class,
					value
			);
			final jsonArray array = new jsonArray();
			array.addAll(list);
			array.indent = indent;
			array.writeString(out, writeIndent);
			return;
		}
		// Set
		if(value instanceof Collection) {
			final Set<Object> set = utilsObject.castSet(
					Object.class,
					value
			);
			final jsonArray array = new jsonArray();
			array.addAll(set);
			array.indent = indent;
			array.writeString(out, writeIndent);
			return;
		}
		// Map
		if(value instanceof Map) {
			final Map<String, Object> m = utilsObject.castMap(
					String.class,
					Object.class,
					value
			);
			final jsonMap map = new jsonMap();
			map.putAll(m);
			map.indent = indent;
			map.writeString(out, writeIndent);
			return;
		}
		// byte[]
		if(value instanceof byte[]) {
			final jsonArray array = new jsonArray();
			for(final byte b : ((byte[]) value) )
				array.add(b);
			array.indent = indent;
			array.writeString(out, writeIndent);
			return;
		}
		// short[]
		if(value instanceof short[]) {
			final jsonArray array = new jsonArray();
			for(final short s : ((short[]) value) )
				array.add(s);
			array.indent = indent;
			array.writeString(out, writeIndent);
			return;
		}
		// int[]
		if(value instanceof int[]) {
			final jsonArray array = new jsonArray();
			for(final int i : ((int[]) value) )
				array.add(i);
			array.indent = indent;
			array.writeString(out, writeIndent);
			return;
		}
		// long[]
		if(value instanceof long[]) {
			final jsonArray array = new jsonArray();
			for(final long l : ((long[]) value) )
				array.add(l);
			array.indent = indent;
			array.writeString(out, writeIndent);
			return;
		}
		// double[]
		if(value instanceof double[]) {
			final jsonArray array = new jsonArray();
			for(final double d : ((double[]) value) )
				array.add(d);
			array.indent = indent;
			array.writeString(out, writeIndent);
			return;
		}
		// float[]
		if(value instanceof float[]) {
			final jsonArray array = new jsonArray();
			for(final float f : ((float[]) value) )
				array.add(f);
			array.indent = indent;
			array.writeString(out, writeIndent);
			return;
		}
		// boolean[]
		if(value instanceof boolean[]) {
			final jsonArray array = new jsonArray();
			for(final boolean b : ((boolean[]) value) )
				array.add(b);
			array.indent = indent;
			array.writeString(out, writeIndent);
			return;
		}
		// char[]
		if(value instanceof char[]) {
			final jsonArray array = new jsonArray();
			for(final char c : ((char[]) value) )
				array.add(c);
			array.indent = indent;
			array.writeString(out, writeIndent);
			return;
		}
		// Object[]
		if(value instanceof Object[]) {
			final jsonArray array = new jsonArray();
			for(final Object o : ((Object[]) value) )
				array.add(o);
			array.indent = indent;
			array.writeString(out, writeIndent);
			return;
		}
		// unknown type
		if(writeIndent)
			WriteIndent(out, indent);
		out.write(value.toString());
	}



	static void WriteIndent(final Writer out, final IntRef indent) throws IOException {
		if(indent == null) return;
		if(out    == null) throw new NullPointerException("out argument is required!");
		if(indent.value < 0) indent.value(0);
		//out.write("<"+Integer.toString(indent.value)+">");
		out.write(utilsString.repeat(indent.value, INDENT));
	}



}
*/