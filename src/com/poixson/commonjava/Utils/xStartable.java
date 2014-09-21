package com.poixson.commonjava.Utils;


public interface xStartable extends Runnable {


	public boolean Start();
	public void    Stop();
	@Override
	public void run();
	public boolean isRunning();


}
