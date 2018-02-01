package com.poixson.logger.commands;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.tools.events.xEventData;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class xCommandEvent extends xEventData {

	public final String commandStr;
	public final String[] args;
	public final boolean help;



	public xCommandEvent(final String line) {
		super();
		if (Utils.isEmpty(line)) throw new RequiredArgumentException("line");
		String str = line.trim();
		boolean ishelp = false;
		// starts with ?
		if (str.startsWith("?")) {
			ishelp = true;
			str = str.substring(1).trim();
			if (Utils.isEmpty(str)) {
				str = line.trim();
			}
		} else
		// ends with ?
		if (str.endsWith(" ?")) {
			ishelp = true;
			str = str.substring(0, str.length() - 1).trim();
			if (Utils.isEmpty(str)) {
				str = line.trim();
			}
		} else
		// starts with 'help'
		if (str.startsWith("help")) {
			ishelp = true;
			str = str.substring(4).trim();
			if (Utils.isEmpty(str)) {
				str = line.trim();
			}
		}
		this.commandStr = str;
		this.args = this.commandStr.split(" ");
		this.help = ishelp;
	}



	public int numArgs() {
		return this.args.length;
	}
	public String getArg(final int index) {
		if (index < 0) throw new ArrayIndexOutOfBoundsException("index cannot be less than 0");
		if (index > this.args.length-1)
			return null;
		return this.args[index];
	}



	@Override
	public String toString() {
		return "CMD:"+StringUtils.MergeStrings(' ', this.args);
	}



	public boolean isHelp() {
		return this.help;
	}



}
