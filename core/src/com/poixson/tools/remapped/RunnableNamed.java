package com.poixson.tools.remapped;


public interface RunnableNamed extends Runnable {


	public String getTaskName();
	public void setTaskName(final String name);
	public boolean taskNameEquals(final String name);


}
