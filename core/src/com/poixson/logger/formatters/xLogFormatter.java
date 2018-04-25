package com.poixson.logger.formatters;

import java.text.SimpleDateFormat;

import com.poixson.logger.xLevel;
import com.poixson.logger.xLogRecord;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class xLogFormatter {



	public xLogFormatter() {
	}



	public String[] formatMessage(final xLogRecord record) {
		// [[ title ]]
		if (xLevel.TITLE.equals(record.level))
			return this.genTitle(record);
		// message only
		return record.getLines();
	}



	// ------------------------------------------------------------------------------- //
	// generate parts



	// title
	protected String[] genTitle(final xLogRecord record) {
		return
			this.genTitle(
				record,
				" [[ ",
				" ]] "
			);
	}
	protected String[] genTitle(final xLogRecord record,
			final String preStr, final String postStr) {
		if (record.isEmpty()) {
			final String msg =
				(new StringBuilder())
					.append(preStr)
					.append("<null>")
					.append(postStr)
					.toString();
			return new String[] { msg };
		}
		final int len = record.getLongestLine();
		final String[] result = new String[ record.lineCount ];
		for (int index=0; index<record.lineCount; index++) {
			final String line =
				StringUtils.PadEnd(
					len,
					record.getLine(index),
					' '
				);
			result[index] =
				(new StringBuilder())
				.append(preStr)
				.append(line)
				.append(postStr)
				.toString();
		}
		return result;
	}



	// timestamp
	protected String genTimestamp(final xLogRecord record, final String format,
			final String preStr, final String postStr) {
		return (new StringBuilder())
			.append(preStr)
			.append( this.genTimestamp(record, format) )
			.append(postStr)
			.toString();
	}
	protected String genTimestamp(final xLogRecord record, final String format) {
		final SimpleDateFormat dateFormat =
			new SimpleDateFormat(format);
		return
			dateFormat.format(
				Long.valueOf(record.timestamp)
			);
	}



	// level
	protected String genLevel(final xLogRecord record,
			final String preStr, final String postStr) {
		return (new StringBuilder())
			.append(preStr)
			.append( this.genLevel(record) )
			.append(postStr)
			.toString();
	}
	protected String genLevel(final xLogRecord record) {
		return StringUtils.PadCenter(7, record.getLevelStr(), ' ');
	}



	// crumbs
	protected String genCrumbs(final xLogRecord record,
			final String preStr, final String midStr, final String postStr) {
		final String[] tree = record.getNameTree();
		if (Utils.isEmpty(tree)) return "";
		final String result = StringUtils.MergeStrings(midStr, tree);
		if (Utils.isEmpty(result)) return "";
		return (new StringBuilder())
			.append(preStr)
			.append(result)
			.append(postStr)
			.toString();
	}



	// message
	protected String genMessage(final xLogRecord record, final int lineIndex) {
		return record.getLine(lineIndex);
	}



}
