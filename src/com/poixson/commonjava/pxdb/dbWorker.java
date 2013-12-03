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
	private volatile Boolean inUse = false;


	protected dbWorker(String dbKey, Connection conn) {
		this.dbKey = dbKey;
		this.conn = conn;
		this.id = getNextId();
	}


	// get db connection
	protected Connection getConnection() {
		return conn;
	}
	// get db key
	public String dbKey() {
		return dbKey;
	}


	// close connection
	public void close() {
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException ignore) {}
		}
		conn = null;
	}
	// has errored / disconnected
	public boolean hasClosed() {
		return (conn == null);
	}


	// in-use lock
	public boolean inUse() {
		return (inUse == true);
	}
	public boolean getLock() {
		if(inUse == true) return false;
		synchronized(inUse) {
			if(inUse == true) return false;
			inUse = true;
		}
		log().finest("LOCKED "+id);
		return true;
	}
	public void release() {
		inUse = false;
		log().finest("RELEASED "+id);
	}


	// connection id
	private static volatile Integer nextId = 0;
	private static int getNextId() {
		synchronized(nextId) {
			nextId++;
			return nextId;
		}
	}
	public int getId() {
		return id;
	}


	// logger
	public static xLog log() {
		return dbManager.log();
	}


}
