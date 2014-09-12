package com.poixson.commonapp.gui.remapped;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.poixson.commonjava.xLogger.xLog;


public class RemappedActionListener implements ActionListener {

	protected final Object obj;
	protected final Method method;



	public RemappedActionListener(final Object listenerClass, final String methodName)
			throws NoSuchMethodException {
		this.obj = listenerClass;
		final Class<?> clss = listenerClass.getClass();
		this.method = clss.getMethod(methodName, ActionEvent.class);
		if(this.method == null)
			throw new NullPointerException();
		xLog.getRoot().finest("New ActionListener created for: "+clss.getName()+"::"+methodName+"()");
	}



	@Override
	public void actionPerformed(final ActionEvent event) {
		try {
			this.method.invoke(this.obj, event);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



}
