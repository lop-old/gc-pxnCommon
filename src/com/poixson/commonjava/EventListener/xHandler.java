package com.poixson.commonjava.EventListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

import com.poixson.commonjava.EventListener.xEvent.Priority;
import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.Utils.threads.xThreadPool;
import com.poixson.commonjava.xLogger.xLog;


public class xHandler {

	protected final CopyOnWriteArraySet<ListenerHolder> listeners =
			new CopyOnWriteArraySet<ListenerHolder>();



	/**
	 * Listener data holder.
	 */
	protected static class ListenerHolder {
		private static final AtomicLong nextId = new AtomicLong(1);
		public final long id;
		public final xListener listener;
		public final Method method;
		public final Priority priority;
//TODO: these annotations aren't used yet
		public final boolean threaded;
		public final boolean filterHandled;
		public final boolean filterCancelled;
		public ListenerHolder(final xListener listener, final Method method,
				final Priority priority, final boolean threaded,
				final boolean filterHandled, final boolean filterCancelled) {
			if(listener == null) throw new NullPointerException("listener argument is required!");
			if(method   == null) throw new NullPointerException("method argument is required!");
			if(priority == null) throw new NullPointerException("priority argument is required!");
			this.id = nextId.getAndIncrement();
			this.listener        = listener;
			this.method          = method;
			this.priority        = priority;
//TODO: these annotations aren't used yet
			this.threaded        = threaded;
			this.filterHandled   = filterHandled;
			this.filterCancelled = filterCancelled;
		}
	}



	/**
	 * Register an event listener.
	 * @param listener
	 */
	public void register(final xListener listener) {
		if(listener == null) throw new NullPointerException("listener argument is required!");
		final Set<ListenerHolder> toadd = new HashSet<ListenerHolder>();
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
	public void unregister(final xListener listener) {
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
	public void unregister(final Class<? extends xListener> clss) {
		if(this.listeners.isEmpty()) return;
		synchronized(this.listeners){
			final Iterator<ListenerHolder> it = this.listeners.iterator();
			while(it.hasNext()) {
				final ListenerHolder listener = it.next();
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
	public void triggerNow(final xEventData event, final Priority onlyPriority) {
		triggerNow( (xThreadPool) null, event, onlyPriority );
	}
	public void triggerNow(final xThreadPool pool, final xEventData event, final Priority onlyPriority) {
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
	public void triggerLater(final xEventData event, final Priority onlyPriority) {
		triggerLater(null, event, onlyPriority);
	}
	public void triggerLater(final xThreadPool pool, final xEventData event, final Priority onlyPriority) {
		if(event == null) throw new NullPointerException("event argument is required!");
		final xThreadPool p = (pool == null ? xThreadPool.getMainPool() : pool);
		p.runLater(
			this.getRunnable(event, onlyPriority)
		);
	}



	// xRunnableEvent
	protected xRunnable getRunnable(final xEventData event, final Priority onlyPriority) {
		return new xRunnable("Event-"+event.toString()) {
			private volatile xEventData event;
			private volatile Priority priority;
			public xRunnable init(final xEventData event, final Priority onlyPriority) {
				this.event = event;
				this.priority = onlyPriority;
				return this;
			}
			@Override
			public void run() {
				if(this.event == null) throw new NullPointerException("event argument is required!");
				if(this.priority == null) {
					for(final Priority p : Priority.values())
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
	public void doTrigger(final xEventData event, final Priority priority) {
		if(event    == null) throw new NullPointerException("event argument is required!");
		if(priority == null) throw new NullPointerException("priority argument is required!");
//		log().finest("doTrigger ( "+
//				utilsString.getLastPart(".", event.getClass().getName())+" , "+priority.name()+
//				(event.isHandled()   ? " <HANDLED>"   : "" )+
//				(event.isCancelled() ? " <CANCELLED>" : "" )+
//				" )"
//		);
		final Iterator<ListenerHolder> it = this.listeners.iterator();
		while(it.hasNext()) {
			final ListenerHolder holder = it.next();
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
