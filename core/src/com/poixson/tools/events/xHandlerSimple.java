package com.poixson.tools.events;

import java.lang.reflect.Method;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.tools.events.xEventListener.ListenerPriority;
import com.poixson.utils.StringUtils;


/**
 * Supports fixed named listener method.
 */
public abstract class xHandlerSimple extends xHandler {



	public xHandlerSimple() {
		super();
	}



	@Override
	protected abstract Class<? extends xEventListener> getEventListenerType();
	@Override
	protected abstract Class<? extends xEventData> getEventDataType();
	protected abstract String getMethodName();



	// register fixed named method
	@Override
	public void register(final xEventListener listener) {
		if (listener == null) throw new RequiredArgumentException("listener");
//TODO:
//		{
//			final Class<? extends xEventListener> expected = this.getEventListenerType();
//			if (!expected.equals(listener.getClass())) {
//				throw new IllegalArgumentException(
//					StringUtils.ReplaceTags(
//						"Invalid event listener type: {}  expected: {}",
//						listener.getClass().getName(),
//						expected.getName()
//					)
//				);
//			}
//		}
		final String methodName = this.getMethodName();
		// find listener methods
		final Method methodFound;
		try {
			methodFound = listener.getClass().getMethod(
				methodName,
				this.getEventDataType()
			);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(
				StringUtils.ReplaceTags(
					"No event listener method {}() found in class: {}",
					methodName,
					listener.getClass().getName()
				)
			);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		if (methodFound == null) {
			throw new RuntimeException(
				StringUtils.ReplaceTags(
					"No event listener method {}() found in class: {}",
					methodName,
					listener.getClass().getName()
				)
			);
		}
		// set properties
		final xListenerDAO dao = new xListenerDAO(
				listener,
				methodFound,
				ListenerPriority.NORMAL,
//TODO:
//				false, // run asynchronous
				false, // filter handled
				true   // filter cancelled
		);
		// log results
		this.log().finest(
			"Registered listener {}() in class: {}",
			methodName,
			listener.getClass()
				.getName()
		);
		this.listeners.add(dao);
	}



}
