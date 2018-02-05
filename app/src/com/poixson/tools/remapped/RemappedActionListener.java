package com.poixson.tools.remapped;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.xLog;
import com.poixson.utils.Utils;
import com.poixson.utils.guiUtils;


public class RemappedActionListener implements ActionListener {

	protected final Object obj;
	protected final Method method;



	public static RemappedActionListener getNew(final Object listenerClass, final String methodName) {
		try {
			return new RemappedActionListener(listenerClass, methodName);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	public RemappedActionListener(final Object listenerClass, final String methodStr)
			throws NoSuchMethodException {
		if (listenerClass == null)    throw new RequiredArgumentException("listenerClass");
		if (Utils.isEmpty(methodStr)) throw new RequiredArgumentException("methodName");
		this.obj = listenerClass;
		final Class<?> clss = listenerClass.getClass();
		this.method = clss.getMethod(methodStr, ActionEvent.class);
		if (this.method == null) {
			this.log()
				.severe(
					"Method: {}() in class: {}",
					methodStr,
					listenerClass.getClass().getName()
				);
			throw new NoSuchMethodException();
		}
		this.log()
			.detail(
				"New ActionListener created for: {}::{}()",
				clss.getName(),
				methodStr
			);
	}



	@Override
	public void actionPerformed(final ActionEvent event) {
		try {
			this.method.invoke(this.obj, event);
		} catch (IllegalAccessException e) {
			this.log().trace(e);
		} catch (IllegalArgumentException e) {
			this.log().trace(e);
		} catch (InvocationTargetException e) {
			this.log().trace(e);
		} catch (Exception e) {
			this.log().trace(e);
		}
	}



	// logger
	public xLog log() {
		return guiUtils.log();
	}



}
