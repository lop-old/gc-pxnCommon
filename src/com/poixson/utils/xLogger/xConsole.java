package com.poixson.utils.xLogger;

import com.poixson.utils.xStartable;


public interface xConsole extends xStartable {


	@Override
	public void run();

	public void clear();
	public void flush();
	public void print(final String msg);
	public void drawPrompt();

	public void setPrompt(final String prompt);
	public String getPrompt();

	public String getBellStr();
	public jlineConsole.BellType getBell();
	public void setBell(final String bellStr);

	public void setCommandHandler(final xCommandHandler handler);


}
