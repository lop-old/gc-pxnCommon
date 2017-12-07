package com.poixson.app.gui.remapped;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.exceptions.RequiredArgumentException;
import com.poixson.commonjava.xLogger.xLog;


public class RemappedItemListener implements ItemListener {

	protected final Object obj;
	protected final Method method;



	public static RemappedItemListener get(final Object listenerClass, final String methodName) {
		try {
			return new RemappedItemListener(listenerClass, methodName);
		} catch (NoSuchMethodException e) {
xLog.getRoot().trace(e);
		}
		throw new RuntimeException();
	}
	public RemappedItemListener(final Object listenerClass, final String methodName)
			throws NoSuchMethodException {
		if(listenerClass == null)     throw new RequiredArgumentException("listenerClass");
		if(utils.isEmpty(methodName)) throw new RequiredArgumentException("methodName");
		this.obj = listenerClass;
		final Class<?> clss = listenerClass.getClass();
		this.method = clss.getMethod(methodName, ItemEvent.class);
		if(this.method == null) {
xLog.getRoot().severe("Method: "+methodName+"() in class: "+listenerClass.getClass().getName());
			throw new NoSuchMethodException();
		}
xLog.getRoot().finest("New ItemListener created for: "+clss.getName()+"::"+methodName+"()");
	}



	@Override
	public void itemStateChanged(final ItemEvent event) {
		try {
			this.method.invoke(this.obj, event);
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
