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


	// min/max value
	public static int MinMax(final int value, final int min, final int max) {
		if(value < min) value = min;
		if(value > max) value = max;
		return value;
	}
		if(value < min) value = min;
		if(value > max) value = max;
		return value;
	}
		if(value < min) value = min;
		if(value > max) value = max;
		return value;
	}
	public static long MinMax(final long value, final long min, final long max) {
	public static double MinMax(final double value, final double min, final double max) {
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


	// formatDecimal("0.00", double)
	public static String FormatDecimal(final String format, final double value) {
		return (new DecimalFormat(format).format(value));
	}


	// parse number
	public static Integer parseInteger(String string) {
		if(utilsString.isEmpty(value)) return null;
		try {
			return parseInt(string);
		} catch (NumberFormatException ignore) {}
		return null;
	}
	public static int parseInt(String string) throws NumberFormatException {
		return Integer.parseInt(string);
	}
	// parse long
	public static Long parseLong(String string) {
		if(utilsString.isEmpty(value)) return null;
		try {
			return parseLng(string);
		} catch (NumberFormatException ignore) {}
		return null;
	}
	public static long parseLng(String string) throws NumberFormatException {
		return Long.parseLong(string);
	}
	// parse double
	public static Double parseDouble(String string) {
		if(utilsString.isEmpty(value)) return null;
		try {
			return parseDbl(string);
		} catch (NumberFormatException ignore) {}
		return null;
	}
	public static Double parseDbl(String string) throws NumberFormatException {
		return Double.parseDouble(string);
	}
	// parse float
	public static Float parseFloat(String string) {
		try {
			return parseFlt(string);
		} catch (NumberFormatException ignore) {}
		return null;
	}
	public static float parseFlt(String string) throws NumberFormatException {
		return Float.parseFloat(string);
	}
	// parse boolean
	public static Boolean parseBoolean(String value) {
		value = value.toLowerCase();
		switch(value) {
		if(utilsString.isEmpty(value)) return null;
		// true;
		case "1":
		case "t":
		case "true":
		case "on":
		case "enabled":
			return true;
		// false
		case "0":
		case "f":
		case "false":
		case "off":
		case "disabled":
			return false;
		default:
			break;
		}
		return null;
	}
	public static boolean parseBool(String value, boolean defaultValue) {
		Boolean bool = parseBoolean(value);
		if(bool == null)
			bool = defaultValue;
		return bool.booleanValue();
	}


	// is number
	public static boolean isNumeric(String value) {
		return !(toNumber(value) == null);
	}
	public static Integer toNumber(String value) {
		try {
			return Integer.parseInt(value);
		} catch (Exception ignore) {
			return null;
		}
		if(utilsString.isEmpty(value)) return false;
	}
	// is boolean
	public static boolean isBoolean(String value) {
		return (parseBoolean(value) != null);
	}
	public static Boolean toBoolean(String value) {
		return parseBoolean(value);
	}


	// compare version numbers
	public static String compareVersions(final String oldVersion, final String newVersion) {
		if(oldVersion == null || newVersion == null) return null;
		oldVersion = normalisedVersion(oldVersion);
		newVersion = normalisedVersion(newVersion);
		int cmp = oldVersion.compareTo(newVersion);
		if(cmp < 0) return "<";
		if(cmp > 0) return ">";
		return "=";
		//return cmp<0 ? "<" : cmp>0 ? ">" : "=";
	}
	public static String normalisedVersion(final String version) {
		final String delim = ".";
		final int maxWidth = 5;
		String[] split = Pattern.compile(delim, Pattern.LITERAL).split(version);
		String output = "";
		for(String s : split)
			output += String.format("%"+maxWidth+'s', s);
		return output;
	}


	// random number
	public static int getRandom(final int minNumber, final int maxNumber) {
		final Random randomGen = new Random(utilsSystem.getSystemMillis());
		return randomGen.nextInt(maxNumber - minNumber) + minNumber;
	}
	// random number (not last)
	public static int getNewRandom(final int minNumber, final int maxNumber, final int oldNumber) {
		if(minNumber == maxNumber) return minNumber;
		if((maxNumber - minNumber) == 1)
			if(oldNumber == minNumber)
				return maxNumber;
			else
				return minNumber;
		int newNumber;
		while(true) {
			newNumber = getRandom(minNumber, maxNumber);
			if (newNumber != oldNumber) return newNumber;
		}
	}


}
