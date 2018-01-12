package com.poixson.tools.remapped;

import java.util.concurrent.Callable;

import com.poixson.exceptions.RequiredArgumentException;


// can also use Executors.callable(run) which returns RunnableAdapter
public class RemappedCallableRunnable<V> implements Callable<V>, Runnable {

	protected final Runnable run;

	protected volatile V result;



	public RemappedCallableRunnable() {
		this(null, null);
	}
	public RemappedCallableRunnable(final V result) {
		this(null, result);
	}
	public RemappedCallableRunnable(final Runnable run) {
		this(run, null);
	}
	public RemappedCallableRunnable(final Runnable run, final V result) {
		super();
		this.run    = run;
		this.result = result;
	}



	@Override
	public V call() throws Exception {
		if (this.run == null) {
			this.run();
		} else {
			this.run.run();
		}
		return this.result;
	}
	@Override
	public void run() {
		throw new RequiredArgumentException("run");
	}



	public V getResult() {
		return this.result;
	}
	public void setResult(final V result) {
		this.result = result;
	}



}
