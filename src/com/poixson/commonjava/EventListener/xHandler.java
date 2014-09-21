package com.poixson.commonjava.EventListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.poixson.commonjava.EventListener.xEvent.Priority;
import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.Utils.xThreadPool;
import com.poixson.commonjava.xLogger.xLog;


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
		protected ListenerHolder(final xListener listener, final Method method,
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
		synchronized(this.listeners) {
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
				this.listeners.add(holder);
System.out.println("Registered listener ["+Integer.toString(this.listeners.size())+"] "+
listener.toString()+" "+method.getName());
			}
		}
	}
	/**
	 * Unregister an event listener.
	 * @param listener
	 */
	public void unregister(final xListener listener) {
		if(this.listeners.isEmpty()) return;
		synchronized(this.listeners) {
			if(this.listeners.contains(listener))
				this.listeners.remove(listener);
		}
	}
	/**
	 * Unregister all listeners.
	 */
	public void unregisterAll() {
		if(this.listeners.isEmpty()) return;
		synchronized(this.listeners) {
			this.listeners.clear();
		}
	}



	// trigger all priorities
	public void triggerNow(final xEventData event) {
		triggerNow(null, event, null);
	}
	// trigger only one priority
	public void triggerNow(final xEventData event, final Priority onlyPriority) {
		triggerNow(null, event, onlyPriority);
	}
	public void triggerNow(final xThreadPool pool, final xEventData event, final Priority onlyPriority) {
		if(event == null) throw new NullPointerException();
		final xThreadPool p = (pool == null ? xThreadPool.get() : pool);
		p.runNow(
			new xRunnableEvent(event, onlyPriority)
		);
	}

	// trigger all priorities
	public void triggerLater(final xEventData event) {
		triggerLater(null, event, null);
	}
	// trigger only one priority
	public void triggerLater(final xEventData event, final Priority onlyPriority) {
		triggerLater(null, event, onlyPriority);
	}
	public void triggerLater(final xThreadPool pool, final xEventData event, final Priority onlyPriority) {
		if(event == null) throw new NullPointerException();
		final xThreadPool p = (pool == null ? xThreadPool.get() : pool);
		p.runLater(
			new xRunnableEvent(event, onlyPriority)
		);
	}



	public class xRunnableEvent extends xRunnable {
		private final xEventData event;
		private final Priority priority;
		public xRunnableEvent(final xEventData event, final Priority onlyPriority) {
			super("Event-"+event.toString());
			this.event = event;
			this.priority = onlyPriority;
		}
		@Override
		public void run() {
			if(this.event == null) throw new NullPointerException();
			if(this.priority == null) {
				for(Priority p : Priority.values())
					doTrigger(this.event, p);
			} else {
				doTrigger(this.event, this.priority);
			}
		}
	}



	/**
	 * Run an event in the current thread.
	 * @param event The event to be triggered.
	 * @param priority The priority level for the event. (this is required)
	 */
	public void doTrigger(final xEventData event, final Priority priority) {
		if(event    == null) throw new NullPointerException("event cannot be null");
		if(priority == null) throw new NullPointerException("priority cannot be null");
		synchronized(this.listeners) {
			for(ListenerHolder holder : this.listeners) {
				if(!priority.equals(holder.priority))
					continue;
				try {
					holder.method.invoke(holder.listener, event);
				} catch (IllegalAccessException e) {
					log().trace(e);
				} catch (IllegalArgumentException e) {
					log().trace(e);
				} catch (InvocationTargetException e) {
					log().trace(e);
				}
			}
		}
	}



	// logger
	public static xLog log() {
		return xLog.getRoot();
	}



}
