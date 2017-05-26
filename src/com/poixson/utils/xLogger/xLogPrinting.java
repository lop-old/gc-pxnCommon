package com.poixson.utils.xLogger;

import java.util.List;

import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public abstract class xLogPrinting {


	public abstract xLog get(final String name);
	public abstract xLog getWeak();
	public abstract xLog getWeak(final String name);
	@Override
	public abstract xLog clone();

	public abstract boolean isRoot();

	public abstract void setLevel(final xLevel lvl);
	public abstract xLevel getLevel();
	public abstract boolean isLoggable(final xLevel lvl);

	public abstract List<String> getNameTree();

	public abstract void addHandler(final xLogHandler handler);
	public abstract void setHandler(final xLogHandler handler);



	public static String ReplaceArgs(final String msg, final Object[] args) {
		if (msg == null)
			return null;
		if (args.length == 0)
			return msg;
		if (args.length == 1) {
			return msg.replaceFirst("\\{\\}", args[0].toString());
		}
		String str = msg;
		for (final Object arg : args) {
			str = str.replaceFirst("\\{\\}", arg.toString());
		}
		return str;
	}



	// publish message
	public abstract void publish(final xLogRecord record);
	public abstract void publish(final String msg);
	public void publish(final String msg, final Object... args) {
		this.publish(
			ReplaceArgs(msg, args)
		);
	}



	// publish message with level
	public void publish(final xLevel level, final String msg) {
		if (level == null) {
			this.publish(msg);
			return;
		}
		this.publish(
			new xLogRecord(
				(xLog) this,
				level,
				msg
			)
		);
	}
	public void publish(final xLevel level, final String msg, final Object... args) {
		this.publish(
			level,
			ReplaceArgs(msg, args)
		);
	}



	public void publish() {
		publish("");
	}



	// title
	public void title(final String msg) {
		if (msg == null) {
			this.publish(" @|FG_MAGENTA [[|@ @|FG_CYAN <null>|@ @|FG_MAGENTA ]]|@");
		} else
		if (msg.contains("\n")) {
			msg.replace("\r", "");
			this.title(
				msg.split("\n")
			);
		} else {
			this.publish(" @|FG_MAGENTA [[|@ @|FG_CYAN "+msg+"|@ @|FG_MAGENTA ]]|@");
		}
	}
	// multi-lined title
	public void title(final String[] msgs) {
		// find max length
		int len = 0;
		for (final String line : msgs) {
			if (line.length() > len) {
				len = line.length();
			}
		}
		// print lines
		for (final String line : msgs) {
			this.title( line + StringUtils.repeat(len - line.length(), " ") );
		}
	}
	public void title(final List<String> list) {
		if (list == null) return;
		this.title(list.toArray(new String[0]));
	}
	public void title(final String msg, final Object... args) {
		this.title(
			ReplaceArgs(msg, args)
		);
	}



	// exception
	public void trace(final Throwable e) {
		this.trace(e, null);
	}
	public void trace(final Throwable e, final String msg) {
		final StringBuilder str = new StringBuilder();
		if (Utils.notEmpty(msg)) {
			str.append(msg).append(" - ");
		}
		str.append(
			StringUtils.ExceptionToString(e)
		);
		this.publish(
			xLevel.SEVERE,
			str.toString()
		);
	}
	public void trace(final Throwable e, final String msg, final Object... args) {
		this.trace(
			e,
			ReplaceArgs(msg, args)
		);
	}



	// stdout
	public void stdout(final String msg) {
		this.publish(xLevel.STDOUT, msg);
	}
	public void stdout(final String msg, final Object... args) {
		this.stdout(
			ReplaceArgs(msg, args)
		);
	}



	// stderr
	public void stderr(final String msg) {
		this.publish(xLevel.STDERR, msg);
	}
	public void stderr(final String msg, final Object... args) {
		this.stderr(
			ReplaceArgs(msg, args)
		);
	}



	// detail
	public void detail(final String msg) {
		this.publish(xLevel.DETAIL, msg);
	}
	public void detail(final String msg, final Object... args) {
		this.detail(
			ReplaceArgs(msg, args)
		);
	}



	// finest
	public void finest(final String msg) {
		this.publish(xLevel.FINEST, msg);
	}
	public void finest(final String msg, final Object... args) {
		this.finest(
			ReplaceArgs(msg, args)
		);
	}



	// finer
	public void finer(final String msg) {
		this.publish(xLevel.FINER, msg);
	}
	public void finer(final String msg, final Object... args) {
		this.finer(
			ReplaceArgs(msg, args)
		);
	}



	// fine
	public void fine(final String msg) {
		this.publish(xLevel.FINE, msg);
	}
	public void fine(final String msg, final Object... args) {
		this.fine(
			ReplaceArgs(msg, args)
		);
	}



	// stats
	public void stats(final String msg) {
		this.publish(xLevel.STATS, msg);
	}
	public void stats(final String msg, final Object... args) {
		this.stats(
			ReplaceArgs(msg, args)
		);
	}



	// info
	public void info(final String msg) {
		this.publish(xLevel.INFO, msg);
	}
	public void info(final String msg, final Object... args) {
		this.info(
			ReplaceArgs(msg, args)
		);
	}



	// warning
	public void warning(final String msg) {
		this.publish(xLevel.WARNING, msg);
	}
	public void warning(final String msg, final Object... args) {
		this.warning(
			ReplaceArgs(msg, args)
		);
	}



	// severe
	public void severe(final String msg) {
		this.publish(xLevel.SEVERE, msg);
	}
	public void severe(final String msg, final Object... args) {
		this.severe(
			ReplaceArgs(msg, args)
		);
	}



	// fatal
	public void fatal(final String msg) {
		this.publish(xLevel.FATAL, msg);
	}
	public void fatal(final String msg, final Object... args) {
		this.fatal(
			ReplaceArgs(msg, args)
		);
	}



}
