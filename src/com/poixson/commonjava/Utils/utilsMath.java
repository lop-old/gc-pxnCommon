package com.poixson.commonjava.Utils;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.regex.Pattern;


public final class utilsMath {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	private utilsMath() {}


	// true;
	public static final String[] truesy = new String[] {
		"1",
		"t",
		"true",
		"on",
		"yes",
		"enabled"
	};
	// false
	public static final String[] falsy = new String[] {
		"0",
		"f",
		"false",
		"off",
		"no",
		"disabled"
	};


	// min/max value
	public static int MinMax(final int value, final int min, final int max) {
		if(min > max) throw new IllegalArgumentException("min cannot be greater than max");
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
	public static byte MinMax(final byte value, final byte min, final byte max) {
		if(min > max) throw new IllegalArgumentException("min cannot be greater than max");
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
	public static short MinMax(final short value, final short min, final short max) {
		if(min > max) throw new IllegalArgumentException("min cannot be greater than max");
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
	public static long MinMax(final long value, final long min, final long max) {
		if(min > max) throw new IllegalArgumentException("min cannot be greater than max");
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
	public static double MinMax(final double value, final double min, final double max) {
		if(min > max) throw new IllegalArgumentException("min cannot be greater than max");
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
	public static float MinMax(final float value, final float min, final float max) {
		if(min > max) throw new IllegalArgumentException("min cannot be greater than max");
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
/*
	// min/max by object
	public static boolean MinMax(Integer value, int min, int max) {
		if(value == null) throw new NullPointerException("value cannot be null");
		boolean changed = false;
		if(value < min) {value = min; changed = true;}
		if(value > max) {value = max; changed = true;}
		return changed;
	}
	public static boolean MinMax(Long value, long min, long max) {
		if(value == null) throw new NullPointerException("value cannot be null");
		boolean changed = false;
		if(value < min) {value = min; changed = true;}
		if(value > max) {value = max; changed = true;}
		return changed;
	}
	public static boolean MinMax(Double value, double min, double max) {
		if(value == null) throw new NullPointerException("value cannot be null");
		boolean changed = false;
		if(value < min) {value = min; changed = true;}
		if(value > max) {value = max; changed = true;}
		return changed;
	}
*/


	// formatDecimal("0.00", double)
	public static String FormatDecimal(final String format, final double value) {
		return (new DecimalFormat(format).format(value));
	}
	// formatDecimal("0.00", float)
	public static String FormatDecimal(final String format, final float value) {
		return (new DecimalFormat(format).format(value));
	}


	// parse number
	public static Integer toInt(final String value) {
		if(utilsString.isEmpty(value)) return null;
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ignore) {}
		return null;
	}
	public static int toInt(final String value, final int defaultValue) {
		Integer num = toInt(value);
		if(num == null) return defaultValue;
		return num.intValue();
	}
	// parse byte
	public static Byte toByte(final String value) {
		if(utilsString.isEmpty(value)) return null;
		try {
			return Byte.parseByte(value);
		} catch (NumberFormatException ignore) {}
		return null;
	}
	// parse short
	public static Short toShort(final String value) {
		if(utilsString.isEmpty(value)) return null;
		try {
			return Short.parseShort(value);
		} catch (NumberFormatException ignore) {}
		return null;
	}
	// parse long
	public static Long toLong(final String value) {
		if(utilsString.isEmpty(value)) return null;
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException ignore) {}
		return null;
	}
	public static long toLong(final String value, final long defaultValue) {
		Long num = toLong(value);
		if(num == null) return defaultValue;
		return num.longValue();
	}
	// parse double
	public static Double toDouble(final String value) {
		if(utilsString.isEmpty(value)) return null;
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException ignore) {}
		return null;
	}
	public static double toDouble(final String value, final double defaultValue) {
		Double num = toDouble(value);
		if(num == null) return defaultValue;
		return num.doubleValue();
	}
	// parse float
	public static Float toFloat(final String value) {
		if(utilsString.isEmpty(value)) return null;
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException ignore) {}
		return null;
	}
	public static float toFloat(final String value, final float defaultValue) {
		Float num = toFloat(value);
		if(num == null) return defaultValue;
		return num.floatValue();
	}
	// parse boolean
	public static Boolean toBoolean(final String value) {
		if(utilsString.isEmpty(value)) return null;
		final String val = value.trim().toLowerCase();
		for(final String v : truesy)
			if(val.equals(v))
				return true;
		for(final String v : falsy)
			if(val.equals(v))
				return false;
		return null;
	}
	public static boolean toBoolean(final String value, final boolean defaultValue) {
		Boolean bool = toBoolean(value);
		if(bool == null) return defaultValue;
		return bool.booleanValue();
	}


	// is number
	public static boolean isNumeric(final String value) {
		if(utilsString.isEmpty(value)) return false;
		return (toLong(value) != null);
	}
	// is boolean
	public static boolean isBoolean(final String value) {
		return (toBoolean(value) != null);
	}


	// compare version numbers
	public static String compareVersions(final String oldVersion, final String newVersion) {
		if(utilsString.isEmpty(oldVersion)) return null;
		if(utilsString.isEmpty(newVersion)) return null;
		final int cmp = normalisedVersion(oldVersion).compareTo(normalisedVersion(newVersion));
		if(cmp < 0) return "<";
		if(cmp > 0) return ">";
		return "=";
		//return cmp<0 ? "<" : cmp>0 ? ">" : "=";
	}
	public static String normalisedVersion(final String version) {
		final String delim = ".";
		final int maxWidth = 5;
		String[] split = Pattern.compile(delim, Pattern.LITERAL).split(version);
		final StringBuilder out = new StringBuilder();
		for(final String str : split)
			out.append(String.format("%"+maxWidth+'s', str));
		return out.toString();
	}


	// random number
	public static int getRandom(final int minNumber, final int maxNumber) {
		final Random randomGen = new Random(utilsSystem.getSystemMillis());
		return randomGen.nextInt(maxNumber - minNumber) + minNumber;
	}
	// random number (not last)
	public static int getNewRandom(final int minNumber, final int maxNumber, final int oldNumber) {
		if(minNumber == maxNumber) return minNumber;
		if((maxNumber - minNumber) == 1) {
			if(oldNumber == minNumber)
				return maxNumber;
			else
				return minNumber;
		}
		int newNumber;
		while(true) {
			newNumber = getRandom(minNumber, maxNumber);
			if(newNumber != oldNumber) return newNumber;
		}
	}


}
