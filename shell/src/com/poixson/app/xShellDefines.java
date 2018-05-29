package com.poixson.app;

import com.poixson.tools.Keeper;


public class xShellDefines {
	private xShellDefines() {}
	static { Keeper.add(new xShellDefines()); }


	public static final String HISTORY_FILE = "history.txt";
	public static final int    HISTORY_SIZE = 1000;

	public static final String  DEFAULT_PROMPT = " #>";
	public static final boolean BELL_ENABLED   = true;


}
