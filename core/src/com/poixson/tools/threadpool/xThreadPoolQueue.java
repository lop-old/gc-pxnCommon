package com.poixson.tools.threadpool;

import java.util.concurrent.Future;

import com.poixson.abstractions.xStartable;


public abstract class xThreadPoolQueue implements xStartable {

	public enum TaskPriority {
		LOW,
		NORM,
		HIGH
	};



	protected xThreadPoolQueue() {
	}








	public <V> Future<V> addTask(final Runnable run, final TaskPriority priority) {
//TODO:
return null;
	}










}
