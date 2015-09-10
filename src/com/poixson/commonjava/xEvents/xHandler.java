package com.poixson.commonjava.xEvents;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.xEvents.xEventListener.ListenerPriority;
import com.poixson.commonjava.xEvents.annotations.xEvent;
import com.poixson.commonjava.xLogger.xLog;


public abstract class xHandler<L extends xEventListener> {

	protected final Set<xListenerDAO> listeners =
			new CopyOnWriteArraySet<xListenerDAO>();



	protected xHandler() {
	}



	// event type
	protected abstract Class<? extends xEventData> getEventDataType();



	/**
	 * Register an event listener.
	 * @param xEventListener event listener instance
	 */
	public void register(final L listener) {
		if(listener == null) throw new NullPointerException("listener argument is required!");
		final Class<? extends xEventData> eventType = this.getEventDataType();
		// find listener methods
		final Set<Method> methodsFound = new HashSet<Method>();
		{
			final Method[] methods = listener.getClass().getMethods();
			for(final Method m : methods) {
				if(m.getParameterCount() != 1)
					continue;
				final Class<?>[] params = m.getParameterTypes();
				if(eventType.equals(params[0]))
					methodsFound.add(m);
			}
		}
		if(utils.isEmpty(methodsFound)) {
			throw new RuntimeException("No event listener methods found in class: "+
					listener.getClass().getName());
		}
		// load annotations
		final Set<xListenerDAO> listeners = new HashSet<xListenerDAO>();
		for(final Method method : methodsFound) {
			final xEvent anno = method.getAnnotation(xEvent.class);
			if(anno == null) {
				throw new RuntimeException("Event listener method is missing @xEvent annotation: "+
						listener.getClass().getName()+" -> "+method.getName());
			}
			// get properties
			final ListenerPriority priority = anno.priority();
//			final boolean async             = anno.async();
			final boolean filterHandled     = anno.filterHandled();
			final boolean filterCancelled   = anno.filterCancelled();
			final xListenerDAO dao = new xListenerDAO(
					listener,
					method,
					priority,
//					async,          // run asynchronous
					filterHandled,  // filter handled
					filterCancelled // filter cancelled
			);
			listeners.add(dao);
		}
		if(utils.isEmpty(listeners)) {
			throw new RuntimeException("No event listener methods found in class: "+
					listener.getClass().getName());
		}
		// log results
		{
			final int size = listeners.size();
			if(size == 1) {
				this.log().finest("Registered listener in class: "+listener.getClass().getName());
			} else {
				this.log().finest("Registered [ "+Long.toString(size)+" ] listeners in class: "+
						listener.getClass().getName());
			}
		}
		synchronized(this.listeners) {
			this.listeners.addAll(listeners);
		}
	}
	/**
	 * Unregister an event listener.
	 * @param listener
	 */
	public void unregister(final xEventListener listener) {
		if(listener == null) throw new NullPointerException("listener argument is required!");
		final Iterator<xListenerDAO> it = this.listeners.iterator();
		while(it.hasNext()) {
			final xListenerDAO dao = it.next();
			if(listener.equals(dao.listener)) {
				it.remove();
				this.log().finest("Removed listener: "+listener.getClass().getName());
				return;
			}
		}
		this.log().finest("Listener not found to remove");
	}
	/**
	 * Unregister an event listener by class type.
	 * @param clss
	 */
	public void unregisterType(final Class<?> listenerClass) {
		if(listenerClass == null) throw new NullPointerException("listener argument is required!");
		final Iterator<xListenerDAO> it = this.listeners.iterator();
		int count = 0;
		while(it.hasNext()) {
			final xListenerDAO dao = it.next();
			if(listenerClass.equals(dao.listener.getClass())) {
				it.remove();
				count++;
			}
		}
		if(count == 0) {
			this.log().finest("Listener not found to remove");
		} else {
			this.log().finest("Removed [ "+Integer.toString(count)+
					" ] listeners of type: "+listenerClass.getName());
		}
	}
	/**
	 * Unregister all listeners.
	 */
	public void unregisterAll() {
		if(this.listeners.isEmpty())
			return;
		synchronized(this.listeners) {
			this.listeners.clear();
		}
	}



	// trigger event
	public void trigger(final xEventData event) {
		// ensure main thread
//TODO: how did I do this before?





		if(event == null) throw new NullPointerException("event argument is required!");
		this.log().finest("Triggering event: "+event.toString());
//		final Set<xRunnableEvent> waitFor = new HashSet<xRunnableEvent>();
		// LOOP_PRIORITIES:
		for(final ListenerPriority p : ListenerPriority.values()) {
			final Iterator<xListenerDAO> it = this.listeners.iterator();
			LOOP_LISTENERS:
			while(it.hasNext()) {
				final xListenerDAO dao = it.next();
				if(!p.equals(dao.priority))
					continue LOOP_LISTENERS;
				if(event.isCancelled() && dao.filterCancelled)
					continue LOOP_LISTENERS;
				if(event.isHandled() && dao.filterHandled)
					continue LOOP_LISTENERS;
				// run event
				final xRunnableEvent run = new xRunnableEvent(
						dao,
						event,
						p
				);
//TODO:
//				waitFor.add(run);
//				xThreadPool.getMainPool()
//					.runLater(run);
				run.run();
			} // listeners loop
		} // priorities loop
//TODO:
//		// wait for event tasks to complete
//		for(final xRunnableEvent run : waitFor) {
//			run.waitUntilRun();
//		}
		if(event.isCancelled())
			this.log().fine("Event was cancelled: "+event.toString());
		if(!event.isHandled())
			this.log().fine("Event was not handled: "+event.toString());
	}



	// logger
	private volatile xLog _log = null;
	private xLog _log_default  = null;
	public xLog log() {
		final xLog log = this._log;
		if(log != null)
			return log;
		if(this._log_default == null)
			this._log_default = xLog.getRoot();
		return this._log_default;
	}
	public void setLog(final xLog log) {
		this._log = log;
	}



}
