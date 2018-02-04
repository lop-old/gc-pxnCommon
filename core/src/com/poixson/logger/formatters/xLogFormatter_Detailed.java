package com.poixson.logger.formatters;

import com.poixson.logger.xLevel;
import com.poixson.logger.xLogRecord;
import com.poixson.utils.StringUtils;


public class xLogFormatter_Detailed extends xLogFormatter {



	public xLogFormatter_Detailed() {
	}



	@Override
	public String formatMsg(final xLogRecord record, final int lineIndex) {
		// [[ title ]]
		if (xLevel.TITLE.equals(record.level))
			return this.genTitle(record, lineIndex);
		// timestamp [level] [crumbs]
		return
			StringUtils.MergeStrings(
				' ',
				// timestamp
				this.genTimestamp(record, "D yyyy-MM-dd HH:mm:ss"),
				// [level]
				this.genLevel(record, "[", "]"),
				// [crumbs]
				this.genCrumbs(record, "[", "] [", "]"),
				// message
				this.genMessage(record, lineIndex)
			);
	}



}
