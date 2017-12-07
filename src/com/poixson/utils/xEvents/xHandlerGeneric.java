package com.poixson.utils.xEvents;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.poixson.utils.Utils;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xEvents.xEventListener.ListenerPriority;


/**
 * Supports dynamicly named listener methods.
 */
public abstract class xHandlerGeneric extends xHandler {



	public xHandlerGeneric() {
		super();
	}



	@Override
	protected abstract Class<? extends xEventListener> getEventListenerType();
	@Override
	protected abstract Class<? extends xEventData> getEventDataType();



	// register all xEvent listeners
	@Override
	public void register(final xEventListener listener) {
		if (listener == null) throw RequiredArgumentException.getNew("listener");
		final Class<? extends xEventData> eventType = this.getEventDataType();
		// find listener methods
		final Set<Method> methodsFound = new HashSet<Method>();
		{
			final Method[] methods = listener.getClass().getMethods();
			for (final Method m : methods) {
				if (m.getParameterCount() != 1)
					continue;
				final Class<?>[] params = m.getParameterTypes();
				if (eventType.equals(params[0])) {
					methodsFound.add(m);
				}
			}
		}
		if (Utils.isEmpty(methodsFound)) {
			throw new RuntimeException("No event listener methods found in class: "+
					listener.getClass().getName());
		}
		// load annotations
		final Set<xListenerDAO> listeners = new HashSet<xListenerDAO>();
		for (final Method method : methodsFound) {
			final xEvent anno = method.getAnnotation(xEvent.class);
			if (anno == null) {
				throw new RuntimeException(
					(new StringBuilder())
						.append("Event listener method is missing @xEvent annotation: ")
						.append(listener.getClass().getName())
						.append(" -> ")
						.append(method.getName())
						.toString()
				);
			}
			// get properties
			final ListenerPriority priority = anno.priority();
//TODO:
//			final boolean async             = anno.async();
			final boolean filterHandled     = anno.filterHandled();
			final boolean filterCancelled   = anno.filterCancelled();
			final xListenerDAO dao = new xListenerDAO(
					listener,
					method,
					priority,
//TODO:
//					async,          // run asynchronous
					filterHandled,  // filter handled
					filterCancelled // filter cancelled
			);
			listeners.add(dao);
		}
		if (Utils.isEmpty(listeners)) {
			throw new RuntimeException("No event listener methods found in class: "+
					listener.getClass().getName());
		}
		// log results
		{
			final int size = listeners.size();
			if (size == 1) {
				this.log()
					.finest(
						"Registered listener in class: {}",
						listener.getClass()
							.getName()
					);
			} else {
				this.log()
					.finest(
						"Registered [ {} ] listeners in class: {}",
						Long.toString(size),
						listener.getClass()
							.getName()
				);
			}
		}
		this.listeners.addAll(listeners);
	}



}
