package com.poixson.logger.formatters;

import java.text.SimpleDateFormat;

import com.poixson.logger.xLevel;
import com.poixson.logger.records.xLogRecord_Msg;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class xLogFormatter {



	public xLogFormatter() {
	}



	public String[] formatMessage(final xLogRecord_Msg record) {
		// [[ title ]]
		if (xLevel.TITLE.equals(record.level))
			return this.genTitle(record);
		// message only
		return record.getLines();
	}



	// ------------------------------------------------------------------------------- //
	// generate parts



	// title
	protected String[] genTitle(final xLogRecord_Msg record) {
		return
			this.genTitle(
				record,
				" [[ ",
				" ]] "
			);
	}
	protected String[] genTitle(final xLogRecord_Msg record,
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
		final String[] lines = record.getLines();
		final String[] result = new String[ lines.length ];
		final int len = StringUtils.FindLongestLine(lines);
		for (int index = 0; index < lines.length; index++) {
			final String line =
				StringUtils.PadEnd(
					len,
					lines[index],
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
	protected String genTimestamp(final xLogRecord_Msg record, final String format,
			final String preStr, final String postStr) {
		return (new StringBuilder())
			.append(preStr)
			.append( this.genTimestamp(record, format) )
			.append(postStr)
			.toString();
	}
	protected String genTimestamp(final xLogRecord_Msg record, final String format) {
		final SimpleDateFormat dateFormat =
			new SimpleDateFormat(format);
		return
			dateFormat.format(
				Long.valueOf(record.timestamp)
			);
	}



	// level
	protected String genLevel(final xLogRecord_Msg record,
			final String preStr, final String postStr) {
		return (new StringBuilder())
			.append(preStr)
			.append( this.genLevel(record) )
			.append(postStr)
			.toString();
	}
	protected String genLevel(final xLogRecord_Msg record) {
		return StringUtils.PadCenter(7, record.getLevelStr(), ' ');
	}



	// crumbs
	protected String genCrumbs(final xLogRecord_Msg record,
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



}
