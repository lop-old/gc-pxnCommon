package com.poixson.utils;


public final class SanUtils {
	private SanUtils() {}



	public static void init() {
		Keeper.add(new SanUtils());
	}



	public static String AlphaNum(final String text) {
		if (text == null)   return null;
		if (text.isEmpty()) return "";
		return text.replaceAll("[^a-zA-Z0-9]+", "");
	}
	public static boolean safeAlphaNum(final String text) {
		if (text == null)
			return true;
		final String safeText = AlphaNum(text);
		return text.equals(safeText);
	}



	public static String AlphaNumUnderscore(final String text) {
		if (text == null)   return null;
		if (text.isEmpty()) return "";
		return text.replaceAll("[^a-zA-Z0-9\\-\\_\\.]+", "");
	}
	public static boolean safeAlphaNumUnderscore(final String text) {
		if (text == null)
			return true;
		final String safeText = AlphaNumUnderscore(text);
		return text.equals(safeText);
	}



//TODO: is this useful?
//	public static String AlphaNumPunc(final String text) {
//		if (text == null)   return null;
//		if (text.isEmpty()) return "";
//		return text.replaceAll("[^a-zA-Z0-9\\.\\?\\!]+", "");
//	}



	public static String FileName(final String text) {
		if (Utils.isBlank(text)) return null;
		return text.replaceAll("[^a-zA-Z0-9\\._]+", "_");
	}
	public static boolean safeFileName(final String text) {
		if (text == null)
			return true;
		final String safeText = FileName(text);
		return text.equals(safeText);
	}



	public static String ValidateStringEnum(final String value, final String...valids) {
		if (Utils.isEmpty(value)) return null;
		if (valids.length == 0)   return null;
		for (final String v : valids) {
			if (v == null || v.isEmpty()) continue;
			if (v.equalsIgnoreCase(value))
				return v;
		}
		return null;
	}



}
