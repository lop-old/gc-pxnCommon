package com.poixson.utils;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import com.poixson.utils.xLogger.xLog;


public class xVars {
	private xVars() {}
	{ Keeper.add(new xVars()); }

	private static final boolean DEFAULT_DEBUG = false;



	// ------------------------------------------------------------------------------- //
	// debug mode



	private static volatile Boolean debug = null;
	private static final Object debugLock = new Object();

	public static boolean debug() {
		final Boolean bool = debug;
		if (bool == null) {
			return DEFAULT_DEBUG;
		}
		return bool.booleanValue();
	}
	public static void debug(final boolean value) {
		synchronized(debugLock) {
			// check existing value
			final Boolean bool = debug;
			if (bool != null) {
				if (bool.booleanValue() == value) {
					return;
				}
			}
			// change debug state
			if (!value) {
				xLog.getRoot()
					.fine("Disabled debug mode");
			}
			debug = Boolean.valueOf(value);
			if (value) {
				xLog.getRoot()
					.fine("Enabled debug mode");
			}
		}
	}



	// ------------------------------------------------------------------------------- //
	// original std out/err/in



	private static volatile PrintStream originalOut = null;
	private static volatile PrintStream originalErr = null;
	private static volatile InputStream originalIn  = null;

	// original std-out stream
	public static PrintStream getOriginalOut() {
		final PrintStream out = originalOut;
		return (
			out == null
			? System.out
			: out
		);
	}
	public static void setOriginalOut(final PrintStream out) {
		if (out != null) {
			originalOut = out;
		}
	}

	// original std-err stream
	public static PrintStream getOriginalErr() {
		final PrintStream err = originalErr;
		return (
			err == null
			? System.err
			: err
		);
	}
	public static void setOriginalErr(final PrintStream err) {
		if (err != null) {
			originalErr = err;
		}
	}

	// original std-in stream
	public static InputStream getOriginalIn() {
		final InputStream in = originalIn;
		return (
			in == null
			? System.in
			: in
		);
	}
	public static void setOriginalIn(final InputStream in) {
		if (in != null) {
			originalIn = in;
		}
	}



	// ------------------------------------------------------------------------------- //
	// console settings



	// console color
	private static volatile Boolean consoleColorEnabled = null;

	public static Boolean getConsoleColorEnabled() {
		return consoleColorEnabled;
	}
	public static boolean isConsoleColorEnabled() {
		final Boolean enabled = consoleColorEnabled;
		if (enabled == null)
			return false;
		return enabled.booleanValue();
	}
	public static boolean isConsoleColorDisabled() {
		final Boolean enabled = consoleColorEnabled;
		if (enabled == null)
			return false;
		return enabled.booleanValue();
	}
	public static void setConsoleColor(final boolean enabled) {
		consoleColorEnabled = Boolean.valueOf(enabled);
	}



	// ------------------------------------------------------------------------------- //
	// jline history



	// history size
	private static volatile int jLineHistorySize = 200;

	public static int getJLineHistorySize() {
		return jLineHistorySize;
	}
	public static void setJLineHistorySize(final int size) {
		jLineHistorySize =
			NumberUtils.MinMax(
				size,
				1,
				10000
			);
	}



	// history file
	private static volatile File jLineHistoryFile = null;

	public static File getJLineHistoryFile() {
		return jLineHistoryFile;
	}
	public static void setJLineHistoryFile(final String fileStr) {
		setJLineHistoryFile(
			new File(fileStr)
		);
	}
	public static void setJLineHistoryFile(final File file) {
		jLineHistoryFile = file;
	}



}
