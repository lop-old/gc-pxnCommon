package com.poixson.commonapp.gui.remapped;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.xLogger.xLog;


public class RemappedActionListener implements ActionListener {

	protected final Object obj;
	protected final Method method;



	public static RemappedActionListener get(final Object listenerClass, final String methodName) {
		try {
			return new RemappedActionListener(listenerClass, methodName);
		} catch (NoSuchMethodException e) {
xLog.getRoot().trace(e);
		}
		throw new RuntimeException();
	}
	public RemappedActionListener(final Object listenerClass, final String methodName)
			throws NoSuchMethodException {
		if(listenerClass == null)     throw new NullPointerException("listenerClass argument is required!");
		if(utils.isEmpty(methodName)) throw new NullPointerException("methodName argument is required!");
		this.obj = listenerClass;
		final Class<?> clss = listenerClass.getClass();
		this.method = clss.getMethod(methodName, ActionEvent.class);
		if(this.method == null) {
xLog.getRoot().severe("Method: "+methodName+"() in class: "+listenerClass.getClass().getName());
			throw new NoSuchMethodException();
		}
xLog.getRoot().finest("New ActionListener created for: "+clss.getName()+"::"+methodName+"()");
	}



	@Override
	public void actionPerformed(final ActionEvent event) {
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
