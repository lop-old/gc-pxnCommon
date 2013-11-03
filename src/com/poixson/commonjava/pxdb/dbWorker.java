package com.poixson.commonjava.pxdb;

import java.sql.Connection;
import java.sql.SQLException;


public class dbWorker extends dbQuery {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private volatile Connection conn = null;
	private final int id;
	private volatile Boolean inUse = false;


	protected dbWorker(Connection conn) {
		this.conn = conn;
		this.id = getNextId();
	}


	protected Connection getConnection() {
		return conn;
	}


	// close connection
	@Override
	public void close() {
		super.close();
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException ignore) {}
		}
		conn = null;
	}
	// has errored / disconnected
	@Override
	public boolean hasClosed() {
		return (conn == null);
	}


	// in-use lock
	@Override
	public boolean inUse() {
		return (inUse == true);
	}
	@Override
	public boolean getLock() {
		if(inUse == true) return false;
		synchronized(inUse) {
			if(inUse == true) return false;
			inUse = true;
		}
		return true;
	}
	@Override
	public void release() {
		super.release();
		inUse = false;
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


}
