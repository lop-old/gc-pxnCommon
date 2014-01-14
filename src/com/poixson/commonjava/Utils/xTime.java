package com.poixson.commonjava.Utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;


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
		put('y', YEAR);
		// months
		put('o', MONTH);
		// weeks
		put('w', WEEK);
		// days
		put('d', DAY);
		// hours
		put('h', HOUR);
		// minutes
		put('m', xTime.MIN);
		// seconds
		put('s', SEC);
		// ticks in ms
		put('t', TICK);
		// ms
		put('n', MS);
	}};


	// get object
	public static xTime get() {
		return new xTime(0);
	}
	public static xTime get(final Long ms, final TimeUnit unit) {
		if(ms == null) return null;
		return get().set(ms.longValue(), unit);
	}
	public static xTime get(final String value) {
		if(utilsString.isEmpty(value)) return null;
		return get().set(value);
	}
	public static xTime get(final xTime time) {
		if(time == null) return null;
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


	// get value
	public long get(final TimeUnit unit) {
		if(unit == null) throw new NullPointerException("unit cannot be null");
		return unit.convert(this.value, xTimeU.MS);
	}
	public String getString() {
		return toString(this);
	}
	public long getMS() {
		return value;
	}
	public int getTicks() {
		return (int) (value / TICK);
	}
	// set value
	public xTime set(final long value, final TimeUnit unit) {
		if(unit == null) throw new NullPointerException("unit cannot be null");
		if(isFinal) return null;
		this.value = xTimeU.MS.convert(value, unit);
		return this;
	}
	public xTime set(final String value) {
		if(isFinal) return null;
		if(utilsString.isNotEmpty(value))
			this.value = parseLong(value);
		return this;
	}
	public xTime set(final xTime time) {
		if(isFinal) return null;
		if(time != null)
			this.value = time.getMS();
		return this;
	}


	// parse time from string
	public static xTime parse(final String value) {
		if(utilsString.isEmpty(value)) return null;
		final Long lng = parseLong(value);
		if(lng == null)
			return null;
		return xTime.get(lng, xTimeU.MS);
	}
	public static Long parseLong(final String value) {
		if(utilsString.isEmpty(value)) return null;
		long time = 0;
		StringBuilder tmp = new StringBuilder();
		for(char chr : value.toCharArray()) {
			if(chr == ' ') continue;
			if(Character.isDigit(chr) || chr == '.' || chr == ',') {
				tmp.append(chr);
				continue;
			}
			if(Character.isLetter(chr)) {
				chr = Character.toLowerCase(chr);
				if(timeValues.containsKey(chr)) {
					final double u = (double) timeValues.get(chr);
					time += (utilsMath.toDouble(tmp.toString()) * u);
				}
				tmp = new StringBuilder();
				continue;
			}
		}
		return time;
	}


	// to string
	@Override
	public String toString() {
		return toString(this);
	}
	public static String toString(final xTime time) {
		if(time == null) return null;
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
		if(ms < 1) return null;
		long tmp = ms;
		final StringBuilder out = new StringBuilder();
		for(final Entry<Character, Long> entry : timeValues.entrySet()) {
			final char c = entry.getKey();
			final long u = entry.getValue();
			// 0 for this unit
			if(tmp < u) continue;
			final long val = (long) Math.floor(
				((double) tmp) / ((double) u)
			);
			// append to string
			if(out.length() > 0)
				out.append(' ');
			out.append(Long.toString(val));
			if(!fullFormat) {
				// minimal format
				out.append(c);
			} else {
				// full format
				switch(c) {
				case 'y':
					out.append(" year");
					break;
				case 'o':
					out.append(" month");
					break;
				case 'w':
					out.append(" week");
					break;
				case 'd':
					out.append(" day");
					break;
				case 'h':
					out.append(" hour");
					break;
				case 'm':
					out.append(" month");
					break;
				case 's':
					out.append(" second");
					break;
				case 'n':
					out.append(" ms");
					break;
				default:
					continue;
				}
				if(c != 'n' && val > 1)
					out.append('s');
			}
			tmp = tmp % u;
		}
		return out.toString();
	}


}
