package com.poixson.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.exceptions.UnmodifiableObjectException;


public class xTime {

	// stored value in ms
	public volatile long value = 0;
	// write lock
	private volatile boolean isFinal = false;

	// static values
	public static final long MS    = 1L;
	public static final long SEC   = MS * 1000L;
	public static final long TICK  = SEC / 20L;
	public static final long MIN   = SEC * 60L;
	public static final long HOUR  = MIN * 60L;
	public static final long DAY   = HOUR * 24L;
	public static final long WEEK  = DAY * 7L;
	public static final long MONTH = DAY * 30L;
	public static final long YEAR  = DAY * 365L;
	protected static final HashMap<Character, Long> timeValues = new LinkedHashMap<Character, Long>() {
		private static final long serialVersionUID = 1L;
	{
		// years
		put( new Character('y'), new Long(YEAR)  );
		// months
		put( new Character('o'), new Long(MONTH) );
		// weeks
		put( new Character('w'), new Long(WEEK)  );
		// days
		put( new Character('d'), new Long(DAY)   );
		// hours
		put( new Character('h'), new Long(HOUR)  );
		// minutes
		put( new Character('m'), new Long(MIN)   );
		// seconds
		put( new Character('s'), new Long(SEC)   );
		// ms
		put( new Character('n'), new Long(MS)    );
		// ticks in ms
		put( new Character('t'), new Long(TICK)  );
	}};



	// get object
	public static xTime get() {
		return new xTime(0);
	}
	public static xTime get(final long ms) {
		return get(ms, xTimeU.MS);
	}
	public static xTime get(final long value, final TimeUnit unit) {
		if (value < 0)
			return null;
		return get().set(value, unit);
	}
	public static xTime get(final String value) {
		if (Utils.isEmpty(value))
			return null;
		return get().set(value);
	}
	public static xTime get(final xTime time) {
		if (time == null)
			return null;
		return get().set(time);
	}
	// new object
	protected xTime(final long ms) {
		this.value = ms;
	}
	// clone object
	@Override
	public xTime clone() {
		return get(this);
	}



	// final value
	public xTime setFinal() {
		this.isFinal = true;
		return this;
	}
	public boolean isFinal() {
		return this.isFinal;
	}



	// reset value to 0
	public void reset() {
		if (this.isFinal)
			throw UnmodifiableObjectException.get();
		this.value = 0;
	}



	// get value
	public long get(final TimeUnit unit) {
		if (unit == null)
			throw new RequiredArgumentException("unit");
		return unit.convert(this.value, xTimeU.MS);
	}
	public String getString() {
		return toString(this);
	}
	public long getMS() {
		return this.value;
	}
	public int getTicks() {
		return (int) (this.value / TICK);
	}
	// set value
	public xTime set(final long value, final TimeUnit unit) {
		if (unit == null) throw new RequiredArgumentException("unit");
		if (this.isFinal) throw UnmodifiableObjectException.get();
		this.value = xTimeU.MS.convert(value, unit);
		return this;
	}
	public xTime set(final String val) {
		if (this.isFinal) throw UnmodifiableObjectException.get();
		if (Utils.notEmpty(val)) {
			this.value = parseLong(val).longValue();
		}
		return this;
	}
	public xTime set(final xTime time) {
		if (this.isFinal) throw UnmodifiableObjectException.get();
		if (time != null) {
			this.value = time.getMS();
		}
		return this;
	}



	// add time
	public void add(final long val, final TimeUnit unit) {
		if (unit == null) throw new RequiredArgumentException("unit");
		if (this.isFinal) throw UnmodifiableObjectException.get();
		this.value += xTimeU.MS.convert(val, unit);
	}
	public void add(final String val) {
		if (this.isFinal) throw UnmodifiableObjectException.get();
		if (Utils.notEmpty(val)) {
			this.value += parseLong(val).longValue();
		}
	}
	public void add(final xTime time) {
		if (time == null) throw new RequiredArgumentException("time");
		if (this.isFinal) throw UnmodifiableObjectException.get();
		this.value += time.value;
	}



	// parse time from string
	public static xTime parse(final String value) {
		if (Utils.isEmpty(value)) return null;
		final Long lng = parseLong(value);
		return (
			lng == null
			? null
			: xTime.get(lng.longValue(), xTimeU.MS)
		);
	}
	public static Long parseLong(final String value) {
		if (Utils.isEmpty(value)) return null;
		long time = 0;
		StringBuilder buf = new StringBuilder();
		for (char c : value.toCharArray()) {
			if (c == ' ') continue;
			if (Character.isDigit(c) || c == '.' || c == ',') {
				buf.append(c);
				continue;
			}
			if (Character.isLetter(c)) {
				final Character chr = new Character(
					Character.toLowerCase(c)
				);
				if (timeValues.containsKey(chr)) {
					final double u = timeValues.get(chr).doubleValue();
					time += (NumberUtils.toDouble(buf.toString()).doubleValue() * u);
				}
				buf = new StringBuilder();
				continue;
			}
		}
		return new Long(time);
	}



	// to string
	@Override
	public String toString() {
		return toString(this);
	}
	public static String toString(final xTime time) {
		if (time == null) return null;
		return toString(time.getMS());
	}
	public static String toString(final long ms) {
		return buildString(ms, false);
	}
	// full format
	public String toFullString() {
		return toString(this, true);
	}
	public static String toString(final xTime time, final boolean fullFormat) {
		return buildString(time.getMS(), fullFormat);
	}
	public static String buildString(final long ms, final boolean fullFormat) {
		if (ms < 1) return null;
		long tmp = ms;
		final StringBuilder buf = new StringBuilder();
		for (final Entry<Character, Long> entry : timeValues.entrySet()) {
			final char c = entry.getKey().charValue();
			final long u = entry.getValue().longValue();
			if (tmp < u) continue;
			final long val = (long) Math.floor(
				((double) tmp) / ((double) u)
			);
			// append to string
			if (buf.length() > 0) {
				buf.append(' ');
			}
			buf.append(Long.toString(val));
			if (!fullFormat) {
				// minimal format
				buf.append(c);
			} else {
				// full format
				switch (c) {
				case 'y':
					buf.append(" year");
					break;
				case 'o':
					buf.append(" month");
					break;
				case 'w':
					buf.append(" week");
					break;
				case 'd':
					buf.append(" day");
					break;
				case 'h':
					buf.append(" hour");
					break;
				case 'm':
					buf.append(" minute");
					break;
				case 's':
					buf.append(" second");
					break;
				case 'n':
					buf.append(" ms");
					break;
				default:
					continue;
				}
				if (c != 'n' && val > 1) {
					buf.append('s');
				}
			}
			tmp = tmp % u;
		}
		return buf.toString();
	}



}
