package com.poixson.commonjava.xLogger.formatters;

import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.xLogger.xLogFormatter;
import com.poixson.commonjava.xLogger.xLogRecord;


public class defaultLogFormatter implements xLogFormatter {


	@Override
	public String formatMsg(xLogRecord record) {
		String[] parts = new String[4];
		// timestamp
		parts[0] = partTimestamp(record);
		// level
		parts[1] = partLevel(record);
		// [logger] [crumbs]
		parts[2] = partCrumbs(record);
		// message
		parts[3] = partMessage(record);
		return utilsString.addArray("", parts, " ");
	}


	// timestamp
	protected String partTimestamp(xLogRecord record) {
		return "xx:xx:xx";
	}
	// level
	protected String partLevel(xLogRecord record) {
		return record.getLevel().toString();
	}
	// [logger] [crumbs]
	protected String partCrumbs(xLogRecord record) {
		StringBuilder crumbs = new StringBuilder();
		for(String name : record.getNameTree()) {
			if(name == null || name.isEmpty())
				continue;
			if(crumbs.length() > 0)
				crumbs.append(" ");
			crumbs.append("[").append(name).append("]");
		}
		return crumbs.toString();
	}
	// raw message
	protected String partMessage(xLogRecord record) {
		return record.getMsg();
	}


}
