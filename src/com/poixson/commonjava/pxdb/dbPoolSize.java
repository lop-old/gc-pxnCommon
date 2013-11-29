package com.poixson.commonjava.pxdb;

import com.poixson.commonjava.Utils.CoolDown;
import com.poixson.commonjava.Utils.utilsMath;
import com.poixson.commonjava.Utils.utilsThread;


public class dbPoolSize extends Thread {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	// max connections
	private volatile int SOFT = 5;
	private volatile int HARD = 10;

	private final dbPool pool;

	private volatile boolean running = false;
	private final Object lock = new Object();


	// hard/soft pool size limits
	protected dbPoolSize(dbPool pool) {
		if(pool == null) throw new NullPointerException("pool cannot be null");
		this.pool = pool;
		this.setName(pool.getKey()+" Warning Thread");
	}


	// pool size warnings
	public void StartWarningThread() {
		synchronized(lock) {
			if(running) return;
			running = true;
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
		System.out.println("Started warning thread.. "+this.getName());
		running = true;
//		synchronized(thread) {
//			if(running) return;
//			running = true;
//		}
		coolSoftLimit.reset();
		coolHardLimit.reset();
		int count = getWorkerCount();
		while(count > SOFT) {
			// try to close unused
			dbWorker worker = pool.getExisting();
			// check again after dropping closed workers
			count = getWorkerCount();
			if(count <= SOFT) {
				if(worker != null)
					worker.release();
				break;
			}
			// try closing a worker
			if(worker != null)
				worker.close();
			// warning message
			if(count >= HARD)
				HardLimitWarningMessage();
			else
			if(count > SOFT)
				SoftLimitWarningMessage();
			// sleep thread
			utilsThread.Sleep(250L);
		}
		System.out.println("Stopped warning thread. "+this.getName());
		running = false;
	}


	// warning messages (with cooldown)
	private final CoolDown coolSoftLimit = CoolDown.get("10s");
	protected void SoftLimitWarningMessage() {
		int count = getWorkerCount();
		if(count <= SOFT) return;
		// don't spam/flood console
		if(coolSoftLimit.runAgain())
			System.out.println("DB connection pool nearing limit! [ "+Integer.toString(count)+" max: "+Integer.toString(HARD)+" ] "+pool.getKey());
	}
	private final CoolDown coolHardLimit = CoolDown.get("2s");
	protected void HardLimitWarningMessage() {
		int count = getWorkerCount();
		if(count < HARD) return;
		// don't spam/flood console
		if(coolHardLimit.runAgain())
			System.out.println("DB connection pool HARD LIMIT REACHED!! [ "+Integer.toString(count)+" max: "+Integer.toString(HARD)+" ] "+pool.getKey());
	}


	// set hard/soft limits
	public void setSoft(int limit) {
		this.SOFT = utilsMath.MinMax(limit, 1, 1000);
	}
	public void setHard(int limit) {
		this.HARD = utilsMath.MinMax(limit, 1, 1000);
	}
	//get hard/soft limits
	public int getSoft() {
		return SOFT;
	}
	public int getHard() {
		return HARD;
	}


	// db connections count
	public int getWorkerCount() {
		return pool.getWorkerCount();
	}


}
