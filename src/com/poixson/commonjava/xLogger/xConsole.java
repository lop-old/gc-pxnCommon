package com.poixson.commonjava.xLogger;


public interface xConsole extends Runnable {

	public void start();
	public void stop();

	@Override
	public void run();

	public void clear();
	public void flush();
	public void print(final String msg);
	public void drawPrompt();

	public void setPrompt(final String prompt);
	public String getPrompt();

}
