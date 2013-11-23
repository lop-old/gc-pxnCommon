package com.poixson.commonjava.Utils;


public final class utilsSan {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	private utilsSan() {}


	public static String AlphaNumOnly(String text) {
		if(text == null) return null;
		if(text.isEmpty()) return "";
		return text.replaceAll("[^a-zA-Z0-9]+", "");
	}
	public static String AlphaNumSafe(String text) {
		if(text == null) return null;
		if(text.isEmpty()) return "";
		return text.replaceAll("[^a-zA-Z0-9\\-\\_\\.]+", "");
	}


//	public static String AlphaNumPunc(String text) {
//		if(text == null) return null;
//		if(text.isEmpty()) return "";
//		return text.replaceAll("[^a-zA-Z0-9\\.\\?\\!]+", "");
//	}


	public static String FileName(String text) {
		return text.replaceAll("[^a-zA-Z0-9\\._]+", "_");
	}


}
