package com.poixson.commonjava.xEvents;

import java.lang.reflect.InvocationTargetException;

import com.poixson.commonjava.Utils.xRunnable;
import com.poixson.commonjava.Utils.byRef.BoolRef;
import com.poixson.commonjava.xEvents.xEventListener.ListenerPriority;
import com.poixson.commonjava.xLogger.xLog;


public class xRunnableEvent extends xRunnable {

	private final xListenerDAO dao;
	private final xEventData event;
	private final ListenerPriority priority;

	public final BoolRef hasRun = new BoolRef(false);



	public xRunnableEvent(final xListenerDAO dao,
			final xEventData event, final ListenerPriority priority) {
		if(dao      == null) throw new NullPointerException("dao argument is required!");
		if(event    == null) throw new NullPointerException("event argument is required!");
		if(priority == null) throw new NullPointerException("priority argument is required!");
		this.dao   = dao;
		this.event = event;
		this.priority = priority;
	}



	@Override
	public void run() {
		this.log().finest("Invoking event: "+this.priority.name()+
				"  "+this.dao.listener.getClass().getName());
		try {
			this.dao.method.invoke(
					this.dao.listener,
					this.event
			);
		} catch (IllegalAccessException e) {
			//this.event.setCancelled();
//System.out.println("Class:  "+this.dao.listener.getClass().getName());
//System.out.println("Method: "+this.dao.method.getName());
//System.out.println("Event:  "+this.event.toString());
			this.log().trace(e);
			this.log().severe("This may be caused by using an anonymous xEventListener class. ");
			this.log().severe("Try extending with a new class file.");
		} catch (IllegalArgumentException e) {
			this.event.setCancelled();
			this.log().trace(e);
		} catch (InvocationTargetException e) {
			this.event.setCancelled();
			this.log().trace(e);
		}
		synchronized(this.hasRun) {
			this.hasRun.value(true);
			this.hasRun.notifyAll();
		}
	}



	public void waitUntilRun() {
		if(this.hasRun.value)
			return;
		synchronized(this.hasRun) {
			try {
				this.hasRun.wait();
			} catch (InterruptedException e) {
				this.log().trace(e);
			}
		}
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
