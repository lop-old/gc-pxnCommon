package com.poixson.utils.xEvents;

import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;

import com.poixson.utils.xRunnable;
import com.poixson.utils.byref.BoolRef;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xEvents.xEventListener.ListenerPriority;
import com.poixson.utils.xEvents.xHandler.xListenerDAO;
import com.poixson.utils.xLogger.xLog;


public class xRunnableEvent extends xRunnable {

	private final xListenerDAO dao;
	private final xEventData event;
	private final ListenerPriority priority;

	public final BoolRef hasRun = new BoolRef(false);



	public xRunnableEvent(final xListenerDAO dao,
			final xEventData event, final ListenerPriority priority) {
		if (dao      == null) throw new RequiredArgumentException("dao");
		if (event    == null) throw new RequiredArgumentException("event");
		if (priority == null) throw new RequiredArgumentException("priority");
		this.dao      = dao;
		this.event    = event;
		this.priority = priority;
	}



	@Override
	public void run() {
		this.log().finest(
			"Invoking event: {}  {}",
			this.priority.name(),
			this.dao.listener
				.getClass()
					.getName()
		);
		try {
			this.dao.method.invoke(
				this.dao.listener,
				this.event
			);
		} catch (IllegalAccessException e) {
			//this.event.setCancelled();
//TODO:
//System.out.println("Class:  "+this.dao.listener.getClass().getName());
//System.out.println("Method: "+this.dao.method.getName());
//System.out.println("Event:  "+this.event.toString());
			this.log().trace(e);
			this.log().severe("This may be caused by using an anonymous xEventListener class. ");
			this.log().severe("Try extending with a new class file.");
		} catch (IllegalArgumentException | InvocationTargetException e) {
			this.event.setCancelled();
			this.log().trace(e);
		}
		synchronized(this.hasRun) {
			this.hasRun.value(true);
			this.hasRun.notifyAll();
		}
	}



	public void waitUntilRun() {
		if (this.hasRun.value)
			return;
		synchronized(this.hasRun) {
			try {
				this.hasRun.wait();
			} catch (InterruptedException e) {
				this.log()
					.trace(e);
			}
		}
	}



	// logger
	private volatile SoftReference<xLog> _log = null;
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null) {
				return log;
			}
		}
		final xLog log = xLog.getRoot();
		this._log = new SoftReference<xLog>(log);
		return log;
	}



}
