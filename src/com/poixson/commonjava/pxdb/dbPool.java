package com.poixson.commonjava.pxdb;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class dbPool {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	// connection config
	private final dbConfig config;

	// workers/connections
	private final List<dbWorker> workers = new ArrayList<dbWorker>();


	protected dbPool(dbConfig config) {
		this.config = config;
		// connect
		getWorker();
	}


	// get unused worker
	public dbWorker getWorker() {
		synchronized(workers) {
			if(workers.isEmpty())
				return newWorker();
			// workers in pool
			Iterator<dbWorker> it = workers.iterator();
			while(it.hasNext()) {
				dbWorker worker = it.next();
				// errored or disconnected
				if(worker == null || worker.hasError()) {
					if(worker != null)
						worker.close();
					it.remove();
					continue;
				}
				// in use
				if(worker.inUse())
					continue;
				// get lock
				if(worker.getLock())
					return worker;
			}
			// new worker/connection
			return newWorker();
		}
	}
	// new worker/connection
	private dbWorker newWorker() {
		Connection conn = config.getConnection();
		// failed to connect
		if(conn == null) {
			//TODO: log.severe("Failed to connect to database! "+config.getKey());
			return null;
		}
		// successful connection
		dbWorker worker = new dbWorker(conn);
		synchronized(workers) {
			workers.add(worker);
		}
		return worker;
	}


	public boolean isConnected() {
		dbWorker worker = getWorker();
		if(worker == null)
			return false;
		worker.release();
		return true;
	}


}
