package com.poixson.commonjava.pxdb;

import java.util.HashMap;
import java.util.Map;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.xLogger.xLog;


public final class dbManager {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	// db connection pools
	private static final Map<dbConfig, dbPool> pools = new HashMap<dbConfig, dbPool>();


	// db manager instance
	private static volatile dbManager manager = null;
	private static final Object lock = new Object();
	public static dbManager get() {
		if(manager != null)
			return manager;
		synchronized(lock) {
			if(manager == null)
				manager = new dbManager();
		}
		return manager;
	}
	private dbManager() {
	}
	// get pool
	public static dbPool getPool(String dbKey) {
		synchronized(pools) {
			dbConfig config = dbConfig.get(dbKey);
			if(config != null)
				if(pools.containsKey(config))
					return pools.get(config);
			System.out.println("db config not found for key: "+dbKey);
			return null;
		}
	}
	// get worker
	public static dbWorker getWorkerLock(String dbKey) {
		dbPool pool = getPool(dbKey);
		if(pool == null)
			return null;
		return pool.getWorkerLock();
	}


	// new db connection pool
	// returns dbKey, used to reference connection later
	protected static String register(dbConfig config) {
		synchronized(pools) {
			if(pools.containsKey(config)) {
				System.out.println("Using an existing db pool :-)");
			} else {
				System.out.println("Starting new db pool..");
				dbPool pool = new dbPool(config);
				pools.put(config, pool);
			}
		}
		// unique key for this pool
		return config.getKey();
	}


	// logger
	public static xLog log() {
		return xVars.log("db");
	}


}
