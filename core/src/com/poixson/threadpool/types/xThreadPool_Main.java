package com.poixson.threadpool.types;

import java.util.concurrent.atomic.AtomicReference;

import com.poixson.threadpool.xThreadPool;


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
			if (instance.compareAndSet(null, pool)) {
				final xThreadPool existing =
					pools.putIfAbsent(MAIN_POOL_NAME, pool);
				if (existing != null) {
					instance.set( (xThreadPool_Main) existing );
					return (xThreadPool_Main) existing;
				}
				return pool;
			}
			return instance.get();
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



	@Override
	public boolean imposeMainPool() {
		return false;
	}
	@Override
	public void setImposeMainPool() {
		throw new UnsupportedOperationException();
	}
	public void disableImposeMainPool() {
		throw new UnsupportedOperationException();
	}



	// ------------------------------------------------------------------------------- //
	// stats



}
