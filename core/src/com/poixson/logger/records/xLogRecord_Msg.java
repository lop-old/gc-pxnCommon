package com.poixson.logger;

import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class xLogRecord {

	public final xLog     log;
	public final xLevel   level;
	public final long     timestamp;
	public final String[] lines;
	public final int      lineCount;



	// new record instance
	public xLogRecord(final xLog log, final xLevel level,
			final String[] lines, Object[] args) {
		this(
			log,
			level,
			-1L, // timestamp
			lines,
			args
		);
	}
	public xLogRecord(final xLog log, final xLevel level, final long timestamp,
			final String[] lines, Object[] args) {
		this.log       = log;
		this.level     = level;
		this.timestamp = ( timestamp < 0L ? Utils.getSystemMillis() : timestamp );
		this.lines     = PrepareLines(lines, args);
		this.lineCount = ( this.lines == null ? 0 : this.lines.length );
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
	public String[] getLines() {
		return this.lines;
	}
	public String getLine(final int index) {
		if (index >= this.lineCount) throw new IndexOutOfBoundsException();
		return this.lines[index];
	}



	public boolean isEmpty() {
		return this.lineCount == 0;
	}
	public boolean notEmpty() {
		return this.lineCount > 0;
	}



	public int getLongestLine() {
		return StringUtils.FindLongestLine(this.lines);
	}



	// level
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
