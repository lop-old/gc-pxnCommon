package com.poixson.utils;

import com.poixson.tools.Keeper;

public final class SanUtils {
	private SanUtils() {}
	static { Keeper.add(new SanUtils()); }



	public static String AlphaNum(final String text) {
		return AlphaNum(text, "");
	}
	public static String AlphaNum(final String text, final String replacement) {
		if (text == null)   return null;
		if (text.isEmpty()) return "";
		return text.replaceAll(
			"[^a-zA-Z0-9]+",
			(replacement == null ? "" : replacement)
		);
	}
	public static boolean SafeAlphaNum(final String text) {
		if (text == null)
			return true;
		final String safeText = AlphaNum(text, null);
		return text.equals(safeText);
	}



	public static String AlphaNumUnderscore(final String text) {
		return AlphaNumUnderscore(text, "");
	}
	public static String AlphaNumUnderscore(final String text, final String replacement) {
		if (text == null)   return null;
		if (text.isEmpty()) return "";
		return text.replaceAll(
			"[^a-zA-Z0-9\\-\\_\\.]+",
			(replacement == null ? "" : replacement)
		);
	}
	public static boolean SafeAlphaNumUnderscore(final String text) {
		if (text == null)
			return true;
		final String safeText = AlphaNumUnderscore(text, null);
		return text.equals(safeText);
	}



//TODO: is this useful?
//	public static String AlphaNumPunc(final String text) {
//		if (text == null)   return null;
//		if (text.isEmpty()) return "";
//		return text.replaceAll("[^a-zA-Z0-9\\.\\?\\!]+", "");
//	}



	public static String FileName(final String text) {
		return FileName(text, "_");
	}
	public static String FileName(final String text, final String replacement) {
		if (Utils.isBlank(text)) return null;
		return text.replaceAll(
			"[^a-zA-Z0-9\\._-]+",
			(replacement == null ? "" : replacement)
		);
	}
	public static boolean SafeFileName(final String text) {
		if (text == null)
			return true;
		final String safeText = FileName(text, null);
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
