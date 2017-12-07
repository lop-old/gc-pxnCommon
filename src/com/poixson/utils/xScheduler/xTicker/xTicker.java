/*
package com.poixson.commonjava.scheduler.ticker;

import java.util.concurrent.atomic.AtomicLong;

import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.Utils.xStartable;
import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.scheduler.xScheduledTask;
import com.poixson.commonjava.scheduler.xScheduler;
import com.poixson.commonjava.scheduler.triggers.triggerInterval;
import com.poixson.commonjava.xEvents.xEventData;
import com.poixson.commonjava.xEvents.xEventListener;
import com.poixson.commonjava.xEvents.xHandlerSimple;


public class xTickHandler extends xHandlerSimple implements xStartable {
	private static final String LISTENER_METHOD_NAME = "onTick";
	public  static final String SCHEDULER_NAME = "xTicker";

	protected static volatile xTickHandler instance = null;
	protected static final Object instanceLock = new Object();

	protected final xTime interval = xTime.getNew("1s");
	protected final AtomicLong tickCounter = new AtomicLong(0);

	protected volatile xScheduledTask schedTask = null;



	public static xTickHandler get() {
		if(instance == null) {
			synchronized(instanceLock) {
				if(instance == null) {
					instance = new xTickHandler();
					Keeper.add(instance);
				}
			}
		}
		return instance;
	}
	public static xTickHandler peek() {
		return instance;
	}
	protected xTickHandler() {
		super();
	}



	// listener type
	@Override
	protected Class<? extends xEventListener> getEventListenerType() {
		return xTickListener.class;
	}
	// event type
	@Override
	protected Class<? extends xEventData> getEventDataType() {
		return xTickEvent.class;
	}
	// fixed method name
	@Override
	protected String getMethodName() {
		return LISTENER_METHOD_NAME;
	}



	@Override
	public void Start() {
		synchronized(instanceLock) {
			if(this.isRunning()) throw new RuntimeException("xTicker already running");
			this.schedTask = xScheduledTask.getNew()
					.setRepeating(true)
					.setRunnable(this)
					.setTrigger(
							triggerInterval.get(this.interval)
					);
			xScheduler.get().schedule(this.schedTask);
		}
	}
	@Override
	public void Stop() {
		xScheduler.get().cancel(this.schedTask);
	}
	@Override
	public boolean isRunning() {
		return xScheduler.get().hasTask(SCHEDULER_NAME);
	}



	@Override
	public void run() {
		synchronized(this.tickCounter) {
			final long id = getNextTickId();
			log().finest("TICK [ "+Long.toString(id)+" ]");
			// trigger tick event
			final xTickEvent event = new xTickEvent(id);
			this.trigger(event);
		}
	}



	private long getNextTickId() {
		return this.tickCounter.incrementAndGet();
	}



	public void setInterval(final String value) {
		this.setInterval(xTime.getNew(value));
	}
	public void setInterval(final xTime value) {
		this.interval.set(value);
		this.log().info("Tick interval: "+value.getString());
	}



}
*/
