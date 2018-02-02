package com.poixson.logger.formatters;

import com.poixson.logger.xLogRecord;
import com.poixson.utils.StringUtils;


public class xLogFormatter_Detailed extends xLogFormatter {



	public xLogFormatter_Detailed() {
	}



	@Override
	public String formatMsg(final xLogRecord record, final int lineIndex) {
		// timestamp [level] [crumbs]
		return
			StringUtils.MergeStrings(
				' ',
				// timestamp
				this.partTimestamp(record, "D yyyy-MM-dd HH:mm:ss"),
				// [level]
				this.partLevel(record, "[", "]"),
				// [crumbs]
				this.partCrumbs(record, "[", "] [", "]"),
				// message
				this.partMessage(record, lineIndex)
			);
	}



}
