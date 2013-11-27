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
	public static xTime get(Long ms, TimeUnit unit) {
		if(ms == null) return null;
		return get().set(ms.longValue(), unit);
	}
	public static xTime get(String string) {
		if(string == null) return null;
		return get().set(string);
	}
	public static xTime get(xTime time) {
		if(time == null) return null;
		return get().set(time);
	}
	// new object
	protected xTime(long ms) {
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
	public long get(TimeUnit unit) {
		if(unit == null)
			return 0;
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
	public xTime set(long value, TimeUnit unit) {
		if(isFinal) return null;
		if(unit == null) unit = (TimeUnit) xTimeU.MS;
		this.value = xTimeU.MS.convert(value, unit);
		return this;
	}
	public xTime set(String string) {
		if(isFinal) return null;
		if(string != null && !string.isEmpty())
			this.value = parseLong(string);
		return this;
	}
	public xTime set(xTime time) {
		if(isFinal) return null;
		if(time != null)
			this.value = time.getMS();
		return this;
	}


	// parse time from string
	public static xTime parse(String string) {
		Long l = parseLong(string);
		if(l == null)
			return null;
		return xTime.get(l, xTimeU.MS);
	}
	public static Long parseLong(String string) {
		if(string == null || string.isEmpty())
			return null;
		long time = 0;
		StringBuilder tmp = new StringBuilder();
		for(char c : string.toCharArray()) {
			if(c == ' ') continue;
			if(Character.isDigit(c) || c == '.' || c == ',') {
				tmp.append(c);
				continue;
			}
			if(Character.isLetter(c)) {
				c = Character.toLowerCase(c);
				if(timeValues.containsKey(c)) {
					double u = (double) timeValues.get(c);
					time += (utilsMath.parseDouble(tmp.toString()) * u);
				}
				tmp = new StringBuilder();
			}
		}
		return time;
	}


	// to string
	@Override
	public String toString() {
		return toString(this);
	}
	public static String toString(xTime time) {
		return toString(time.getMS());
	}
	public static String toString(long ms) {
		return buildString(ms, false);
	}
	// full format
	public String toFullString() {
		return toString(this, true);
	}
	public static String toString(xTime time, boolean fullFormat) {
		return buildString(time.getMS(), fullFormat);
	}
	public static String buildString(long ms, boolean fullFormat) {
		if(ms <= 0)
			return null;
		StringBuilder string = new StringBuilder();
		for(Entry<Character, Long> entry : timeValues.entrySet()) {
			char c    = entry.getKey();
			long u = entry.getValue();
			if(ms < u) continue;
			long val = (long) Math.floor(
				((double) ms) / ((double) u)
			);
			// append to string
			if(string.length() > 0)
				string.append(' ');
			string.append(Long.toString(val));
			if(!fullFormat) {
				string.append(c);
			} else {
				switch(c) {
				case 'y':
					string.append(" year");
					break;
				case 'o':
					string.append(" month");
					break;
				case 'w':
					string.append(" week");
					break;
				case 'd':
					string.append(" day");
					break;
				case 'h':
					string.append(" hour");
					break;
				case 'm':
					string.append(" month");
					break;
				case 's':
					string.append(" second");
					break;
				case 'n':
					string.append(" ms");
					break;
				default:
					continue;
				}
				if(c != 'n' && val > 1)
					string.append('s');
			}
			ms = ms % u;
		}
		return string.toString();
	}


}
