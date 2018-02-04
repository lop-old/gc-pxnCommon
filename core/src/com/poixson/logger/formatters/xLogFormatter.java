package com.poixson.logger.formatters;

import java.text.SimpleDateFormat;

import com.poixson.logger.xLevel;
import com.poixson.logger.xLogRecord;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class xLogFormatter {



	public String formatMsg(final xLogRecord record, final int lineIndex) {
		// [[ title ]]
		if (xLevel.TITLE.equals(record.level))
			return this.genTitle(record, lineIndex);
		// message only
		return this.genMessage(record, lineIndex);
	}



	// ------------------------------------------------------------------------------- //
	// generate parts



	// title
	protected String genTitle(final xLogRecord record, final int lineIndex) {
		return
			this.genTitle(
				record,
				lineIndex,
				" [[ ",
				" ]] "
			);
	}
	protected String genTitle(final xLogRecord record, final int lineIndex,
			final String preStr, final String postStr) {
		if (record.isEmpty()) {
			return (new StringBuilder())
				.append(preStr)
				.append("<null>")
				.append(postStr)
				.toString();
		}
		final int len = record.getLongestLine();
		return (new StringBuilder())
			.append(preStr)
			.append(
				StringUtils.PadEnd(
					len,
					record.lines[lineIndex],
					' '
				)
			)
			.append(postStr)
			.toString();
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
		if (Utils.isEmpty(tree))
			return "";
		final String result =
			StringUtils.MergeStrings(midStr, tree);
		if (result.length() == 0)
			return "";
		return (new StringBuilder())
			.append(preStr)
			.append(result)
			.append(postStr)
			.toString();
	}



	// message
	protected String genMessage(final xLogRecord record, final int lineIndex) {
		return (new StringBuilder())
			.append(' ')
			.append( record.lines[ lineIndex ] )
			.toString();
	}



}
