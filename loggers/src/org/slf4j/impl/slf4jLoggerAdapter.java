package org.slf4j.impl;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

import com.poixson.utils.xLogger.xLevel;
import com.poixson.utils.xLogger.xLog;


public class slf4jLoggerAdapter extends MarkerIgnoringBase {
	private static final long serialVersionUID = 1L;

	private final xLog log;



	public slf4jLoggerAdapter(final String name, final xLog log) {
		this.name = name;
		this.log  = log;
	}



	public xLog getXLog() {
		return this.log;
	}



	// trace
	@Override
	public boolean isTraceEnabled() {
		return
			this.getXLog()
				.isLoggable(xLevel.FINEST);
	}
	@Override
	public void trace(final String msg) {
		this.getXLog()
			.finest(msg);
	}
	@Override
	public void trace(final String format, final Object arg) {
		if (this.isTraceEnabled()) {
			final FormattingTuple ft =
				MessageFormatter.format(format, arg);
			this.trace(ft.getMessage(), ft.getThrowable());
		}
	}
	@Override
	public void trace(final String format, final Object arg1, final Object arg2) {
		if (this.isTraceEnabled()) {
			final FormattingTuple ft =
				MessageFormatter.format(format, arg1, arg2);
			this.trace(ft.getMessage(), ft.getThrowable());
		}
	}
	@Override
	public void trace(final String format, final Object... args) {
		if (this.isTraceEnabled()) {
			final FormattingTuple ft =
				MessageFormatter.format(format, args);
			this.trace(ft.getMessage(), ft.getThrowable());
		}
	}
	@Override
	public void trace(final String msg, final Throwable e) {
		final xLog log = this.getXLog();
		log.warning(msg);
		log.trace(e);
	}



	// debug
	@Override
	public boolean isDebugEnabled() {
		return
			this.getXLog()
				.isLoggable(xLevel.FINE);
	}
	@Override
	public void debug(final String msg) {
		this.getXLog()
			.fine(msg);
	}
	@Override
	public void debug(final String format, final Object arg) {
		if (this.isDebugEnabled()) {
			final FormattingTuple ft =
				MessageFormatter.format(format, arg);
			this.debug(ft.getMessage(), ft.getThrowable());
		}
	}
	@Override
	public void debug(final String format, final Object arg1, final Object arg2) {
		if (this.isDebugEnabled()) {
			final FormattingTuple ft =
				MessageFormatter.format(format, arg1, arg2);
			this.debug(ft.getMessage(), ft.getThrowable());
		}
	}
	@Override
	public void debug(final String format, final Object... args) {
		if (this.isDebugEnabled()) {
			final FormattingTuple ft =
				MessageFormatter.format(format, args);
			this.debug(ft.getMessage(), ft.getThrowable());
		}
	}
	@Override
	public void debug(final String msg, final Throwable e) {
		final xLog log = this.getXLog();
		log.warning(msg);
		log.trace(e);
	}



	// info
	@Override
	public boolean isInfoEnabled() {
		return
			this.getXLog()
				.isLoggable(xLevel.INFO);
	}
	@Override
	public void info(final String msg) {
		this.getXLog()
			.info(msg);
	}
	@Override
	public void info(final String format, final Object arg) {
		if (this.isInfoEnabled()) {
			final FormattingTuple ft =
				MessageFormatter.format(format, arg);
			this.info(ft.getMessage(), ft.getThrowable());
		}
	}
	@Override
	public void info(final String format, final Object arg1, final Object arg2) {
		if (this.isInfoEnabled()) {
			final FormattingTuple ft =
				MessageFormatter.format(format, arg1, arg2);
			this.info(ft.getMessage(), ft.getThrowable());
		}
	}
	@Override
	public void info(final String format, final Object... args) {
		if (this.isInfoEnabled()) {
			final FormattingTuple ft =
				MessageFormatter.format(format, args);
			this.info(ft.getMessage(), ft.getThrowable());
		}
	}
	@Override
	public void info(final String msg, final Throwable e) {
		final xLog log = this.getXLog();
		log.warning(msg);
		log.trace(e);
	}



	// warning
	@Override
	public boolean isWarnEnabled() {
		return
			this.getXLog()
				.isLoggable(xLevel.WARNING);
	}
	@Override
	public void warn(final String msg) {
		this.getXLog()
			.warning(msg);
	}
	@Override
	public void warn(final String format, final Object arg) {
		if (this.isWarnEnabled()) {
			final FormattingTuple ft =
				MessageFormatter.format(format, arg);
			this.warn(ft.getMessage(), ft.getThrowable());
		}
	}
	@Override
	public void warn(final String format, final Object arg1, final Object arg2) {
		if (this.isWarnEnabled()) {
			final FormattingTuple ft =
				MessageFormatter.format(format, arg1, arg2);
			this.warn(ft.getMessage(), ft.getThrowable());
		}
	}
	@Override
	public void warn(final String format, final Object... args) {
		if (this.isWarnEnabled()) {
			final FormattingTuple ft =
				MessageFormatter.format(format, args);
			this.warn(ft.getMessage(), ft.getThrowable());
		}
	}
	@Override
	public void warn(final String msg, final Throwable e) {
		final xLog log = this.getXLog();
		log.warning(msg);
		log.trace(e);
	}



	// error
	@Override
	public boolean isErrorEnabled() {
		return
			this.getXLog()
				.isLoggable(xLevel.SEVERE);
	}
	@Override
	public void error(final String msg) {
		this.getXLog()
			.severe(msg);
	}
	@Override
	public void error(final String format, final Object arg) {
		if (this.isErrorEnabled()) {
			final FormattingTuple ft =
				MessageFormatter.format(format, arg);
			this.error(ft.getMessage(), ft.getThrowable());
		}
	}
	@Override
	public void error(final String format, final Object arg1, final Object arg2) {
		if (this.isErrorEnabled()) {
			final FormattingTuple ft =
				MessageFormatter.format(format, arg1, arg2);
			this.error(ft.getMessage(), ft.getThrowable());
		}
	}
	@Override
	public void error(final String format, final Object... args) {
		if (this.isErrorEnabled()) {
			final FormattingTuple ft =
				MessageFormatter.format(format, args);
			this.error(ft.getMessage(), ft.getThrowable());
		}
	}
	@Override
	public void error(final String msg, final Throwable e) {
		final xLog log = this.getXLog();
		log.severe(msg);
		log.trace(e);
	}



}
