package com.poixson.commonjava.xLogger.handlers;

import com.poixson.commonjava.EventListener.xEventData;
import com.poixson.commonjava.Utils.utils;


public class xCommandEvent extends xEventData {

	public final String commandStr;
	public final String[] args;
	public final boolean help;



	public xCommandEvent(final String line) {
		if(utils.isEmpty(line)) throw new NullPointerException();
		this.commandStr = line.trim();
		this.args = this.commandStr.split(" ");
		{
			final String firstArg = this.args[this.args.length - 1];
			this.help = (!utils.isEmpty(firstArg) && firstArg.equals("?"));
		}
	}


	public String arg(final int index) {
		if(index < 0) throw new ArrayIndexOutOfBoundsException();
		if(index > this.args.length-1) return null;
		return this.args[index];
	}


	public boolean isHelp() {
		return this.help;
	}


}
