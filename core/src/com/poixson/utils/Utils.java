package com.poixson.utils;

import java.io.Closeable;
import java.io.PrintStream;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import com.poixson.app.xVars;
import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.xLevel;
import com.poixson.logger.xLog;
import com.poixson.logger.xLogRoot;
import com.poixson.tools.Keeper;


public final class Utils {
	private Utils() {}
	{ Keeper.add(new Utils()); }



	public enum jLineVersion {
		version2x,
		version3x
	}
	public static final jLineVersion JLINE_VERSION = jLineVersion.version2x;



	public static boolean isArray(final Object obj) {
		if (obj == null)
			return false;
		return obj.getClass().isArray();
	}
	public static boolean notArray(final Object obj) {
		if (obj == null)
			return true;
		return ! obj.getClass().isArray();
	}



	/**
	 * Is string empty.
	 * @param String
	 * @return True if string is null or empty.
	 */
	public static boolean isEmpty(final String value) {
		return (value == null || value.length() == 0);
	}
	/**
	 * Is string populated.
	 * @param String
	 * @return True if string is not null and contains data.
	 */
	public static boolean notEmpty(final String value) {
		return (value != null && value.length() > 0);
	}



	public static boolean isBlank(final String value) {
		if (isEmpty(value)) {
			return true;
		}
		for (int i=0; i < value.length(); i++) {
			if ( ! Character.isWhitespace(value.charAt(i)) ) {
				return false;
			}
		}
		return true;
	}
	public static boolean notBlank(final String value) {
		return ! isBlank(value);
	}



	public static String DefaultIfBlank(final String value, final String defaultStr) {
		return (
			isBlank(value)
			? defaultStr
			: value
		);
	}



	/**
	 * Is array empty.
	 * @param Object[]
	 * @return True if array is null or empty.
	 */
	public static boolean isEmpty(final Object[] array) {
		return (array == null || array.length == 0);
	}
	/**
	 * Is array populated.
	 * @param Object[]
	 * @return True if array is not null and contains data.
	 */
	public static boolean notEmpty(final Object[] array) {
		return (array != null && array.length > 0);
	}



	/**
	 * Is collection/set/list empty.
	 * @param Collection or Set or List
	 * @return True if collection is null or empty.
	 */
	public static boolean isEmpty(final Collection<?> collect) {
		return (collect == null || collect.isEmpty());
	}
	/**
	 * Is collection/set/list populated.
	 * @param Collection or Set or List
	 * @return True if collection is not null and contains data.
	 */
	public static boolean notEmpty(final Collection<?> collect) {
		return (collect != null && !collect.isEmpty());
	}



	/**
	 * Is map empty.
	 * @param Map
	 * @return True if map is null or empty.
	 */
	public static boolean isEmpty(final Map<?, ?> map) {
		return (map == null || map.isEmpty());
	}
	/**
	 * Is map populated.
	 * @param Map
	 * @return True if map is not null and contains data.
	 */
	public static boolean notEmpty(final Map<?, ?> map) {
		return (map != null && !map.isEmpty());
	}



	/**
	 * Is byte array empty.
	 * @param byte[]
	 * @return True if array is null or empty.
	 */
	public static boolean isEmpty(final byte[] bytes) {
		return (bytes == null || bytes.length == 0);
	}
	/**
	 * Is byte array populated.
	 * @param byte[]
	 * @return True if array is not null and contains data.
	 */
	public static boolean notEmpty(final byte[] bytes) {
		return (bytes != null && bytes.length != 0);
	}



	/**
	 * Is char array empty.
	 * @param char[]
	 * @return True if array is null or empty.
	 */
	public static boolean isEmpty(final char[] chars) {
		return (chars == null || chars.length == 0);
	}
	/**
	 * Is char array populated.
	 * @param char[]
	 * @return True if array is not null and contains data.
	 */
	public static boolean notEmpty(final char[] chars) {
		return (chars != null && chars.length != 0);
	}



	/**
	 * Is Character empty.
	 * @param Character
	 * @return True if chr is null.
	 */
	public static boolean isEmpty(final Character chr) {
		return (chr == null);
	}
	/**
	 * is Character populated.
	 * @param Character
	 * @return True if chr contains a value.
	 */
	public static boolean notEmpty(final Character chr) {
		return (chr != null);
	}



	/**
	 * Is short array empty.
	 * @param short[]
	 * @return True if array is null or empty.
	 */
	public static boolean isEmpty(final short[] shorts) {
		return (shorts == null || shorts.length == 0);
	}
	/**
	 * Is short array populated.
	 * @param short[]
	 * @return True if array is not null and contains data.
	 */
	public static boolean notEmpty(final short[] shorts) {
		return (shorts != null && shorts.length != 0);
	}



	/**
	 * Is int array empty.
	 * @param int[]
	 * @return True if array is null or empty.
	 */
	public static boolean isEmpty(final int[] ints) {
		return (ints == null || ints.length == 0);
	}
	/**
	 * Is int array populated.
	 * @param int[]
	 * @return True if array is not null and contains data.
	 */
	public static boolean notEmpty(final int[] ints) {
		return (ints != null && ints.length != 0);
	}



	/**
	 * Is long array empty.
	 * @param long[]
	 * @return True if array is null or empty.
	 */
	public static boolean isEmpty(final long[] longs) {
		return (longs == null || longs.length == 0);
	}
	/**
	 * Is long array populated.
	 * @param long[]
	 * @return True if array is not null and contains data.
	 */
	public static boolean notEmpty(final long[] longs) {
		return (longs != null && longs.length != 0);
	}



	/**
	 * Is double array empty.
	 * @param double[]
	 * @return True if array is null or empty.
	 */
	public static boolean isEmpty(final double[] doubles) {
		return (doubles == null || doubles.length == 0);
	}
	/**
	 * Is double array populated.
	 * @param double[]
	 * @return True if array is not null and contains data.
	 */
	public static boolean notEmpty(final double[] doubles) {
		return (doubles != null && doubles.length != 0);
	}



	/**
	 * Is float array empty.
	 * @param float[]
	 * @return True if array is null or empty.
	 */
	public static boolean isEmpty(final float[] floats) {
		return (floats == null || floats.length == 0);
	}
	/**
	 * Is float array populated.
	 * @param float[]
	 * @return True if array is not null and contains data.
	 */
	public static boolean notEmpty(final float[] floats) {
		return (floats != null && floats.length != 0);
	}



	/**
	 * Is boolean array empty.
	 * @param boolean[]
	 * @return True if array is null or empty.
	 */
	public static boolean isEmpty(final boolean[] bools) {
		return (bools == null || bools.length == 0);
	}
	/**
	 * Is boolean array populated.
	 * @param boolean[]
	 * @return True if array is not null and contains data.
	 */
	public static boolean notEmpty(final boolean[] bools) {
		return (bools != null && bools.length != 0);
	}



	/**
	 * Close safely, ignoring errors.
	 */
	public static void safeClose(Closeable obj) {
		if (obj == null) return;
		try {
			obj.close();
		} catch (Exception ignore) {}
	}
	public static void safeClose(AutoCloseable obj) {
		if (obj == null) return;
		try {
			obj.close();
		} catch (Exception ignore) {}
	}



	/**
	 * Current system time ms.
	 * @return
	 */
	public static long getSystemMillis() {
		return System.currentTimeMillis();
	}



	public static void MemoryStats() {
		MemoryStats(
			xLogRoot.get()
		);
	}
	public static void MemoryStats(final xLog log) {
		MemoryStats(
			null,
			log
		);
	}
	public static void MemoryStats(final xLevel level, final xLog log) {
		final int[] stats = getMemoryStats();
		final String[] str = new String[4];
		int longest = 0;
		for (int i=0; i<4; i++) {
			str[i] = Integer.toString(stats[i]);
			if (str[i].length() > longest) {
				longest = str[i].length();
			}
		}
		log.publish( level, "##### Heap utilization statistics [MB] #####" );
		log.publish( level, "Used Memory:  {} MB", StringUtils.PadFront(longest, str[0], ' ') );
		log.publish( level, "Free Memory:  {} MB", StringUtils.PadFront(longest, str[1], ' ') );
		log.publish( level, "Total Memory: {} MB", StringUtils.PadFront(longest, str[2], ' ') );
		log.publish( level, "Max Memory:   {} MB", StringUtils.PadFront(longest, str[3], ' ') );
	}
	public static int[] getMemoryStats() {
		final int MB = 1024 * 1024;
		final Runtime runtime = Runtime.getRuntime();
		return new int[] {
				(int) ((runtime.totalMemory() - runtime.freeMemory()) / MB),
				(int) (runtime.freeMemory()  / MB),
				(int) (runtime.totalMemory() / MB),
				(int) (runtime.maxMemory()   / MB)
		};
	}



	public static void ListProperties() {
		ListProperties(xVars.getOriginalErr());
	}
	public static void ListProperties(final PrintStream out) {
		final Properties props = System.getProperties();
		props.list(out);
	}



	public static boolean isLibAvailable(final String classpath) {
		try {
			Class.forName(classpath);
			return true;
		} catch (ClassNotFoundException ignore) {}
		return false;
	}
	public static boolean isJLineAvailable() {
		switch (JLINE_VERSION) {
		case version2x:
			if (!isLibAvailable("jline.console.ConsoleReader"))
				return false;
			if (!isLibAvailable("jline.Terminal"))
				return false;
			return true;
		case version3x:
			if (!isLibAvailable("org.jline.reader.LineReader"))
				return false;
			if (!isLibAvailable("org.jline.terminal.Terminal"))
				return false;
			return true;
		default:
			break;
		}
		return false;
	}
/*
	public static boolean isRxtxAvailable() {
		return isLibAvailable("gnu.io.CommPortIdentifier");
	}
*/



/*
	public static boolean validBaud(final int baud) {
		switch (baud) {
		case 300:
		case 1200:
		case 2400:
		case 4800:
		case 9600:
		case 14400:
		case 19200:
		case 28800:
		case 38400:
		case 57600:
		case 115200:
			return true;
		default:
		}
		return false;
	}
*/



	// compare version numbers
	public static String CompareVersions(final String versionA, final String versionB) {
		if (Utils.isEmpty(versionA)) throw new RequiredArgumentException("versionA");
		if (Utils.isEmpty(versionB)) throw new RequiredArgumentException("versionB");
		final String[] norms = NormalizeVersions(versionA, versionB);
		final int cmp = norms[0].compareTo(norms[1]);
		if (cmp < 0) return "<";
		if (cmp > 0) return ">";
		return "=";
	}
	private static String[] NormalizeVersions(final String versionA, final String versionB) {
		if (Utils.isEmpty(versionA)) throw new RequiredArgumentException("versionA");
		if (Utils.isEmpty(versionB)) throw new RequiredArgumentException("versionB");
		// split string by .
		final String[] splitA = Pattern.compile(".", Pattern.LITERAL).split(versionA);
		final String[] splitB = Pattern.compile(".", Pattern.LITERAL).split(versionB);
		if (Utils.isEmpty(splitA)) throw new RuntimeException();
		if (Utils.isEmpty(splitB)) throw new RuntimeException();
		// find longest part
		int width = -1;
		for (final String part : splitA) {
			if (width == -1 || width < part.length()) {
				width = part.length();
			}
		}
		for (final String part : splitB) {
			if (width == -1 || width < part.length()) {
				width = part.length();
			}
		}
		if (width == -1) throw new RuntimeException();
		// build padded string
		final StringBuilder outA = new StringBuilder();
		for (final String part : splitA) {
			outA.append( StringUtils.PadFront(width, part, '0') );
		}
		final StringBuilder outB = new StringBuilder();
		for (final String part : splitB) {
			outB.append( StringUtils.PadFront(width, part, '0') );
		}
		return new String[] {
			outA.toString(),
			outB.toString()
		};
	}



	public static boolean isVersionEqual(final String versionA, final String versionB) {
		final String result = CompareVersions(versionA, versionB);
		if (Utils.isEmpty(result))
			return false;
		return result.equals("=");
	}
	public static boolean isVersionGreater(final String versionA, final String versionB) {
		final String result = CompareVersions(versionA, versionB);
		if (Utils.isEmpty(result))
			return false;
		return result.equals(">");
	}
	public static boolean isVersionLess(final String versionA, final String versionB) {
		final String result = CompareVersions(versionA, versionB);
		if (Utils.isEmpty(result))
			return false;
		return result.equals("<");
	}
	public static boolean isVersionGreaterOrEqual(final String versionA, final String versionB) {
		final String result = CompareVersions(versionA, versionB);
		if (Utils.isEmpty(result))
			return false;
		return result.equals(">") || result.equals("=");
	}
	public static boolean isVersionLessOrEqual(final String versionA, final String versionB) {
		final String result = CompareVersions(versionA, versionB);
		if (Utils.isEmpty(result))
			return false;
		return result.equals("<") || result.equals("=");
	}



	public static boolean CheckJavaVersion(final String requiredVersion) {
		final String javaVersion;
		{
			final String vers = System.getProperty("java.version");
			if (vers == null || vers.isEmpty())
				throw new RuntimeException("Failed to get java version");
			javaVersion = vers.replace('_', '.');
		}
		return isVersionGreaterOrEqual(javaVersion, requiredVersion);
	}



	public static void SleepDot(final String msg) {
		SleepDot(msg, 1.0);
	}
	public static void SleepDot(final String msg, final double time) {
		final PrintStream out = xVars.getOriginalErr();
		String str = msg;
		str = StringUtils.TrimEnd(str, "\r", "\n", " ", ".");
		str = StringUtils.ForceStarts(" ", str);
		out.print(str);
		final long dotTime = (long) (((time - 0.1) / 3.0) * 1000.0);
		ThreadUtils.Sleep("0.1s");
		out.print(".");
		ThreadUtils.Sleep(dotTime);
		out.print(".");
		ThreadUtils.Sleep(dotTime);
		out.print(".");
		ThreadUtils.Sleep(dotTime);
		out.println();
	}



	// logger
	private static volatile SoftReference<xLog> _log = null;
	public static xLog log() {
		if (_log != null) {
			final xLog log = _log.get();
			if (log != null) {
				return log;
			}
		}
		final xLog log = xLogRoot.get();
		_log = new SoftReference<xLog>(log);
		return log;
	}



}
