package com.poixson.utils.xLogger;

import java.util.List;


public interface AttachedLogger {



	public xLog log();



	// publish
	default void publish(final xLogRecord record) {
		this.log()
			.publish(record);
	}
	default void publish(final String msg) {
		this.log()
			.publish(msg);
	}
	default void publish(final String msg, final Object... args) {
		this.log()
			.publish(msg, args);
	}
	default void publish(final xLevel level, final String msg) {
		this.log()
			.publish(level, msg);
	}
	default void publish(final xLevel level, final String msg, final Object... args) {
		this.log()
			.publish(level, msg, args);
	}
	default void publish() {
		this.log()
			.publish();
	}



	// title
	default void title(final String msg) {
		this.log()
			.title(msg);
	}
	default void title(final String[] msgs) {
		this.log()
			.title(msgs);
	}
	default void title(final List<String> list) {
		this.log()
			.title(list);
	}
	default void title(final String msg, final Object... args) {
		this.log()
			.title(msg, args);
	}



	// trace
	default void trace(final Throwable e) {
		this.log()
			.trace(e);
	}
	default void trace(final Throwable e, final String msg) {
		this.log()
			.trace(e, msg);
	}
	default void trace(final Throwable e, final String msg, final Object... args) {
		this.log()
			.trace(e, msg, args);
	}



	// detail
	default void detail(final String msg) {
		this.log()
			.detail(msg);
	}
	default void detail(final String msg, final Object... args) {
		this.log()
			.detail(msg, args);
	}



	// finest
	default void finest(final String msg) {
		this.log()
			.finest(msg);
	}
	default void finest(final String msg, final Object... args) {
		this.log()
			.finest(msg, args);
	}



	// finer
	default void finer(final String msg) {
		this.log()
			.finer(msg);
	}
	default void finer(final String msg, final Object... args) {
		this.log()
			.finer(msg, args);
	}



	// fine
	default void fine(final String msg) {
		this.log()
			.fine(msg);
	}
	default void fine(final String msg, final Object... args) {
		this.log()
			.fine(msg, args);
	}



	// stats
	default void stats(final String msg) {
		this.log()
			.stats(msg);
	}
	default void stats(final String msg, final Object... args) {
		this.log()
			.stats(msg, args);
	}



	// info
	default void info(final String msg) {
		this.log()
			.info(msg);
	}
	default void info(final String msg, final Object... args) {
		this.log()
			.info(msg, args);
	}



	// warning
	default void warning(final String msg) {
		this.log()
			.warning(msg);
	}
	default void warning(final String msg, final Object... args) {
		this.log()
			.warning(msg, args);
	}



	// severe
	default void severe(final String msg) {
		this.log()
			.severe(msg);
	}
	default void severe(final String msg, final Object... args) {
		this.log()
			.severe(msg, args);
	}



	// fatal
	default void fatal(final String msg) {
		this.log()
			.fatal(msg);
	}
	default void fatal(final String msg, final Object... args) {
		this.log()
			.fatal(msg, args);
	}



}
