package com.poixson.logger.formatters;

import com.poixson.logger.xLevel;
import com.poixson.logger.xLogRecord;
import com.poixson.utils.StringUtils;


public class xLogFormatter_Color extends xLogFormatter {



	public xLogFormatter_Color() {
		super();
	}



	@Override
	public String[] formatMessage(final xLogRecord record) {
		// publish plain message
		if (record.level == null) {
			return record.getPreparedLines();
		}
		// [[ title ]]
		if (xLevel.TITLE.equals(record.level)) {
			return
				this.genTitle(
					record,
					" @|FG_MAGENTA [[|@ @|FG_CYAN ",
					"|@ @|FG_MAGENTA ]]|@ "
				);
		}
		// format message lines
		final String[] result = new String[ record.lineCount ];
		for (int index=0; index<record.lineCount; index++) {
			// timestamp [level] [crumbs] message
			result[index] =
				StringUtils.MergeStrings(
					' ',
					// timestamp
					this.genTimestamp(
						record,
						"D yyyy-MM-dd HH:mm:ss",
						"@|FG_WHITE ",
						"|@"
					),
					// [level]
					this.genLevelColored(record),
					// [crumbs]
					this.genCrumbsColored(record),
					// message
					this.genMessage(record, index)
				);
		}
		return result;
	}



	// ------------------------------------------------------------------------------- //
	// generate parts



	// [level]
	protected String genLevelColored(final xLogRecord record) {
		return (new StringBuilder())
			.append("@|FG_BLACK,BOLD [|@@|")
			.append( this.getLevelColor(record.level) )
			.append(' ')
			.append( StringUtils.PadCenter(7, record.getLevelStr(), ' ') )
			.append("|@@|FG_BLACK,BOLD ]|@")
			.toString();
	}
	protected String getLevelColor(final xLevel level) {
		if (level == null)
			return "FG_BLACK,BOLD";
		// all, finest, finer, fine
		if (level.isLoggable(xLevel.FINE))
			return "FG_BLACK,BOLD";
		// info
		if (level.isLoggable(xLevel.INFO))
			return "FG_CYAN";
		// warning
		if (level.isLoggable(xLevel.WARNING))
			return "FG_RED";
		// severe
		if (level.isLoggable(xLevel.SEVERE))
			return "FG_RED,BOLD";
		// fatal
		if (level.isLoggable(xLevel.FATAL))
			return "FG_RED,BOLD,UNDERLINE";
		// stdout
		if (level.isLoggable(xLevel.STDOUT))
			return "FG_GREEN";
		// stderr
		if (level.isLoggable(xLevel.STDERR))
			return "FG_YELLOW";
		// off
		return "FG_BLACK,BOLD";
	}



	// crumbs
	protected String genCrumbsColored(final xLogRecord record) {
		return (new StringBuilder())
			.append("@|FG_BLACK,BOLD ")
			.append( super.genCrumbs(record, "[", "] [", "]") )
			.append("|@")
			.toString();
	}



}
