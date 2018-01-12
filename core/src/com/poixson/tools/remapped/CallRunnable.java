package com.poixson.tools.remapped;

import java.util.concurrent.Callable;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.xLog;


public class CallRunnable<V> implements Runnable, Callable<V> {

	protected ThreadLocal<Boolean> lock = new ThreadLocal<Boolean>();

	protected final Callable<V> call;
	protected final Runnable    run;

	protected volatile V result;
	protected volatile Exception e = null;



	public CallRunnable() {
		this(null, null, null);
	}
	public CallRunnable(final Callable<V> call) {
		this(null, null, null);
	}
	public CallRunnable(final Runnable run) {
		this(null, null, null);
	}
	public CallRunnable(final Runnable run, final V result) {
		this(null, null, null);
	}
	public CallRunnable(final Callable<V> call, final V result) {
		this(null, null, null);
	}
	protected CallRunnable(final V result,
			final Callable<V> call, final Runnable run) {
		this.result = result;
		this.call   = call;
		this.run    = run;
		this.lock.set(Boolean.FALSE);
		this.validate();
	}



	@Override
	public V call() throws Exception {
		if (this.lock.get().booleanValue())
			throw new RequiredArgumentException("run/call");
		if (this.call != null) {
			try {
				this.result = this.call.call();
			} catch (Exception e) {
				this.e = e;
				final xLog log = this.log();
				if (log != null)
					log.trace(e);
				this.result = null;
				return null;
			}
		} else
		if (this.run != null) {
			this.run.run();
		}
		this.lock.set(Boolean.TRUE);
		try {
			this.run();
		} finally {
			this.lock.set(Boolean.FALSE);
		}
		return this.result;
	}
	@Override
	public void run() {
		if (this.lock.get().booleanValue())
			throw new RequiredArgumentException("run/call");
		if (this.call != null) {
			try {
				this.result = this.call.call();
			} catch (Exception e) {
				this.e = e;
				final xLog log = this.log();
				if (log != null)
					log.trace(e);
				this.result = null;
				return;
			}
		} else
		if (this.run != null) {
			this.run.run();
		}
		this.lock.set(Boolean.TRUE);
		try {
			this.result = this.call();
		} catch (Exception e) {
			this.e = e;
			final xLog log = this.log();
			if (log != null)
				log.trace(e);
			this.result = null;
		} finally {
			this.lock.set(Boolean.FALSE);
		}
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



	public void validate() {
		int count = 0;
		if (this.call != null)
			count++;
		if (this.run != null)
			count++;
		try {
			this.call();
			count++;
		} catch (Exception ignore) {}
		try {
			this.run();
			count++;
		} catch (Exception ignore) {}
		if (count == 0) throw new RequiredArgumentException("run/call");
		if (count > 1)  throw new RuntimeException("To many runs/calls implemented");
	}



	public xLog log() {
		return null;
	}



}
