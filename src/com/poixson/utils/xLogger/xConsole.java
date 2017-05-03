package com.poixson.utils.xLogger;

import com.poixson.utils.xStartable;


public interface xConsole extends xStartable {


	@Override
	public void run();

	public void clear();
	public void flush();

	public void print(final String msg);

	public String getPrompt();
	public void setPrompt(final String prompt);
	public void drawPrompt();

	public void setCommandHandler(final xCommandHandler handler);


}
