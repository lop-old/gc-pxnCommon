package com.poixson.tools.remapped;

import java.util.concurrent.Callable;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.xLog;


public class RemappedRunnableCallable<V> implements Callable<Object>, Runnable {

	protected final Callable<V> call;
	protected volatile V result;

	protected volatile Exception e = null;
	protected volatile xLog log = null;



	public RemappedRunnableCallable() {
		this(null, null);
	}
	public RemappedRunnableCallable(final V result) {
		this(null, result);
	}
	public RemappedRunnableCallable(final Callable<V> call) {
		this(call, null);
	}
	public RemappedRunnableCallable(final Callable<V> call, final V result) {
		super();
		this.call   = call;
		this.result = result;
	}



	@Override
	public void run() {
		try {
			if (this.call == null) {
				this.result = this.call();
			} else {
				this.result = this.call.call();
			}
		} catch (Exception e) {
			this.e = e;
			final xLog log = this.log();
			if (log != null)
				log.trace(e);
			this.result = null;
		}
	}
	@Override
	public V call() throws Exception {
		throw new RequiredArgumentException("task");
	}



	public V getResult() {
		return this.result;
	}
	public void setResult(final V result) {
		this.result = result;
	}



	public Exception getException() {
		return this.e;
	}



	public xLog log() {
		return null;
	}



}
