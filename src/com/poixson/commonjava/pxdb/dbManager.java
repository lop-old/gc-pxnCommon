package com.poixson.commonjava.pxdb;

import java.util.HashMap;
import java.util.Map;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.xLogger.xLog;


public final class dbManager {

	// db configs
	private static final Map<String, dbConfig> configs = new HashMap<String, dbConfig>();

	// db connection pools
	private static final Map<dbConfig, dbPool> pools = new HashMap<dbConfig, dbPool>();


	// db manager instance
	private static volatile dbManager manager = null;
	private static final Object lock = new Object();
	public static dbManager get() {
		if(manager == null) {
			synchronized(lock) {
				if(manager == null)
					manager = new dbManager();
			}
		}
		return manager;
	}
	private dbManager() {
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}



	// get config object
	public static dbConfig getConfig(final String dbKey) {
		if(utils.isEmpty(dbKey))
			return null;
		synchronized(configs) {
			if(configs.containsKey(dbKey))
				return configs.get(dbKey);
		}
		return null;
	}
	// get pool
	public static dbPool getPool(final String dbKey) {
		synchronized(pools) {
			final dbConfig config = getConfig(dbKey);
			if(config != null)
				if(pools.containsKey(config))
					return pools.get(config);
			log().warning("db config not found for key: "+dbKey);
			return null;
		}
	}
	// get worker
	public static dbWorker getWorkerLock(final String dbKey) {
		final dbPool pool = getPool(dbKey);
		if(pool == null)
			return null;
		return pool.getWorkerLock();
	}


	// new db connection pool
	// returns dbKey, used to reference connection later
	protected static String register(final dbConfig config) {
		if(config == null) throw new NullPointerException();
		synchronized(pools) {
			if(!configs.containsKey(config.dbKey()))
				configs.put(config.dbKey(), config);
			if(pools.containsKey(config)) {
				log().finest("Sharing an existing db pool :-)");
			} else {
				// initial connection
				log().finest("Starting new db pool..");
				final dbPool pool = new dbPool(config);
				final dbWorker worker = pool.getWorkerLock();
				if(worker == null)
					return null;
				worker.desc("Initial connection successful");
				worker.free();
				pools.put(config, pool);
			}
		}
		// unique key for this pool
		return config.dbKey();
	}



//	public static Map<String, dbConfig> getConfigs() {
//		return new HashMap<String, dbConfig> (configs);
//	}
//	public static Map<dbConfig, dbPool> getPools() {
//		return new HashMap<dbConfig, dbPool> (pools);
//	}



	// logger
	public static xLog log() {
		return xLog.getRoot("db");
	}


}
