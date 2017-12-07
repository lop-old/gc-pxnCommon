package com.poixson.utils.pxdb;

import java.util.concurrent.atomic.AtomicBoolean;

import com.poixson.utils.CoolDown;
import com.poixson.utils.NumberUtils;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLog;


public class dbPoolSize extends Thread {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	// max connections
	private volatile int SOFT = 5;
	private volatile int HARD = 8;

	private final CoolDown coolSoftLimit = CoolDown.getNew("10s");
	private final CoolDown coolHardLimit = CoolDown.getNew("2s");

	private final dbPool pool;

	private final AtomicBoolean running = new AtomicBoolean(false);



	// hard/soft pool size limits
	protected dbPoolSize(final dbPool pool) {
		if (pool == null) throw RequiredArgumentException.getNew("pool");
		this.pool = pool;
		this.setName(pool.dbKey()+" Warning Thread");
	}



	// pool size warnings
	public void startWarningThread() {
		if (!this.running.compareAndSet(false, true))
			return;
		if (!this.isAlive()) {
			this.start();
		}
//TODO:
//switch (thread.getState()) {
//case NEW:           System.out.println("NEW"); break;
//case RUNNABLE:      System.out.println("RUNNABLE"); break;
//case BLOCKED:       System.out.println("BLOCKED"); break;
//case WAITING:       System.out.println("WAITING"); break;
//case TIMED_WAITING: System.out.println("TIMED_WAITING"); break;
//case TERMINATED:    System.out.println("TERMINATED"); break;
//}
//System.out.println("STARTIING THREAD");
	}
	@Override
	public void run() {
		log().finest("Started warning thread.. {}", this.getName());
		this.coolSoftLimit.reset();
		this.coolHardLimit.reset();
		int count = getWorkerCount();
		while (count > this.SOFT) {
			// try to close unused
			final dbWorker worker = this.pool.getLockedFromExisting();
			// check again after dropping closed workers
			count = getWorkerCount();
			if (count <= this.SOFT) {
				if (worker != null) {
					worker.free();
				}
				break;
			}
			// try closing a worker
			if (worker != null) {
				worker.close();
			}
			// warning message
			if (count >= this.HARD) {
				hardLimitWarningMessage();
			} else
			if (count > this.SOFT) {
				softLimitWarningMessage();
			}
			// sleep thread
			ThreadUtils.Sleep(250L);
		}
		log().finest("Stopped warning thread. {}", this.getName());
		this.running.set(false);
	}



	// warning messages (with cooldown)
	protected void softLimitWarningMessage() {
		final int count = getWorkerCount();
		if (count <= this.SOFT)
			return;
		// don't spam/flood console
		if (this.coolSoftLimit.runAgain()) {
			log().warning(
				"DB connection pool nearing limit! [ {} max: {} ] {}",
				Integer.toString(count),
				Integer.toString(this.HARD),
				this.pool.dbKey()
			);
		}
	}
	protected void hardLimitWarningMessage() {
		final int count = getWorkerCount();
		if (count < this.HARD)
			return;
		// don't spam/flood console
		if (this.coolHardLimit.runAgain()) {
			log().severe(
				"DB connection pool HARD LIMIT REACHED!! [ {} max: {} ] {}",
				Integer.toString(count),
				Integer.toString(this.HARD),
				this.pool.dbKey()
			);
		}
	}



	// set hard/soft limits
	public void setSoft(final int limit) {
		this.SOFT =
			NumberUtils.MinMax(
				limit,
				1,
				1000
			);
	}
	public void setHard(final int limit) {
		this.HARD =
			NumberUtils.MinMax(
				limit,
				1,
				1000
			);
	}
	//get hard/soft limits
	public int getSoft() {
		return this.SOFT;
	}
	public int getHard() {
		return this.HARD;
	}



	// db connections count
	public int getWorkerCount() {
		return this.pool.getWorkerCount();
	}



	// logger
	public static xLog log() {
		return dbManager.log();
	}



}
