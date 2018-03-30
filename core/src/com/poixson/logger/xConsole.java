package com.poixson.logger;

import com.poixson.abstractions.xStartable;


public interface xConsole extends xStartable {


	public void clearScreen();
	public void clearLine();
	public void flush();
	public void beep();

	public void publish(final String line);
	public void publish();

	public String getPrompt();
	public void setPrompt(final String prompt);
	public void drawPrompt();

	public Character getMask();
	public void setMask(final Character mask);


}
