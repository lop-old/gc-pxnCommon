package com.poixson.commonjava.pxdb;

import com.poixson.commonjava.Utils.CoolDown;
import com.poixson.commonjava.Utils.utilsMath;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.xLogger.xLog;


public class dbPoolSize extends Thread {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	// max connections
	private volatile int SOFT = 5;
	private volatile int HARD = 8;

	private final dbPool pool;

	private volatile boolean running = false;
	private final Object lock = new Object();



	// hard/soft pool size limits
	protected dbPoolSize(final dbPool pool) {
		if(pool == null) throw new NullPointerException("pool cannot be null");
		this.pool = pool;
		this.setName(pool.dbKey()+" Warning Thread");
	}



	// pool size warnings
	public void StartWarningThread() {
		synchronized(this.lock) {
			if(this.running) return;
			this.running = true;
//			this.setName(pool.getKey()+" Warning Thread");
			if(!this.isAlive())
				this.start();
		}
//switch(thread.getState()) {
//case NEW:System.out.println("NEW");break;case RUNNABLE:System.out.println("RUNNABLE");break;case BLOCKED:System.out.println("BLOCKED");break;
//case WAITING:System.out.println("WAITING");break;case TIMED_WAITING:System.out.println("TIMED_WAITING");break;case TERMINATED:System.out.println("TERMINATED");break;}
//System.out.println("STARTIING THREAD");
	}
	@Override
	public void run() {
		log().finer("Started warning thread.. "+this.getName());
		this.running = true;
//		synchronized(thread) {
//			if(running) return;
//			running = true;
//		}
		this.coolSoftLimit.reset();
		this.coolHardLimit.reset();
		int count = getWorkerCount();
		while(count > this.SOFT) {
			// try to close unused
			final dbWorker worker = this.pool.getExisting();
			// check again after dropping closed workers
			count = getWorkerCount();
			if(count <= this.SOFT) {
				if(worker != null)
					worker.free();
				break;
			}
			// try closing a worker
			if(worker != null)
				worker.close();
			// warning message
			if(count >= this.HARD)
				HardLimitWarningMessage();
			else
			if(count > this.SOFT)
				SoftLimitWarningMessage();
			// sleep thread
			utilsThread.Sleep(250L);
		}
		log().finer("Stopped warning thread. "+this.getName());
		this.running = false;
	}



	// warning messages (with cooldown)
	private final CoolDown coolSoftLimit = CoolDown.get("10s");
	protected void SoftLimitWarningMessage() {
		final int count = getWorkerCount();
		if(count <= this.SOFT) return;
		// don't spam/flood console
		if(this.coolSoftLimit.runAgain())
			log().warning("DB connection pool nearing limit! [ "+Integer.toString(count)+" max: "+Integer.toString(this.HARD)+" ] "+this.pool.dbKey());
	}
	private final CoolDown coolHardLimit = CoolDown.get("2s");
	protected void HardLimitWarningMessage() {
		final int count = getWorkerCount();
		if(count < this.HARD) return;
		// don't spam/flood console
		if(this.coolHardLimit.runAgain())
			log().severe("DB connection pool HARD LIMIT REACHED!! [ "+Integer.toString(count)+" max: "+Integer.toString(this.HARD)+" ] "+this.pool.dbKey());
	}



	// set hard/soft limits
	public void setSoft(final int limit) {
		this.SOFT = utilsMath.MinMax(limit, 1, 1000);
	}
	public void setHard(final int limit) {
		this.HARD = utilsMath.MinMax(limit, 1, 1000);
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
