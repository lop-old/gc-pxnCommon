package com.poixson.utils.pxdb;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import com.poixson.utils.Keeper;
import com.poixson.utils.Utils;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLog;


public final class dbManager {
	private static final String LOG_NAME = "DB";

	private static final AtomicReference<dbManager> instance =
			new AtomicReference<dbManager>(null);

	// db connection pools
	private final ConcurrentMap<String, dbPool> pools =
			new ConcurrentHashMap<String, dbPool>();



	// db manager instance
	public static dbManager get() {
		// existing manager instance
		{
			final dbManager manager = instance.get();
			if (manager != null)
				return manager;
		}
		// new manager instance
		{
			final dbManager manager = new dbManager();
			if (instance.compareAndSet(null, manager)) {
				return manager;
			}
		}
		return instance.get();
	}
	private dbManager() {
		Keeper.add(this);
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}



//TODO: is this useful?
//	// get config object
//	public static dbConfig getConfig(final String dbKey) {
//		if (Utils.isEmpty(dbKey))
//			return null;
//		return configs.get(dbKey);
//	}
	// get pool
	public static dbPool getPool(final String key) {
		if (Utils.isEmpty(key)) throw RequiredArgumentException.getNew("dbKey");
		final dbManager manager = get();
		return manager.pool(key);
	}
	public dbPool pool(final String key) {
		if (Utils.isEmpty(key)) throw RequiredArgumentException.getNew("dbKey");
		final dbPool pool = this.pools.get(key);
		if (pool == null) {
			log().warning("db config not found for key: {}", key);
			return null;
		}
		return pool;
	}
	// get worker
	public static dbWorker getLockedWorker(final String dbKey) {
		final dbPool pool = getPool(dbKey);
		if (pool != null) {
			return pool.getLockedWorker();
		}
		return null;
	}



	/**
	 * Register a new db config as a new pool.
	 * @param config
	 * @return null if successful; on failure, returns an instance of the existing pool.
	 */
	// new db connection pool and initial connection
	protected static dbPool register(final dbConfig config) {
		final dbManager manager = get();
		return manager.reg(config);
	}
	/**
	 * Register a new db config as a new pool.
	 * @param config
	 * @return null if successful; on failure, returns an instance of the existing pool.
	 */
	protected dbPool reg(final dbConfig config) {
		if (config == null) throw RequiredArgumentException.getNew("config");
		final String key = config.dbKey();
		if (Utils.isEmpty(key))
			throw new RuntimeException("dbKey returned from dbConfig is empty!");
		// use existing pool
		{
			final dbPool pool = this.pools.get(key);
			if (pool != null) {
				log().finest("Sharing existing db pool with matching config :-)");
				return pool;
			}
		}
		// new pool
		{
			final dbPool pool = new dbPool(config);
			final dbPool existing =
				this.pools.putIfAbsent(key, pool);
			if (existing != null) {
				log().finest("Sharing existing db pool with matching config :-)");
				return existing;
			}
			log().finer("Starting new db pool..");
			final dbWorker worker = pool.getLockedWorker();
			if (worker == null) {
				log().severe("Failed to start db conn pool");
				throw new RuntimeException("Failed to start db conn pool");
			}
			worker.desc("Initial connection successful");
			worker.free();
			return pool;
		}
	}



//TODO: is this useful?
//	public static Map<String, dbConfig> getConfigs() {
//		return new HashMap<String, dbConfig> (configs);
//	}
//	public static Map<dbConfig, dbPool> getPools() {
//		return new HashMap<dbConfig, dbPool> (pools);
//	}



	// logger
	private static volatile SoftReference<xLog> _log = null;
	public static xLog log() {
		if (_log != null) {
			final xLog log = _log.get();
			if (log != null) {
				return log;
			}
		}
		final xLog log =
			xLog.getRoot()
				.get(LOG_NAME);
		_log = new SoftReference<xLog>(log);
		return log;
	}



}
