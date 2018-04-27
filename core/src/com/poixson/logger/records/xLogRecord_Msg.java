package com.poixson.logger.records;

import com.poixson.logger.xLevel;
import com.poixson.logger.xLog;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class xLogRecord_Msg implements xLogRecord {

	public final xLog     log;
	public final xLevel   level;
	public final long     timestamp;
	public final String[] lines;



	// new record instance
	public xLogRecord_Msg(final xLog log, final xLevel level,
			final String[] lines, Object[] args) {
		this(
			log,
			level,
			-1L, // timestamp
			lines,
			args
		);
	}
	public xLogRecord_Msg(final xLog log, final xLevel level, final long timestamp,
			final String[] lines, Object[] args) {
		this.log       = log;
		this.level     = level;
		this.timestamp = ( timestamp < 0L ? Utils.getSystemMillis() : timestamp );
		this.lines     = PrepareLines(lines, args);
	}
	protected static String[] PrepareLines(final String[] lines, final Object[] args) {
		final String[] linesSplit = StringUtils.SplitLines(lines);
		if (linesSplit == null) {
			// empty message
			if (Utils.isEmpty(args))
				return null;
			// args only message
			return
				new String[] {
					StringUtils.MergeObjects(", ", args)
				};
		}
		// lines only message
		if (Utils.isEmpty(args)) {
			return linesSplit;
		}
		// insert args into lines
		return
			StringUtils.ReplaceTags(
				linesSplit,
				args
			);
	}



	// message lines
	@Override
	public String[] getLines() {
		return this.lines;
	}



	@Override
	public boolean isEmpty() {
		return Utils.isEmpty(this.lines);
	}
	@Override
	public boolean notEmpty() {
		return Utils.notEmpty(this.lines);
	}



	// level
	@Override
	public xLevel getLevel() {
		return this.level;
	}
	public String getLevelStr() {
		return (
			this.level == null
			? "<null>"
			: this.level.toString()
		);
	}
	// java util level type
	public java.util.logging.Level getJavaLevel() {
		if (this.level == null)
			return null;
		return this.level.getJavaLevel();
	}



	// [logger] [crumbs]
	public String[] getNameTree() {
		if (this.log == null)
			return null;
		return this.log
				.getNameTree();
	}



}
