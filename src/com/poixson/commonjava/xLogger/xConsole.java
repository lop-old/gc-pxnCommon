package com.poixson.commonjava.xLogger;


public interface xConsole extends Runnable {

	public void start();
	public void stop();
	public void shutdown();

	@Override
	public void run();
	public void doCommand(final String line);

	public void clear();
	public void flush();
	public void print(final String msg);
	public void redraw();

	public void setPrompt(final String prompt);
	public String getPrompt();

}
