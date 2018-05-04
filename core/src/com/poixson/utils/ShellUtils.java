package com.poixson.utils;

import java.util.concurrent.atomic.AtomicReference;


public class ShellUtils {

	private static final AtomicReference<ShellUtils> instance =
			new AtomicReference<ShellUtils>(null);



	private static ShellUtils get() {
		// existing instance
		{
			final ShellUtils util = instance.get();
			if (util != null)
				return util;
		}
		// extended utils if available
		try {
			final Class<?> clss = Class.forName("com.poixson.utils.ShellUtils_Extended");
			if (clss != null) {
				ShellUtils util;
				util = (ShellUtils) clss.newInstance();
				if (instance.compareAndSet(null, util))
					return util;
				return instance.get();
			}
		} catch (ClassNotFoundException ignore) {
		} catch (InstantiationException ignore) {
		} catch (IllegalAccessException ignore) {}
		// new default instance
		{
			final ShellUtils util = new ShellUtils();
			if (instance.compareAndSet(null, util))
				return util;
			return instance.get();
		}
	}
	protected ShellUtils() {
	}



	public static String RenderAnsi(final String line) {
		return get().renderAnsi(line);
	}
	public static String[] RenderAnsi(final String[] lines) {
		return get().renderAnsi(lines);
	}



	// ------------------------------------------------------------------------------- //
	// colors



	protected String renderAnsi(final String line) {
		return StripColorTags(line);
	}
	protected String[] renderAnsi(final String[] lines) {
		return StripColorTags(lines);
	}



	// strip color tags
	public static String StripColorTags(final String line) {
		if (Utils.isEmpty(line))
			return line;
		final StringBuilder result = new StringBuilder(line);
		boolean changed = false;
		while (true) {
			final int posA = result.indexOf("@|");
			if (posA == -1) break;
			final int posB = result.indexOf(" ", posA);
			final int posC = result.indexOf("|@", posB);
			if (posB == -1) break;
			if (posC == -1) break;
			result.replace(posC, posC+2, "");
			result.replace(posA, posB+1, "");
			changed = true;
		}
		if (changed)
			return result.toString();
		return line;
	}
	public static String[] StripColorTags(final String[] lines) {
		if (Utils.isEmpty(lines))
			return lines;
		String[] result = new String[ lines.length ];
		for (int index=0; index<lines.length; index++) {
			result[index] =
				StripColorTags(
					result[index]
				);
		}
		return result;
	}



}
