package com.poixson.commonjava.EventListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.poixson.commonjava.EventListener.xEvent.Priority;


public class xHandler {

	protected final Set<ListenerHolder> listeners = new HashSet<ListenerHolder>();


	/**
	 * Listener data holder.
	 */
	protected static class ListenerHolder {
		public final xListener listener;
		public final Method method;
		public final Priority priority;
		public final boolean threaded;
		public final boolean ignoreHandled;
		public final boolean ignoreCancelled;
		private ListenerHolder(final xListener listener, final Method method,
				final Priority priority, final boolean threaded,
				final boolean ignoreHandled, final boolean ignoreCancelled) {
			if(listener == null) throw new NullPointerException("listener cannot be null");
			if(method   == null) throw new NullPointerException("method cannot be null");
			if(priority == null) throw new NullPointerException("priority cannot be null");
			this.listener = listener;
			this.method = method;
			this.priority = priority;
			this.threaded = threaded;
			this.ignoreHandled = ignoreHandled;
			this.ignoreCancelled = ignoreCancelled;
		}
	}


	/**
	 * Register an event listener.
	 * @param listener
	 */
	public void register(final xListener listener) {
		if(listener == null) throw new NullPointerException("listener cannot be null");
		synchronized(listeners) {
			// find annotated listener methods
			for(final Method method : listener.getClass().getMethods()) {
				if(method == null) continue;
				// has @xEvent annotation
				final xEvent annotate = method.getAnnotation(xEvent.class);
				if(annotate == null) continue;
				// register listener
				final ListenerHolder holder = new ListenerHolder(
					listener,
					method,
					annotate.priority(),
					annotate.threaded(),
					annotate.ignoreHandled(),
					annotate.ignoreCancelled()
				);
				listeners.add(holder);
System.out.println("Registered listener ["+Integer.toString(listeners.size())+"] "+
listener.toString()+" "+method.getName());
			}
		}


	}
	/**
	 * Unregister an event listener.
	 * @param listener
	 */
	public void unregister(final xListener listener) {
		if(listeners.isEmpty()) return;
		synchronized(listeners) {
			if(listeners.contains(listener))
				listeners.remove(listener);
		}
	}
	/**
	 * Unregister all listeners.
	 */
	public void unregisterAll() {
		if(listeners.isEmpty()) return;
		synchronized(listeners) {
			listeners.clear();
		}
	}


	public void trigger(final xEventMeta event) {
//TODO: this may need to be reversed
		for(Priority priority : Priority.values())
			trigger(event, priority);
	}
	public void trigger(final xEventMeta event, final Priority onlyPriority) {
		if(event        == null) throw new NullPointerException("event cannot be null");
		if(onlyPriority == null) throw new NullPointerException("priority cannot be null");
		synchronized(listeners) {
			for(ListenerHolder holder : listeners) {
				if(!onlyPriority.equals(holder.priority)) continue;
				try {
					holder.method.invoke(holder.listener, event);
					//m.invoke(event);
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}


}
