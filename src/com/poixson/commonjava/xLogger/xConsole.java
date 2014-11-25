package com.poixson.commonjava.xLogger;

import com.poixson.commonjava.EventListener.xHandler;
import com.poixson.commonjava.Utils.xStartable;


public interface xConsole extends Runnable, xStartable {


	@Override
	public void run();

	public void clear();
	public void flush();
	public void print(final String msg);
	public void drawPrompt();

	public void setPrompt(final String prompt);
	public String getPrompt();

	public void setCommandHandler(final xHandler handler);


}
