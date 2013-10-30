package com.poixson.commonjava.Utils;

import java.util.HashMap;
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
	protected static final HashMap<Character, Long> timeValues = new HashMap<Character, Long>() {
		private static final long serialVersionUID = 1L;
	{
		// ms
		put('n', MS);
		// ticks in ms
		put('t', TICK);
		// seconds
		put('s', SEC);
		// minutes
		put('m', MIN);
		// hours
		put('h', HOUR);
		// days
		put('d', DAY);
		// weeks
		put('w', WEEK);
		// months
		put('m', MONTH);
		// years
		put('y', YEAR);
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
		if(!isFinal) {
			if(unit == null) unit = (TimeUnit) xTimeU.MS;
			this.value = xTimeU.MS.convert(value, unit);
		}
		return this;
	}
	public xTime set(String string) {
		if(string != null && !string.isEmpty())
			this.value = parseLong(string);
		return this;
	}
	public xTime set(xTime time) {
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
					long mult = timeValues.get(c);
					time += (utilsMath.ParseNumber(tmp) * mult);
				}
				tmp = new StringBuilder();
			}
		}
		return time;
	}


	public static String toString(xTime time) {
		return toString(time.getMS());
	}
	public static String toString(long ms) {
		for(Entry<Character, Long> entry : timeValues.entrySet()) {
			System.out.println(entry.getKey());
//			long timeMod = entry.getValue();
		}
		return "<TIME>";
	}
//	private String buildString(long value, long unitValue, String unit) {
//		value = (long) Math.floor(
//			((double)value) / ((double)unitValue)
//		);
//		if(value <= 0)
//			return null;
//		return Long.toString(value)+unit;
//	}


	// final value
	public xTime setFinal() {
		this.isFinal = true;
		return this;
	}
	public boolean isFinal() {
		return this.isFinal;
	}


}
