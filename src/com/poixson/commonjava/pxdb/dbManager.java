package com.poixson.commonjava.pxdb;

import java.util.HashMap;
import java.util.Map;


public final class dbManager {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}


	// db manager instance
	private static volatile dbManager manager = null;
	private static final Object lock = new Object();
	public static dbManager get() {
		synchronized(lock) {
			if(manager == null)
				manager = new dbManager();
			return manager;
		}
	}
	public static dbPool get(String dbKey) {
		return get().getPool(dbKey);
	}
	public dbManager() {
	}


	// db connection pools
	private static final Map<dbConfig, dbPool> pools = new HashMap<dbConfig, dbPool>();


	// new db connection pool
	// returns dbKey, used to reference connection later
	protected String newConfig(dbConfig config) {
		synchronized(pools) {
			if(pools.containsKey(config)) {
//				log.info("db", "Using an existing db pool :-)");
			} else {
				dbPool pool = new dbPool(config);
				pools.put(config, pool);
			}
		}
		return config.getKey();
	}
	// get pool
	public dbPool getPool(String key) {
		synchronized(pools) {
			dbConfig config = dbConfig.get(key);
			if(config != null)
				if(pools.containsKey(config))
					return pools.get(config);
			return null;
		}
	}


}
