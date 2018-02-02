package com.poixson.logger.formatters;

import com.poixson.logger.xLogRecord;
import com.poixson.utils.StringUtils;


public class xLogFormatter_Simple extends xLogFormatter {



	public xLogFormatter_Simple() {
	}



	@Override
	public String formatMsg(final xLogRecord record, final int lineIndex) {
		return
			StringUtils.MergeStrings(
				' ',
				// timestamp
				this.partTimestamp(record, "D HH:mm:ss"),
				// [level]
				this.partLevel(record, "[", "]"),
				// message
				this.partMessage(record, lineIndex)
			);
	}



}
