package com.poixson.commonjava.pxdb;

import java.sql.Connection;
import java.sql.SQLException;

import com.poixson.commonjava.xLogger.xLog;


public class dbWorker {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private final String dbKey;
	private final int id;
	private volatile Connection conn = null;
	private volatile boolean inUse = false;
	private final Object useLock = new Object();


	protected dbWorker(String dbKey, Connection conn) {
		this.dbKey = dbKey;
		this.conn = conn;
		this.id = getNextId();
	}


	// get db connection
	protected Connection getConnection() {
		return this.conn;
	}
	// get db key
	public String dbKey() {
		return this.dbKey;
	}


	// close connection
	public void close() {
		if(this.conn != null) {
			try {
				this.conn.close();
			} catch (SQLException ignore) {}
		}
		this.conn = null;
	}
	// has errored / disconnected
	public boolean hasClosed() {
		return (this.conn == null);
	}


	// in-use lock
	public boolean inUse() {
		return this.inUse;
	}
	public boolean getLock() {
		if(this.inUse == true) return false;
		synchronized(this.useLock) {
			if(this.inUse == true)
				return false;
			this.inUse = true;
		}
		log().finest("LOCKED "+this.id);
		return true;
	}
	public void release() {
		this.inUse = false;
		log().finest("RELEASED "+this.id);
	}


	// connection id
	private static volatile int nextId = 0;
	private static final Object nextLock = new Object();
	private static int getNextId() {
		synchronized(nextLock) {
			nextId++;
			return nextId;
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
