/*
package com.poixson.commonjava.pxdb;

import java.sql.Connection;
import java.sql.SQLException;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.xCloseable;
import com.poixson.commonjava.xLogger.xLog;


public class dbWorker implements xCloseable {

	private final String dbKey;
	private final int id;
	private volatile String desc = null;

	private volatile Connection conn = null;
	private volatile boolean inUse = false;
	private final Object useLock = new Object();



	protected dbWorker(final String dbKey, final Connection conn) {
		this.dbKey = dbKey;
		this.conn = conn;
		this.id = getNextId();
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}



	// get db connection
	protected Connection getConnection() {
		return this.conn;
	}
	// get db key
	public String dbKey() {
		return this.dbKey;
	}
	public String getTablePrefix() {
		final dbConfig config = dbManager.getConfig(this.dbKey);
		if(config == null)
			return null;
		return config.getTablePrefix();
	}



	// close connection
	@Override
	public void close() {
		if(this.conn != null) {
			try {
				this.conn.close();
			} catch (SQLException ignore) {}
		}
		this.conn = null;
	}
	// has errored / disconnected
	@Override
	public boolean isClosed() {
		return (this.conn == null);
	}



	// in-use lock
	public boolean inUse() {
		return this.inUse;
	}
	public boolean getLock() {
		if(this.inUse)
			return false;
		synchronized(this.useLock) {
			if(this.inUse)
				return false;
			this.inUse = true;
		}
		log().finest("Locked #"+Integer.toString(this.id));
		return true;
	}
	public void free() {
		if(utils.notEmpty(this.desc))
			this.logDesc();
		log().finest("Released #"+Integer.toString(this.id));
		this.inUse = false;
	}
//	/ **
//	 * Get the time connection has been locked for
//	 * @return time in milliseconds
//	 * /
//	@Override
//	public long getLockTime() {
//TODO: this isn't being used yet
//		if(lockTime < 1)
//			return -1;
//		return pxnUtils.getCurrentMillis() - lockTime;
//	}



	// query description
	public void desc(final String descStr) {
		this.desc = descStr;
	}
	public void logDesc() {
		if(utils.isEmpty(this.desc))
			return;
		log().fine("Query: "+this.desc);
		this.desc = null;
	}



	// connection id
	private static volatile int nextId = 0;
	private static final Object nextLock = new Object();
	private static int getNextId() {
		synchronized(nextLock) {
			return ++nextId;
		}
	}
	public int getId() {
		return this.id;
	}



	// logger
	public static xLog log() {
		return dbManager.log();
	}



}
*/
