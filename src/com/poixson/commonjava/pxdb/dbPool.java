package com.poixson.commonjava.pxdb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.poixson.commonjava.Utils.CoolDown;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.xLogger.xLog;


public class dbPool {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	// connection config
	private final dbConfig config;

	// workers/connections
	private final List<dbWorker> workers = new ArrayList<dbWorker>();
	// hard/soft pool size limits
	private final dbPoolSize poolSize;


	protected dbPool(final dbConfig config) {
		if(config == null) throw new NullPointerException("config object cannot be null");
		this.config = config;
		this.poolSize = new dbPoolSize(this);
// handled in dbManager::register()
//		// force first connect
//		getWorkerLock()
//			.free();
	}


	// pool size
	public int getWorkerCount() {
		return this.workers.size();
	}


	// get db key
	public String dbKey() {
		if(this.config == null) throw new NullPointerException("config object cannot be null");
		return this.config.dbKey();
	}


	// pool is connected
	public boolean isConnected() {
		// TODO: this get a lock and releases. can probably be improved using isLocked()
		final dbWorker worker = getExisting();
		if(worker == null)
			return false;
		worker.free();
		return true;
	}
	// ping the db server
	public boolean isConnectionValid() {
		final dbWorker worker = getExisting();
		if(worker == null)
			return false;
		try {
			return worker.getConnection().isValid(1);
		} catch (SQLException ignore) {
		} finally {
			worker.free();
		}
		return false;
	}


	// get unused worker
	public dbWorker getWorkerLock() {
		dbWorker worker = null;
		synchronized(this.workers) {
			final CoolDown maxHardBlocking = CoolDown.get("5s");
			maxHardBlocking.runAgain();
			int count = 0;
			while(true) {
				count = getWorkerCount();
				// get existing connection
				if(count > 0) {
					worker = getExisting();
					if(worker != null)
						break;
					count = getWorkerCount();
				}
				// soft max
				if(count >= this.poolSize.getSoft())
					this.poolSize.StartWarningThread();
				// hard max
				if(count >= this.poolSize.getHard()) {
					this.poolSize.HardLimitWarningMessage();
					// stop waiting
					if(maxHardBlocking.runAgain()) {
						log().severe("Failed to get a db connection! Blocked for "+maxHardBlocking.getDuration().toFullString()+".. Giving up.");
						return null;
					}
					// wait for a free worker
					utilsThread.Sleep(100L);
					continue;
				}
				// new worker/connection
				worker = newWorker();
				if(worker == null)
					return null;
				return worker;
			}
		}
		return worker;
	}
	public dbWorker getExisting() {
		synchronized(this.workers) {
			if(this.workers.isEmpty())
				return null;
			// workers in pool
			final Iterator<dbWorker> it = this.workers.iterator();
			while(it.hasNext()) {
				final dbWorker worker = it.next();
				// errored or disconnected
				if(worker == null || worker.isClosed()) {
					if(worker != null) {
						log().warning("Connection [ "+Integer.toString(worker.getId())+" ] dropped");
						worker.close();
					}
					it.remove();
					continue;
				}
				// in use
				if(worker.inUse())
					continue;
				// get lock
				if(worker.getLock()) {
//					if(debug)
//						log.debug("db", "Connection pool size: "+Integer.toString(pool.size()));
					return worker;
				}
			}
			return null;
		}
	}
	// new worker/connection
//	private final CoolDown coolFail = CoolDown.get("2s");
	private dbWorker newWorker() {
		// hard limit reached
		if(getWorkerCount() >= this.poolSize.getHard())
			return null;
		// connect to db
		final Connection conn = this.config.getConnection();
		// failed to connect
//		if(conn == null) {
//			if(this.coolFail.runAgain())
//				log().severe("Failed to connect to database! "+this.config.dbKey());
//			return null;
//		}
		if(conn == null)
			return null;
		// successful connection
		final dbWorker worker = new dbWorker(this.config.dbKey(), conn);
		synchronized(this.workers) {
			this.workers.add(worker);
			if(!worker.getLock())
				return null;
		}
		return worker;
	}


	// logger
	public static xLog log() {
		return dbManager.log();
	}


}
