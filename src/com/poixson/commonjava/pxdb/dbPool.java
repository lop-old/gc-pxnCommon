package com.poixson.commonjava.pxdb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.poixson.commonjava.Utils.CoolDown;
import com.poixson.commonjava.Utils.utilsThread;


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


	protected dbPool(dbConfig config) {
		if(config == null) throw new NullPointerException("config object cannot be null");
		this.config = config;
		poolSize = new dbPoolSize(this);
		// force first connect
		getWorkerLock()
			.release();
	}


	// pool size
	public int getWorkerCount() {
		return workers.size();
	}


	// get db key
	public String getKey() {
		//if(config == null)
		//	return null;
		if(config == null) throw new NullPointerException("config object cannot be null");
		return config.getKey();
	}


	// pool is connected
	public boolean isConnected() {
		// TODO: this get a lock and releases. can probably be improved using isLocked()
		dbWorker worker = getExisting();
		if(worker == null)
			return false;
		worker.release();
		return true;
	}
	// ping the db server
	public boolean isConnectionValid() {
		dbWorker worker = getExisting();
		if(worker == null)
			return false;
		try {
			return worker.getConnection().isValid(1);
		} catch (SQLException ignore) {
		} finally {
			worker.release();
		}
		return false;
	}


	// get unused worker
	public dbWorker getWorkerLock() {
		dbWorker worker = null;
		synchronized(workers) {
			CoolDown maxHardBlocking = CoolDown.get("5s");
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
				if(count >= poolSize.getSoft())
					poolSize.StartWarningThread();
				// hard max
				if(count >= poolSize.getHard()) {
					poolSize.HardLimitWarningMessage();
					// stop waiting
					if(maxHardBlocking.runAgain()) {
						System.out.println("Failed to get a db connection! Blocked for "+maxHardBlocking.getDuration().toFullString()+".. Giving up.");
						return null;
					}
					// wait for a free worker
					utilsThread.Sleep(100L);
					continue;
				}
				// new worker/connection
				worker = newWorker();
				if(worker != null)
					return worker;
			}
		}
		return worker;
	}
	public dbWorker getExisting() {
		synchronized(workers) {
			if(workers.isEmpty())
				return null;
			// workers in pool
			Iterator<dbWorker> it = workers.iterator();
			while(it.hasNext()) {
				dbWorker worker = it.next();
				// errored or disconnected
				if(worker == null || worker.hasClosed()) {
					if(worker != null) {
						System.out.println("Connection [ "+Integer.toString(worker.getId())+" ] dropped");
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
else
System.out.println("FAILED TO GET LOCK!!!!");
			}
			return null;
		}
	}
	// new worker/connection
	private final CoolDown coolFail = CoolDown.get("2s");
	private dbWorker newWorker() {
		// hard limit reached
		if(getWorkerCount() >= poolSize.getHard())
			return null;
		// connect to db
		Connection conn = config.getConnection();
		// failed to connect
		if(conn == null) {
			if(coolFail.runAgain())
				System.out.println("Failed to connect to database! "+config.getKey());
			return null;
		}
		// successful connection
		dbWorker worker = new dbWorker(config.getKey(), conn);
		synchronized(workers) {
			workers.add(worker);
			if(worker.getLock())
				return worker;
		}
		return null;
	}


}
