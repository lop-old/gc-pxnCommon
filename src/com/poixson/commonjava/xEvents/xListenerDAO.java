package com.poixson.commonjava.xEvents;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import com.poixson.commonjava.xEvents.xEventListener.ListenerPriority;


/**
 * Listener data holder.
 */
public class xListenerDAO {

	private static final AtomicLong listenerIdCounter = new AtomicLong(0);
	public final long id;

	public final xEventListener   listener;
	public final Method           method;

	public final ListenerPriority priority;
//	public final boolean          async;
	public final boolean          filterHandled;
	public final boolean          filterCancelled;



	public xListenerDAO(final xEventListener listener, final Method method,
			final ListenerPriority priority,
//			final boolean async,
			final boolean filterHandled, final boolean filterCancelled) {
		if(listener == null) throw new NullPointerException("listener argument is required!");
		if(method   == null) throw new NullPointerException("method argument is required!");
		if(priority == null) throw new NullPointerException("priority argument is required!");
		this.id = getNextId();
		this.listener        = listener;
		this.method          = method;
		this.priority        =
				priority == null
				? ListenerPriority.NORMAL
				: priority;
//		this.async           = async;
		this.filterHandled   = filterHandled;
		this.filterCancelled = filterCancelled;
	}



	private static long getNextId() {
		return listenerIdCounter.incrementAndGet();
	}



}
