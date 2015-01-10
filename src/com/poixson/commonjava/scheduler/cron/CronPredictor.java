package com.poixson.commonjava.scheduler.cron;
/*
 * cron4j - A pure Java cron-like scheduler
 *
 * Copyright (C) 2007-2010 Carlo Pelliccia (www.sauronsoftware.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version
 * 2.1, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License 2.1 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License version 2.1 along with this program.
 * If not, see <http://www.gnu.org/licenses/>
 */

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.poixson.commonjava.Utils.utilsNumbers;
import com.poixson.commonjava.Utils.xClock;
import com.poixson.commonjava.scheduler.cron.matchers.MonthDayMatcher;


/**
 * A predictor is able to predict when a scheduling pattern will be matched.
 * Suppose you want to know when the scheduler will execute a task scheduled
 * with the pattern <em>0 3 * jan-jun,sep-dec mon-fri</em>. You can predict the
 * next <em>n</em> execution of the task using a Predictor instance:
 *
 * <pre>
 * String pattern = &quot;0 3 * jan-jun,sep-dec mon-fri&quot;;
 * CronPredictor p = new CronPredictor(pattern);
 * for (int i = 0; i &lt; n; i++) {
 *   System.out.println(p.nextMatchingDate());
 * }
 * </pre>
 *
 * @author Carlo Pelliccia
 * @since 1.1
 */
public class CronPredictor {

	private final CronPattern pattern;
	private volatile long next = -1L;
	private volatile TimeZone timezone = TimeZone.getDefault();



	public CronPredictor(final String pattern) {
		this(
			pattern,
			-1L
		);
	}
	public CronPredictor(final String pattern, final long start) {
		this(
			new CronPattern(pattern),
			start
		);
	}
	public CronPredictor(final CronPattern pattern) {
		this(
			pattern,
			-1L
		);
	}
	public CronPredictor(final CronPattern pattern, final long start) {
		this.pattern = pattern;
		// round to the next minute
		if(start == -1)
			this.next = -1;
		else
			this.next = utilsNumbers.ceil(
				start + 1000L,
				60000
			);
	}



	public synchronized long untilNextMatching(final long now) {
		if(now < 1L)
			return this.untilNextMatching(getCurrentMillis());
		if(this.next == -1L) {
			this.next = utilsNumbers.ceil(
				now + 1000L,
				60000
			);
		} else {
			if(this.next > now) {
				final long until = this.next - now;
				// negative time
				if(until <= 0L) {
					this.next = -1L;
					return 0L;
				}
				// use cached
				return until;
			}
			if(this.next <= now) {
				// matches?
				if(this.pattern.match(now)) {
					this.next = -1L;
					return 0L;
				}
			}
		}
		// values to increment
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(
			utilsNumbers.floor(
				this.next,
				60000
			)
		);
		cal.setTimeZone(this.timezone);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND,      0);
		int minute   = cal.get(Calendar.MINUTE);
		int hour     = cal.get(Calendar.HOUR_OF_DAY);
		int monthday = cal.get(Calendar.DAY_OF_MONTH);
		int month    = cal.get(Calendar.MONTH);
		int year     = cal.get(Calendar.YEAR);
		while(true) { // day of week
			while(true) { // month
				while(true) { // day of month
					while(true) { // hour
						while(true) { // minutes
							if(this.pattern.matcher_minute.match(minute))
								break;
							minute++;
							if(minute > 59) {
								minute = 0;
								hour++;
							}
						}
						// hour
						if(hour > 23) {
							hour = 0;
							monthday++;
						}
						if(this.pattern.matcher_hour.match(hour))
							break;
						hour++;
						minute = 0;
					}
					// day of month
					if(monthday > 31) {
						monthday = 1;
						month++;
					}
					if(month > Calendar.DECEMBER) {
						month = Calendar.JANUARY;
						year++;
					}
					if(this.pattern.matcher_monthday instanceof MonthDayMatcher) {
						final MonthDayMatcher aux = (MonthDayMatcher) this.pattern.matcher_monthday;
						if(aux.match(monthday, month + 1, cal.isLeapYear(year)))
							break;
						monthday++;
						hour   = 0;
						minute = 0;
					} else {
						if(this.pattern.matcher_monthday.match(monthday))
							break;
						monthday++;
						hour   = 0;
						minute = 0;
					}
				}
				// month
				if(this.pattern.matcher_month.match(month + 1))
					break;
				month++;
				monthday = 1;
				hour   = 0;
				minute = 0;
			}
			cal = new GregorianCalendar();
			cal.setTimeZone(this.timezone);
			cal.set(Calendar.MILLISECOND,  0);
			cal.set(Calendar.SECOND,       0);
			cal.set(Calendar.MINUTE,       minute);
			cal.set(Calendar.HOUR_OF_DAY,  hour);
			cal.set(Calendar.DAY_OF_MONTH, monthday);
			cal.set(Calendar.MONTH,        month);
			cal.set(Calendar.YEAR,         year);
			// day/month/year compatibility check
			{
				// fixed invalid date - take another spin!
				if(
					cal.get(Calendar.DAY_OF_MONTH) != monthday ||
					cal.get(Calendar.MONTH)        != month    ||
					cal.get(Calendar.YEAR)         != year
					) {
						monthday = cal.get(Calendar.DAY_OF_MONTH);
						month    = cal.get(Calendar.MONTH);
						year     = cal.get(Calendar.YEAR);
						continue;
				}
			}
			// day of week
			{
				final int weekday = cal.get(Calendar.DAY_OF_WEEK) - 1;
				if(this.pattern.matcher_weekday.match(weekday))
					break;
			}
			monthday++;
			hour   = 0;
			minute = 0;
			if(monthday > 31) {
				monthday = 1;
				month++;
				if(month > Calendar.DECEMBER) {
					month = Calendar.JANUARY;
					year++;
				}
			}
		}
		// round to minute
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND,      0);
		this.next = utilsNumbers.floor(
			cal.getTimeInMillis(),
			60000
		);
		// time until next
		{
			final long until = this.next - now;
			// negative time
			if(until <= 0L) {
				this.next = -1L;
				return 0;
			}
			return until;
		}
	}



	/**
	 * Sets the time zone for predictions.
	 * @param timeZone The time zone for predictions.
	 */
	public void setTimeZone(final TimeZone timezone) {
		this.timezone = timezone;
	}



	public static long getCurrentMillis() {
		return xClock.get().millis();
	}



}
