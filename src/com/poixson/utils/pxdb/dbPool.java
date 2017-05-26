package com.poixson.utils.pxdb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.poixson.utils.CoolDown;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.xTime;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLog;


public class dbPool {

	private static final xTime MAX_HARD_BLOCKING = xTime.get("5s");

	// connection config
	private final dbConfig config;

	// workers/connections
	private final CopyOnWriteArraySet<dbWorker> workers =
			new CopyOnWriteArraySet<dbWorker>();
	// hard/soft pool size limits
	private final dbPoolSize poolSize;



	protected dbPool(final dbConfig config) {
		if (config == null) throw new RequiredArgumentException("config");
		this.config   = config;
		this.poolSize = new dbPoolSize(this);
		this.poolSize.setSoft(config.getPoolSizeWarn());
		this.poolSize.setHard(config.getPoolSizeHard());
	}



	// pool size
	public int getWorkerCount() {
		return this.workers.size();
	}



	// get db key
	public String dbKey() {
		return this.config.dbKey();
	}
	// db config
	public dbConfig getConfig() {
		return this.config;
	}



	// pool is connected
	public boolean isConnected() {
// TODO: this get a lock and releases. can probably be improved using isLocked()
		final dbWorker worker = this.getLockedFromExisting();
		if (worker == null)
			return false;
		worker.free();
		return true;
	}
	// ping the db server
	public boolean isConnectionValid() {
		final dbWorker worker = this.getLockedFromExisting();
		if (worker == null)
			return false;
		try {
			return worker.getConnection().isValid(1);
		} catch (SQLException ignore) {}
		worker.free();
		return false;
	}



	// get unused worker
	public dbWorker getLockedWorker() {
		final CoolDown maxHardBlocking = CoolDown.get(MAX_HARD_BLOCKING);
		maxHardBlocking.resetRun();
		while (true) {
			// use existing connection
			if (!this.workers.isEmpty()) {
				final dbWorker worker = this.getLockedFromExisting();
				if (worker != null)
					return worker;
			}
			// soft max
			final int count = this.getWorkerCount();
			if (count >= this.poolSize.getSoft()) {
				this.poolSize.StartWarningThread();
			}
			// hard max
			if (count >= this.poolSize.getHard()) {
				this.poolSize.HardLimitWarningMessage();
				// give up waiting
				if (maxHardBlocking.runAgain()) {
					log().severe(
						"Failed to get a db connection! Blocked for {}.. Giving up!",
						maxHardBlocking.getDuration().toFullString()
					);
					return null;
				}
				// wait for a free worker
				ThreadUtils.Sleep(100L);
				continue;
			}
			// new worker/connection
			{
				final dbWorker worker = this.newLockedWorker();
				if (worker != null)
					return worker;
			}
		}
	}
	public dbWorker getLockedFromExisting() {
		if (this.workers.isEmpty())
			return null;
		dbWorker output = null;
		final Set<dbWorker> removing = new HashSet<dbWorker>();
		{
			final Iterator<dbWorker> it = this.workers.iterator();
			while (it.hasNext()) {
				final dbWorker worker = it.next();
				// errored or disconnected
				if (worker == null || worker.isClosed()) {
					if (worker != null) {
						log().warning(
							"Connection [ {} ] dropped",
							Integer.toString(worker.getIndex())
						);
						worker.close();
					}
					removing.add(worker);
					continue;
				}
				// in use
				if (worker.inUse())
					continue;
				// get lock
				if (worker.getLock()) {
					output = worker;
					break;
				}
			}
		}
		// removed workers
		if (!removing.isEmpty()) {
			final Iterator<dbWorker> it = removing.iterator();
			while (it.hasNext()) {
				this.workers.remove(it.next());
			}
		}
		return output;
	}
	// new worker/connection
//	private final CoolDown coolFail = CoolDown.get("2s");
	private dbWorker newLockedWorker() {
		// hard limit reached
		if (getWorkerCount() >= this.poolSize.getHard())
			return null;
		// connect to db
		final Connection conn = this.config.getConnection();
//TODO:
//		// failed to connect
//		if (conn == null) {
//			if (this.coolFail.runAgain()) {
//				log().severe("Failed to connect to database! {}", this.config.dbKey());
//			}
//			return null;
//		}
		if (conn == null)
			return null;
		// successful connection
		final dbWorker worker =
			new dbWorker(
				this.dbKey(),
				conn
			);
		this.workers.add(worker);
		if (!worker.getLock())
			return null;
		return worker;
	}



	// logger
	public static xLog log() {
		return dbManager.log();
	}



}
