package com.poixson.commonjava.xLogger.formatters;

import java.text.SimpleDateFormat;

import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.xLogger.xLogFormatter;
import com.poixson.commonjava.xLogger.xLogRecord;


public class defaultLogFormatter implements xLogFormatter {



	@Override
	public String formatMsg(final xLogRecord record) {
		if(record == null) throw new NullPointerException();
		String[] parts = new String[4];
		// timestamp
		parts[0] = partTimestamp(record);
		// level
		parts[1] = partLevel(record);
		// [logger] [crumbs]
		parts[2] = partCrumbs(record);
		// message
		parts[3] = partMessage(record);
		return utilsString.addArray(" ", parts);
	}



	// timestamp
	protected String partTimestamp(final xLogRecord record) {
		if(record == null) throw new NullPointerException();
		SimpleDateFormat dateFormat = new SimpleDateFormat("D yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(new Long(record.timestamp()));
	}
	// level
	protected String partLevel(final xLogRecord record) {
		if(record == null) throw new NullPointerException();
		return (new StringBuilder())
			.append("[")
			.append(utilsString.padCenter(
				7,
				record.level().toString(),
				' '
			))
			.append("]")
			.toString();
	}
	// [logger] [crumbs]
	protected String partCrumbs(final xLogRecord record) {
		if(record == null) throw new NullPointerException();
		final StringBuilder crumbs = new StringBuilder();
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
	protected String partMessage(final xLogRecord record) {
		return record.msg();
	}



}
