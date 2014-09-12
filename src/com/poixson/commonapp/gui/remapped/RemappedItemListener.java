package com.poixson.commonapp.gui.remapped;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.poixson.commonjava.xLogger.xLog;


public class RemappedItemListener implements ItemListener {

	protected final Object obj;
	protected final Method method;



	public RemappedItemListener(final Object listenerClass, final String methodName)
			throws NoSuchMethodException {
		this.obj = listenerClass;
		final Class<?> clss = listenerClass.getClass();
		this.method = clss.getMethod(methodName, ItemEvent.class);
		if(this.method == null)
			throw new NullPointerException();
		xLog.getRoot().finest("New ItemListener created for: "+clss.getName()+"::"+methodName+"()");
	}



	@Override
	public void itemStateChanged(final ItemEvent event) {
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
