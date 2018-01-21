package com.poixson.tools.remapped;

import java.util.concurrent.Callable;


public interface RunnableNamed extends Runnable {



	public String getTaskName();
	public void setTaskName(final String name);
	public boolean taskNameEquals(final String name);



	public static String GetName(final Runnable run) {
		if (run == null)
			return null;
		if (run instanceof RunnableNamed)
			return ((RunnableNamed) run).getTaskName();
		return null;
	}
	public static String GetName(final Callable<?> call) {
		if (call == null)
			return null;
		if (call instanceof RunnableNamed)
			return ((RunnableNamed) call).getTaskName();
		return null;
	}



}
