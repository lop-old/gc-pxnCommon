package com.poixson.tools.events;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.utils.Utils;


public abstract class xHandler <T extends Annotation> {

	protected final Class<T> type;



	public xHandler(final Class<T> type) {
		if (type == null) throw new RequiredArgumentException("type");
		this.type = type;
	}



	public int register(final Object...objects) {
		if (Utils.isEmpty(objects)) return -1;
		int count = 0;
		OBJECTS_LOOP:
		for (final Object obj : objects) {
			if (obj == null) continue OBJECTS_LOOP;
			count += this.registerObject(obj);
		}
		return count;
	}
	protected int registerObject(final Object object) {
		if (object == null) return -1;
		final Method[] methods = object.getClass().getMethods();
		if (Utils.isEmpty(methods)) return -1;
		int count = 0;
		METHODS_LOOP:
		for (final Method m : methods) {
			if (m == null) continue METHODS_LOOP;
			final T anno = m.getAnnotation(this.type);
			if (anno == null) continue METHODS_LOOP;
			// found annotation
			if (this.registerMethod(object, m, anno)) {
				count++;
			}
		}
		return count;
	}
	protected abstract boolean registerMethod(
			final Object object, final Method method, final T anno);



	public abstract void unregisterObject(final Object object);
	public abstract void unregisterMethod(final Object object, final String methodName);
	public abstract void unregisterAll();



}
