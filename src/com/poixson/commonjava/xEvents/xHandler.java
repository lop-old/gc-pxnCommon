package com.poixson.commonjava.xEvents;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.Utils.threads.xThreadPool;
import com.poixson.commonjava.xEvents.xEventListener.ListenerPriority;
import com.poixson.commonjava.xEvents.annotations.xEvent;
import com.poixson.commonjava.xLogger.xLog;


public class xHandler {

	protected final CopyOnWriteArraySet<ListenerHolder> listeners =
			new CopyOnWriteArraySet<ListenerHolder>();



	/**
	 * Register an event listener.
	 * @param listener
	 */
	public void register(final xEventListener listener) {
		if(listener == null) throw new NullPointerException("listener argument is required!");
		final Set<xListenerDAO> toadd = new HashSet<xListenerDAO>();
		// find annotated listener methods
		for(final Method method : listener.getClass().getMethods()) {
			if(method == null) continue;
			// has @xEvent annotation
			final xEvent annotate = method.getAnnotation(xEvent.class);
			if(annotate == null) continue;
			// register listener
			final xListenerDAO holder = new xListenerDAO(
				listener,
				method,
				annotate.priority(),
//				annotate.async(),
				annotate.filterHandled(),
				annotate.filterCancelled()
			);
			toadd.add(holder);
			log().finest("Registered listener ["+Long.toString(holder.id)+"] "+
					listener.toString()+" "+method.getName());
		}
		this.listeners.addAll(toadd);
	}
	/**
	 * Unregister an event listener.
	 * @param listener
	 */
	public void unregister(final xEventListener listener) {
		if(this.listeners.isEmpty()) return;
		synchronized(this.listeners) {
			if(this.listeners.contains(listener))
				this.listeners.remove(listener);
		}
	}
	/**
	 * Unregister an event listener by class type.
	 * @param clss
	 */
	public void unregister(final Class<? extends xEventListener> clss) {
		if(this.listeners.isEmpty()) return;
		synchronized(this.listeners){
			final Iterator<xListenerDAO> it = this.listeners.iterator();
			while(it.hasNext()) {
				final xListenerDAO listener = it.next();
				if(clss.isInstance(listener.listener)) {
					it.remove();
					log().finest("Unregistered listener: "+clss.getName());
				}
			}
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
	public void triggerNow(final xEventData event, final ListenerPriority onlyPriority) {
		triggerNow( (xThreadPool) null, event, onlyPriority );
	}
	public void triggerNow(final xThreadPool pool, final xEventData event, final ListenerPriority onlyPriority) {
		if(event == null) throw new NullPointerException("event argument is required!");
		final xThreadPool p = (
				pool == null
				? xThreadPool.getMainPool()
				: pool
		);
		p.runNow(
			this.getRunnable(event, onlyPriority)
		);
	}

	// trigger all priorities
	public void triggerLater(final xEventData event) {
		triggerLater(null, event, null);
	}
	// trigger only one priority
	public void triggerLater(final xEventData event, final ListenerPriority onlyPriority) {
		triggerLater(null, event, onlyPriority);
	}
	public void triggerLater(final xThreadPool pool, final xEventData event, final ListenerPriority onlyPriority) {
		if(event == null) throw new NullPointerException("event argument is required!");
		final xThreadPool p = (pool == null ? xThreadPool.getMainPool() : pool);
		p.runLater(
			this.getRunnable(event, onlyPriority)
		);
	}



	// xRunnableEvent
	protected xRunnable getRunnable(final xEventData event, final ListenerPriority onlyPriority) {
		return new xRunnable("Event-"+event.toString()) {
			private volatile xEventData event;
			private volatile ListenerPriority priority;
			public xRunnable init(final xEventData event, final ListenerPriority onlyPriority) {
				this.event = event;
				this.priority = onlyPriority;
				return this;
			}
			@Override
			public void run() {
				if(this.event == null) throw new NullPointerException("event argument is required!");
				if(this.priority == null) {
					for(final ListenerPriority p : ListenerPriority.values())
						doTrigger(this.event, p);
				} else {
					doTrigger(this.event, this.priority);
				}
			}
		}.init(event, onlyPriority);
	}



	/**
	 * Run an event in the current thread.
	 * @param event The event to be triggered.
	 * @param priority The priority level for the event. (this is required)
	 */
	public void doTrigger(final xEventData event, final ListenerPriority priority) {
		if(event    == null) throw new NullPointerException("event argument is required!");
		if(priority == null) throw new NullPointerException("priority argument is required!");
//		log().finest("doTrigger ( "+
//				utilsString.getLastPart(".", event.getClass().getName())+" , "+priority.name()+
//				(event.isHandled()   ? " <HANDLED>"   : "" )+
//				(event.isCancelled() ? " <CANCELLED>" : "" )+
//				" )"
//		);
		final Iterator<xListenerDAO> it = this.listeners.iterator();
		while(it.hasNext()) {
			final xListenerDAO holder = it.next();
			if(!priority.equals(holder.priority))             continue;
			if(holder.filterHandled && event.isHandled())     continue;
			if(holder.filterCancelled && event.isCancelled()) continue;
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



	// logger
	public static xLog log() {
		return xLog.getRoot();
	}



}
