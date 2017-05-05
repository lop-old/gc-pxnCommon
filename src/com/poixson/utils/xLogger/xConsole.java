package com.poixson.utils.xLogger;

import com.poixson.utils.xStartable;


public interface xConsole extends xStartable {


	@Override
	public void Start();
	@Override
	public void Stop();

	@Override
	public void run();
	@Override
	public boolean isRunning();
	@Override
	public boolean isStopping();


	public void clear();
	public void clearLine();
	public void flush();

	public void print(final String msg);

	public String getPrompt();
	public void setPrompt(final String prompt);
	public void drawPrompt();

	public Character getMask();
	public void setMask(final Character mask);

	public xCommandHandler getCommandHandler();


}
