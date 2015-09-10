package com.poixson.commonjava.xEvents;

import java.lang.reflect.Method;

import com.poixson.commonjava.xEvents.xEventListener.ListenerPriority;


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
		if(listener == null) throw new NullPointerException("listener argument is required!");
		{
			final Class<? extends xEventListener> expected = this.getEventListenerType();
			if(!expected.equals(listener.getClass())) {
				throw new IllegalArgumentException("Invalid event listener type:  "+
						listener.getClass().getName()+"  expected: "+expected.getName());
			}
		}
		final String methodName = this.getMethodName();
		// find listener methods
		final Method methodFound;
		try {
			methodFound = listener.getClass().getMethod(
					methodName,
					this.getEventDataType()
			);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("No event listener method "+
					methodName+"() found in class: "+
					listener.getClass().getName());
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		if(methodFound == null) {
			throw new RuntimeException("No event listener method "+
					methodName+"() found in class: "+
					listener.getClass().getName());
		}
		// set properties
		final xListenerDAO dao = new xListenerDAO(
				listener,
				methodFound,
				ListenerPriority.NORMAL,
//				false, // run asynchronous
				false, // filter handled
				true   // filter cancelled
		);
		// log results
		this.log().finest("Registered listener "+methodName+
				"() in class: "+listener.getClass().getName());
		synchronized(this.listeners) {
			this.listeners.add(dao);
		}
	}



}
