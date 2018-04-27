package com.poixson.logger;

import com.poixson.abstractions.xStartable;


public interface xConsole extends xStartable {


	public void doPublish(final String line);
	public void doClearScreen();
	public void doFlush();
	public void doBeep();

	public String getPrompt();
	public void setPrompt(final String prompt);
	public void drawPrompt();

	public Character getMask();
	public void setMask(final Character mask);


}
