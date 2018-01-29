package com.poixson.threadpool.types;

import java.util.concurrent.atomic.AtomicReference;


public class xThreadPool_Main extends xThreadPool_SingleWorker {

	public static final String MAIN_POOL_NAME = "main";

	private final static AtomicReference<xThreadPool_Main> instance =
			new AtomicReference<xThreadPool_Main>(null);



	public static xThreadPool_Main get() {
		// existing instance
		{
			final xThreadPool_Main pool = instance.get();
			if (pool != null)
				return pool;
		}
		// new instance
		{
			final xThreadPool_Main pool = new xThreadPool_Main();
			if ( ! instance.compareAndSet(null, pool) )
				return instance.get();
			return pool;
		}
	}



	protected xThreadPool_Main() {
		super(MAIN_POOL_NAME);
	}



	// ------------------------------------------------------------------------------- //
	// which thread



	@Override
	public boolean isMainPool() {
		return true;
	}
	@Override
	public boolean isEventDispatchPool() {
		return false;
	}



	// ------------------------------------------------------------------------------- //
	// stats



}
