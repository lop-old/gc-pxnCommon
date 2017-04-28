package com.poixson.utils.xThreadPool;

import java.util.Iterator;

import com.poixson.utils.NumberUtils;
import com.poixson.utils.xLogger.xLevel;


public class xThreadPoolStats {

	private final xThreadPool pool;



	public xThreadPoolStats(final xThreadPool pool) {
		this.pool = pool;
	}



	public void displayStats(final xLevel level) {
		this.pool.log()
			.publish(
				level,
				(new StringBuilder())
					.append("Queued: [")
						.append(this.getQueueCount())
						.append("]  ")
					.append("Threads: ")
						.append(this.getCurrentThreadCount())
						.append(" [")
						.append(this.getMaxThreads())
						.append("]  ")
					.append("Active/Free: ")
						.append(this.getActiveThreadCount())
						.append("/")
						.append(this.getInactiveThreadCount())
						.append("  ")
					.append("Global: ")
						.append(getGlobalThreadCount())
						.append(" [")
						.append(getGlobalMaxThreads())
						.append("]")
						.toString()
			);
	}



	public int getCurrentThreadCount() {
		return this.pool
				.threadCount.get();
	}
	public static int getGlobalThreadCount() {
		if (xThreadPoolFactory.pools.isEmpty())
			return 0;
		int count = 0;
		final Iterator<xThreadPool> it =
			xThreadPoolFactory.pools
				.values().iterator();
		while (it.hasNext()) {
			count += it.next()
					.getStats().getCurrentThreadCount();
		}
		return count;
	}



	public int getActiveThreadCount() {
		return this.pool
				.active.get();
	}
	public int getInactiveThreadCount() {
		return NumberUtils.MinMax(
			this.getCurrentThreadCount() - this.getActiveThreadCount(),
			0,
			this.getMaxThreads()
		);
	}



	public int getMaxThreads() {
		return this.pool
				.poolSize;
	}
	public static int getGlobalMaxThreads() {
		return xThreadPool
				.GLOBAL_LIMIT;
	}
//TODO:
//	public int getThreadsFree() {
//	}
//	public static int getGlobalThreadsFree() {
//	}



	public int getQueueCount() {
		return this.pool
				.queue.size();
	}



}
