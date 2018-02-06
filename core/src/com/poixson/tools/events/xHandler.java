package com.poixson.tools.events;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.xLog;
import com.poixson.tools.events.xEventListener.ListenerPriority;


public abstract class xHandler {

	protected final CopyOnWriteArraySet<xListenerDAO> listeners = new CopyOnWriteArraySet<>();



	/**
	 * Listener data holder.
	 */
	protected static class xListenerDAO {

		private static final AtomicLong nextIndex = new AtomicLong(0);
		public final long index;

		public final xEventListener   listener;
		public final Method           method;

		public final ListenerPriority priority;
//TODO:
//		public final boolean          async;
		public final boolean          filterHandled;
		public final boolean          filterCancelled;

		public xListenerDAO(final xEventListener listener, final Method method,
				final ListenerPriority priority,
//TODO:
//				final boolean async,
				final boolean filterHandled, final boolean filterCancelled) {
			if (listener == null) throw new RequiredArgumentException("listener");
			if (method   == null) throw new RequiredArgumentException("method");
			this.index    = getNextIndex();
			this.listener = listener;
			this.method   = method;
			this.priority = (
				priority == null
				? ListenerPriority.NORMAL
				: priority
			);
//TODO:
//			this.async           = async;
			this.filterHandled   = filterHandled;
			this.filterCancelled = filterCancelled;
		}

		private static long getNextIndex() {
			return nextIndex.incrementAndGet();
		}

	}



	protected xHandler() {
	}



	// listener type
	protected abstract Class<? extends xEventListener> getEventListenerType();
	// event type
	protected abstract Class<? extends xEventData> getEventDataType();



	/**
	 * Register an event listener.
	 * @param xEventListener event listener instance
	 */
	public abstract void register(final xEventListener listener);



	/**
	 * Unregister an event listener.
	 * @param listener
	 */
	public void unregister(final xEventListener listener) {
		if (listener == null) throw new RequiredArgumentException("listener");
		final Set<xListenerDAO> removing = new HashSet<xListenerDAO>();
		{
			final Iterator<xListenerDAO> it = this.listeners.iterator();
			while (it.hasNext()) {
				final xListenerDAO dao = it.next();
				if (listener.equals(dao.listener)) {
					this.log().finest(
						"Removed listener: {}",
						listener.getClass().getName()
					);
					removing.add(dao);
					return;
				}
			}
		}
		if (removing.isEmpty()) {
			this.log()
				.finest("Listener not found to remove");
		} else {
			final Iterator<xListenerDAO> it = removing.iterator();
			while (it.hasNext()) {
				this.listeners.remove(it.next());
			}
		}
	}
	/**
	 * Unregister an event listener by class type.
	 * @param clss
	 */
	public void unregisterType(final Class<?> listenerClass) {
		if (listenerClass == null) throw new RequiredArgumentException("listenerClass");
		final Iterator<xListenerDAO> it = this.listeners.iterator();
		int count = 0;
		while (it.hasNext()) {
			final xListenerDAO dao = it.next();
			if (listenerClass.equals(dao.listener.getClass())) {
				this.listeners.remove(dao);
				count++;
				this.log()
					.finest(
						"Removed listener: {}",
						dao.listener.getClass()
							.getName()
					);
			}
		}
		if (count == 0) {
			this.log().finest("Listener not found to remove");
		} else {
			this.log().finest(
				"Removed [ {} ] listeners of type: {}",
				Integer.toString(count),
				listenerClass.getName()
			);
		}
	}
	/**
	 * Unregister all listeners.
	 */
	public void unregisterAll() {
		if (this.listeners.isEmpty())
			return;
		this.listeners.clear();
	}



	// trigger event
	public void trigger(final xEventData event) {
//TODO: ensure main thread
		if (event == null) throw new RequiredArgumentException("event");
//TODO:
//		final Set<xRunnableEvent> waitFor = new HashSet<xRunnableEvent>();
		boolean isFirst = true;
		// LOOP_PRIORITIES:
		for (final ListenerPriority p : ListenerPriority.values()) {
			final Iterator<xListenerDAO> it = this.listeners.iterator();
			LOOP_LISTENERS:
			while (it.hasNext()) {
				final xListenerDAO dao = it.next();
				if (!p.equals(dao.priority)) {
					continue LOOP_LISTENERS;
				}
				if (event.isCancelled() && dao.filterCancelled) {
					continue LOOP_LISTENERS;
				}
				if (event.isHandled() && dao.filterHandled) {
					continue LOOP_LISTENERS;
				}
				if (isFirst) {
					isFirst = false;
					this.log().finest("Triggering events:", event.toString());
				}
				// run event
				final xRunnableEvent run =
					new xRunnableEvent(
						dao,
						event,
						p
					);
//TODO:
//				waitFor.add(run);
//				xThreadPool.getMainPool()
//					.runLater(run);
				run.run();
			} /* listeners loop */
		} /* priorities loop */
		if (isFirst) {
			this.log().finest("Event ignored:", event.toString());
		}
//TODO:
//		// wait for event tasks to complete
//		for (final xRunnableEvent run : waitFor) {
//			run.waitUntilRun();
//		}
		if (event.isCancelled()) {
			this.log().fine("Event was cancelled:", event.toString());
		}
		if (!event.isHandled()) {
			this.log().fine("Event was not handled:", event.toString());
		}
	}



	// logger
	private volatile SoftReference<xLog> _log = null;
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null)
				return log;
		}
		final xLog log = xLog.getRoot();
		this._log = new SoftReference<xLog>(log);
		return log;
	}



}
