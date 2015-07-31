package com.poixson.commonjava.scheduler.ticker;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import com.poixson.commonjava.EventListener.xEvent;
import com.poixson.commonjava.EventListener.xHandler;
import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.Utils.xStartable;
import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.scheduler.xScheduledTask;
import com.poixson.commonjava.scheduler.xScheduler;
import com.poixson.commonjava.scheduler.triggers.triggerInterval;
import com.poixson.commonjava.xLogger.xLog;


public class xTicker extends xHandler implements xStartable {

	public static final String SCHEDULER_NAME = "xTicker";

	protected static volatile xTicker instance = null;
	protected static final Object instanceLock = new Object();

	protected final xTime interval = xTime.get("1s");
	protected final AtomicLong nextId = new AtomicLong(0);

	protected volatile xScheduledTask schedTask = null;



	public static xTicker get() {
		if(instance == null) {
			synchronized(instanceLock) {
				if(instance == null)
					instance = new xTicker();
			}
		}
		return instance;
	}
	protected xTicker() {
		super();
		Keeper.add(this);
	}



	@Override
	public void Start() {
		if(this.isRunning()) throw new RuntimeException("xTicker already running");
		this.schedTask = xScheduledTask.get()
			.setRepeating(true)
			.setRunnable(this)
			.setTrigger(
				triggerInterval.get(this.interval)
			);
		xScheduler.get().schedule(this.schedTask);
	}
	@Override
	public void Stop() {
		xScheduler.get().cancel(this.schedTask);
	}
	@Override
	public boolean isRunning() {
		return xScheduler.get().hasTask(SCHEDULER_NAME);
	}



	public void register(final xTickListener listener) {
		if(listener == null) throw new NullPointerException("listener argument is required!");
		final Method method;
		try {
			method = listener.getClass().getMethod("onTick", xTickEvent.class);
		} catch (NoSuchMethodException e) {
			log().severe("onTick method is missing!");
			log().trace(e);
			return;
		} catch (SecurityException e) {
			log().trace(e);
			return;
		}
		if(method == null) throw new NullPointerException("onTick method is missing!");
		final ListenerHolder holder = new xHandler.ListenerHolder(
			listener,
			method,
			xEvent.Priority.NORMAL,
			false, // threaded
			false, // filter handled
			true   // filter cancelled
		);
		log().finest("Registered listener ["+Long.toString(holder.id)+"] "+
				listener.toString()+" "+method.getName());
		this.listeners.add(holder);
	}



	@Override
	public void run() {
		synchronized(this.nextId) {
			final long id = this.nextId.incrementAndGet();
			final xTickEvent event = new xTickEvent(id);
			xLog.getRoot().finest("TICK [ "+Long.toString(id)+" ]");
			this.triggerLater(event);
		}
	}



	public void setInterval(final String value) {
		this.setInterval(xTime.get(value));
	}
	public void setInterval(final xTime value) {
		this.interval.set(value);
		xLog.getRoot().info("Tick interval: "+value.getString());
	}



}
