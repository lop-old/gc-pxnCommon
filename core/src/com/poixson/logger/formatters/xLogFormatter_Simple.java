package com.poixson.logger.formatters;

import com.poixson.logger.xLevel;
import com.poixson.logger.xLogRecord;
import com.poixson.utils.StringUtils;


public class xLogFormatter_Simple extends xLogFormatter {



	public xLogFormatter_Simple() {
	}



	@Override
	public String formatMsg(final xLogRecord record, final int lineIndex) {
		// [[ title ]]
		if (xLevel.TITLE.equals(record.level))
			return this.genTitle(record, lineIndex);
		return
			StringUtils.MergeStrings(
				' ',
				// timestamp
				this.genTimestamp(record, "D HH:mm:ss"),
				// [level]
				this.genLevel(record, "[", "]"),
				// message
				this.genMessage(record, lineIndex)
			);
	}



}
