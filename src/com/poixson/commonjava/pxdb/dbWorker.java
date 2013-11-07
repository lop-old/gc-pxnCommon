package com.poixson.commonjava.pxdb;

import java.sql.Connection;
import java.sql.SQLException;


public class dbWorker {
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
			//System.out.println("LOCKING "+id);
			inUse = true;
		}
		return true;
	}
	public void release() {
		//System.out.println("RELEASING "+id);
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
