package com.poixson.utils.xLogger;

import java.text.SimpleDateFormat;
import java.util.List;

import com.poixson.utils.StringUtils;
import com.poixson.utils.exceptions.RequiredArgumentException;


public class xLogFormatter_Default implements xLogFormatter {



	@Override
	public String formatMsg(final xLogRecord record) {
		if (record == null) throw new RequiredArgumentException("record");
		if (record.level() == null) {
			return record.msg();
		}
		String[] parts = new String[4];
		// timestamp
		parts[0] = partTimestamp(record);
		// level
		parts[1] = partLevel(record);
		// [logger] [crumbs]
		parts[2] = partCrumbs(record);
		// message
		parts[3] = partMessage(record);
		return StringUtils.AddArray(" ", parts);
	}



	// timestamp
	protected String partTimestamp(final xLogRecord record) {
		if (record == null) throw new RequiredArgumentException("record");
		final SimpleDateFormat dateFormat = new SimpleDateFormat("D yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(new Long(record.timestamp()));
	}
	// level
	protected String partLevel(final xLogRecord record) {
		if (record == null) throw new RequiredArgumentException("record");
		return (new StringBuilder())
			.append("[")
			.append(StringUtils.PadCenter(
				7,
				record.level().toString(),
				' '
			))
			.append("]")
			.toString();
	}
	// [logger] [crumbs]
	protected String partCrumbs(final xLogRecord record) {
		if (record == null) throw new RequiredArgumentException("record");
		final List<String> tree = record.getNameTree();
		if (tree.isEmpty())
			return "";
		final StringBuilder crumbs = new StringBuilder();
		boolean first = true;
		crumbs.append("@|FG_BLACK,BOLD ");
		for (String name : tree) {
			if (name == null || name.isEmpty())
				continue;
			if (first) {
				first = false;
			} else {
				crumbs.append(' ');
			}
			crumbs.append("[").append(name).append("]");
		}
		crumbs.append("|@");
		return crumbs.toString();
	}
	// raw message
	protected String partMessage(final xLogRecord record) {
		return record.msg();
	}



}
