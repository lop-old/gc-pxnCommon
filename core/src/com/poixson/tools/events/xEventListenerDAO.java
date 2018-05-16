package com.poixson.tools.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.xLogRoot;
import com.poixson.threadpool.types.xThreadPool_Main;
import com.poixson.utils.Utils;


public class xEventListenerDAO {

	private static final AtomicLong nextIndex = new AtomicLong(0);
	public final long index;

	protected final Object object;
	protected final Method method;



	public xEventListenerDAO(final Object object, final Method method) {
		if (object == null) throw new RequiredArgumentException("object");
		if (method == null) throw new RequiredArgumentException("method");
		this.index = nextIndex.incrementAndGet();
		this.object = object;
		this.method = method;
	}



	public void invoke() {
		// ensure main thread
		if (xThreadPool_Main.get().force(this, "invoke"))
			return;
		xLogRoot.get()
			.finest(
				"Invoking event: {}->{}",
				this.object.getClass().getName(),
				this.method.getName()
			);
		// method(object)
		try {
			this.method.invoke(this.object);
			return;
		} catch (IllegalAccessException ignore) {
		} catch (IllegalArgumentException ignore) {
		} catch (InvocationTargetException ignore) {}
		// method with arguments not found/supported
		throw new RuntimeException(
			(new StringBuilder())
				.append("Method arguments not supported: ")
				.append(this.method.getName())
				.toString()
		);
	}



	public long getIndex() {
		return this.index;
	}



	public boolean isObject(final Object object) {
		if (object == null) return false;
		return object.equals(this.object);
	}
	public boolean isMethod(final Object object, final String methodName) {
		if (object == null) return false;
		if (Utils.isEmpty(methodName)) return false;
		if ( ! object.equals(this.object) )
			return false;
		if ( ! methodName.equals(this.method.getName()) )
			return false;
		return true;
	}



}
