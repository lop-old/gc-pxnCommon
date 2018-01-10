package com.poixson.tools.remapped;

import java.util.concurrent.Callable;

import com.poixson.exceptions.RequiredArgumentException;


// can also use Executors.callable(run) which returns RunnableAdapter
public class RemappedCallableRunnable<V> implements Callable<Object>, Runnable {

	protected final Runnable task;
	protected volatile V result;



	public RemappedCallableRunnable() {
		this(null, null);
	}
	public RemappedCallableRunnable(final V result) {
		this(null, result);
	}
	public RemappedCallableRunnable(final Runnable task) {
		this(task, null);
	}
	public RemappedCallableRunnable(final Runnable task, final V result) {
		super();
		this.task   = task;
		this.result = result;
	}



	@Override
	public V call() throws Exception {
		if (this.task == null) {
			this.run();
		} else {
			this.task.run();
		}
		return this.result;
	}
	@Override
	public void run() {
		throw new RequiredArgumentException("task");
	}



	public V getResult() {
		return this.result;
	}
	public void setResult(final V result) {
		this.result = result;
	}



}
