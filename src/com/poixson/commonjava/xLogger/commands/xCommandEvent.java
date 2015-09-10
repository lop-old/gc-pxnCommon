package com.poixson.commonjava.xLogger.commands;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.xEvents.xEventData;


public class xCommandEvent extends xEventData {

	public final String commandStr;
	public final String[] args;
	public final boolean help;



	public xCommandEvent(final String line) {
		super();
		if(utils.isEmpty(line)) throw new NullPointerException("line argument is required!");
		String str = line.trim();
		boolean ishelp = false;
		// starts with ?
		if(str.startsWith("?")) {
			ishelp = true;
			str = str.substring(1).trim();
			if(utils.isEmpty(str))
				str = line.trim();
		// ends with ?
		} else if(str.endsWith(" ?")) {
			ishelp = true;
			str = str.substring(0, str.length() - 1).trim();
			if(utils.isEmpty(str))
				str = line.trim();
		// starts with 'help'
		} else if(str.startsWith("help")) {
			ishelp = true;
			str = str.substring(4).trim();
			if(utils.isEmpty(str))
				str = line.trim();
		}
		this.commandStr = str;
		this.args = this.commandStr.split(" ");
		this.help = ishelp;
	}



	public String getArg(final int index) {
		if(index < 0) throw new ArrayIndexOutOfBoundsException("index cannot be less than 0");
		if(index > this.args.length-1) return null;
		return this.args[index];
	}



	public boolean isHelp() {
		return this.help;
	}



}
