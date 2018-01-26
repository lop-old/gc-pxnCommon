package com.poixson.tools.remapped;

import java.util.concurrent.Callable;

import com.poixson.utils.Utils;


public class xCallable<V> extends xRunnable implements Callable<V> {

	public final Callable<V> call;

	protected final ThreadLocal<Boolean> callDepth = new ThreadLocal<Boolean>();

	protected volatile V result;
	protected volatile Exception e = null;



	public xCallable() {
		super();
		this.call   = null;
		this.result = null;
	}
	public xCallable(final String taskName) {
		this(null, null, null);
		this.taskName = taskName;
	}
	public xCallable(final V result) {
		this(result, null, null);
	}
	public xCallable(final Runnable run) {
		this(null, run, null);
	}
	public xCallable(final Callable<V> call) {
		this(null, null, call);
	}
	public xCallable(final V result, final Runnable run) {
		this(result, run, null);
	}
	public xCallable(final V result, final Callable<V> call) {
		this(result, null, call);
	}
	protected xCallable(final V result,
			final Runnable run, final Callable<V> call) {
		super(run);
		if (run != null && call != null)
			throw new IllegalArgumentException("Cannot set runnable and callable at the same time!");
		this.call   = call;
		this.result = result;
	}



	// ------------------------------------------------------------------------------- //
	// cast



	@SuppressWarnings("unchecked")
	public static <V> xCallable<V> cast(final Object obj) {
		if (obj == null)
			return null;
		// already correct type
		if (obj instanceof xCallable)
			return (xCallable<V>) obj;
		// cast from runnable
		if (obj instanceof Runnable) {
			final Runnable run = (Runnable) obj;
			final xCallable<V> result = new xCallable<V>(run);
			// get name from interface
			if (run instanceof RunnableNamed) {
				result.setTaskName(
					((RunnableNamed) run).getTaskName()
				);
			}
			return result;
		} else
		// cast from callable
		if (obj instanceof Callable) {
			final Callable<V> call = (Callable<V>) obj;
			final xCallable<V> result = new xCallable<V>(call);
			// get name from interface
			if (call instanceof RunnableNamed) {
				result.setTaskName(
					((RunnableNamed) call).getTaskName()
				);
			}
			return result;
		}
		// unknown object
		throw new UnsupportedOperationException("Invalid object, cannot cast!");
	}



	// ------------------------------------------------------------------------------- //
	// run task



	@Override
	public void run() {
		if (this.task != null) {
			this.task.run();
			return;
		}
		try {
			this.checkCallDepth();
			this.result = this.call();
		} finally {
			this.releaseCallDepth();
		}
	}
	@Override
	public V call() {
		if (this.call != null) {
			this.result =
				this.call();
			return this.result;
		}
		try {
			this.checkCallDepth();
			this.run();
		} finally {
			this.releaseCallDepth();
		}
		return this.result;
	}
	private void checkCallDepth() {
		final Boolean depth = this.callDepth.get();
		if (depth == null) {
			this.callDepth.set(Boolean.TRUE);
			return;
		}
		if (depth.booleanValue())
			throw new UnsupportedOperationException("Must set or override run() or call()");
		this.callDepth.set(Boolean.TRUE);
	}
	private void releaseCallDepth() {
		this.callDepth.set(Boolean.FALSE);
	}



	// ------------------------------------------------------------------------------- //
	// result



	public V getResult() {
		return this.result;
	}
	public void setResult(final V result) {
		this.result = result;
	}



	public Exception getException() {
		return this.e;
	}



	// ------------------------------------------------------------------------------- //
	// config



	@Override
	public String getTaskName() {
		if (this.call != null) {
			if (this.call instanceof RunnableNamed) {
				final String taskName = ((RunnableNamed) this.call).getTaskName();
				if (Utils.notEmpty(taskName))
					return taskName;
			}
		}
		return this.taskName;
	}
	@Override
	public void setTaskName(final String taskName) {
		this.taskName = (
			Utils.isEmpty(taskName)
			? null
			: taskName
		);
	}
	@Override
	public boolean taskNameEquals(final String taskName) {
		final String thisName = this.getTaskName();
		if (Utils.isEmpty(taskName))
			return Utils.isEmpty(thisName);
		return taskName.equals(thisName);
	}



}
