package com.poixson.commonjava.scheduler.ticker;

import java.util.concurrent.atomic.AtomicLong;

import com.poixson.commonjava.EventListener.xHandler;
import com.poixson.commonjava.Utils.xStartable;
import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.scheduler.xScheduledTask;
import com.poixson.commonjava.scheduler.xScheduler;
import com.poixson.commonjava.scheduler.triggers.triggerInterval;
import com.poixson.commonjava.xLogger.xLog;


public class xTicker extends xHandler implements xStartable {

	public static final String SCHEDULER_NAME = "xTicker";

	protected final xTime interval = xTime.get("1s");
	protected final AtomicLong nextId = new AtomicLong(0);

	protected volatile xScheduledTask schedTask = null;



	public xTicker() {
		super();
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
