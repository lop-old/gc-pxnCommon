package com.poixson.utils.xScheduler.trigger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.poixson.utils.Utils;
import com.poixson.utils.xTime;
import com.poixson.utils.xTimeU;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLog;
import com.poixson.utils.xScheduler.xSchedulerTrigger;


public class TriggerClock extends xSchedulerTrigger {

	public static final String DEFAULT_DATE_FORMAT = "yy/MM/dd HH:mm:ss";
	private static final long  DEFAULT_GRACE_TIME  = 1000L;

	private volatile Date date = null;
	private final xTime grace = xTime.getNew();

	private final Object updateLock = new Object();



	// builder
	public static TriggerClock builder() {
		return new TriggerClock();
	}



	public TriggerClock() {
	}
	public TriggerClock(final long time) {
		this();
		this.setDate(time);
	}
	public TriggerClock(final String dateStr, final String dateFormatStr) throws ParseException {
		this();
		this.setDate(
			dateStr,
			dateFormatStr
		);
	}
	public TriggerClock(final Date date) {
		this();
		this.setDate(date);
	}



	@Override
	public long untilNextTrigger(final long now) {
		if (this.notEnabled())
			return Long.MIN_VALUE;
		if (this.date == null) throw RequiredArgumentException.getNew("date");
		synchronized(this.updateLock) {
			final Date date = this.date;
			if (date == null) throw RequiredArgumentException.getNew("date");
			final long time = date.getTime();
			final long grace = this.getGraceTime();
			// calculate time until trigger
			final long untilNext = time - now;
			if (0 - untilNext > grace) {
//TODO: what should we do here?
xLog.getRoot().warning("Skipping old scheduled clock trigger..");
				this.setDisabled();
				return Long.MIN_VALUE;
			}
			return untilNext;
		}
	}



	// ------------------------------------------------------------------------------- //
	// trigger config



	// scheduled date
	public TriggerClock setDate(final long time) {
		final Date date = new Date(time);
		return this.setDate(date);
	}
	public TriggerClock setDate(final String dateStr, final String dateFormatStr)
			throws ParseException {
		if (Utils.isBlank(dateStr))       throw RequiredArgumentException.getNew("dateStr");
		if (Utils.isBlank(dateFormatStr)) throw RequiredArgumentException.getNew("dateFormatStr");
		final DateFormat format =
			new SimpleDateFormat(
				(
					Utils.isBlank(dateFormatStr)
					? DEFAULT_DATE_FORMAT
					: dateFormatStr
				),
				Locale.ENGLISH
			);
		final Date date = format.parse(dateStr);
		return this.setDate(date);
	}
	public TriggerClock setDate(final Date date) {
		if (date == null) throw RequiredArgumentException.getNew("date");
		this.date = date;
		return this;
	}



	// grace time
	public long getGraceTime() {
		final long time = this.grace.getMS();
		return (
			time <= 0L
			? DEFAULT_GRACE_TIME
			: time
		);
	}
	public TriggerClock setGraceTime(final long time) {
		this.grace.set(
			time,
			xTimeU.MS
		);
		return this;
	}
	public TriggerClock setGraceTime(final String timeStr) {
		this.grace.set(timeStr);
		return this;
	}
	public TriggerClock setGraceTime(final xTime time) {
		this.grace.set(time);
		return this;
	}



	// ------------------------------------------------------------------------------- //
	// overrides



	public TriggerClock enable() {
		return (
			super.enable() == null
			? null
			: this
		);
	}
	public TriggerClock disable() {
		return (
			super.disable() == null
			? null
			: this
		);
	}
	public TriggerClock enable(final boolean enabled) {
		return (
			super.enable(enabled) == null
			? null
			: this
		);
	}



	public TriggerClock repeat() {
		return (
			super.repeat() == null
			? null
			: this
		);
	}
	public TriggerClock noRepeat() {
		return (
			super.noRepeat() == null
			? null
			: this
		);
	}
	public TriggerClock runOnce() {
		return (
			super.runOnce() == null
			? null
			: this
		);
	}
	public TriggerClock repeat(final boolean repeating) {
		return (
			super.repeat(repeating) == null
			? null
			: this
		);
	}



}
