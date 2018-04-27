package com.poixson.logger.formatters;

import com.poixson.logger.xLevel;
import com.poixson.logger.records.xLogRecord_Msg;
import com.poixson.utils.StringUtils;


public class xLogFormatter_Simple extends xLogFormatter {



	public xLogFormatter_Simple() {
		super();
	}



	@Override
	public String[] formatMessage(final xLogRecord_Msg record) {
		final xLevel level = record.getLevel();
		// publish plain message
		if (level == null) {
			return record.getLines();
		}
		// [[ title ]]
		if (xLevel.TITLE.equals(level))
			return this.genTitle(record);
		// format message lines
		final String[] result = new String[ record.lineCount ];
		for (int index=0; index<record.lineCount; index++) {
			// timestamp [level] message
			result[index] =
				StringUtils.MergeStrings(
					' ',
					// timestamp
					this.genTimestamp(record, "D HH:mm:ss"),
					// [level]
					this.genLevel(record, "[", "]"),
					// message
					this.genMessage(record, index)
				);
		}
		return result;
	}



}
