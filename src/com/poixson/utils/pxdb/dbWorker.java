package com.poixson.utils.pxdb;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.sql.SQLException;

import com.poixson.utils.Utils;
import com.poixson.utils.xCloseable;
import com.poixson.utils.xLogger.xLog;


public class dbWorker implements xCloseable {

	private final String dbKey;
	private final int id;
	private final AtomicReference<String> desc = new AtomicReference<String>(null);

	private volatile Connection conn = null;
	private final AtomicBoolean inUse = new AtomicBoolean(false);



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
		if (config == null)
			return null;
		return config.getTablePrefix();
	}



	// close connection
	@Override
	public void close() {
		if (this.conn != null) {
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
		return this.inUse.get();
	}
	public boolean getLock() {
		final boolean result = this.inUse.compareAndSet(false, true);
		if (result) {
			log().finest(
				(new StringBuilder())
					.append("Locked #")
					.append(this.getIndex())
					.toString()
			);
		}
		return result;
	}
	public void free() {
		// flush desc
		this.logDesc();
		// release lock
		if (this.inUse.compareAndSet(true, false)) {
			log().finest(
				(new StringBuilder())
					.append("Released #")
					.append(this.getIndex())
					.toString()
			);
		}
	}
//TODO:
//	/**
//	 * Get the time connection has been locked for
//	 * @return time in milliseconds
//	 */
//	@Override
//	public long getLockTime() {
//TODO: this isn't being used yet
//		if (lockTime < 1)
//			return -1;
//		return pxnUtils.getCurrentMillis() - lockTime;
//	}



	// query description
	public void desc(final String descStr) {
		this.desc.set(
			Utils.isBlank(descStr)
			? null
			: descStr
		);
	}
	public void logDesc() {
		final String desc = this.desc.get();
		if (Utils.isBlank(desc))
			return;
		if (!this.desc.compareAndSet(desc, null))
			return;
		log().fine(
			(new StringBuilder())
				.append("Query: ")
				.append(desc)
				.toString()
		);
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
