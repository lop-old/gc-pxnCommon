package com.poixson.app;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicReference;

import com.poixson.logger.xLogRoot;
import com.poixson.tools.Keeper;
import com.poixson.utils.Utils;


public final class xVars {
	private xVars() {}

	// defaults
	private static final boolean DEFAULT_DEBUG              = false;

	private static final boolean DEFAULT_CONSOLE_COLOR      = true;

	public  static final int     MAX_JLINE_HISTORY_SIZE     = 10000;
	private static final int     DEFAULT_JLINE_HISTORY_SIZE = 200;
	private static final String  DEFAULT_JLINE_HISTORY_FILE = null;

	public static final String[] SEARCH_DEBUG_FILES =
		new String[] {
			"debug",
			".debug"
		};



	// debug mode
	private static final AtomicReference<Boolean> debug =
			new AtomicReference<Boolean>(null);

	// original out/err/in
	private static final AtomicReference<PrintStream> originalOut =
			new AtomicReference<PrintStream>(null);
	private static final AtomicReference<PrintStream> originalErr =
			new AtomicReference<PrintStream>(null);
	private static final AtomicReference<InputStream> originalIn  =
			new AtomicReference<InputStream>(null);

	// console color
	private static final AtomicReference<Boolean> colorEnabled =
			new AtomicReference<Boolean>(null);

	// jline history size
	private static final AtomicReference<Integer> jlineHistorySize =
			new AtomicReference<Integer>(null);
	private static final AtomicReference<String> jlineHistoryFile =
			new AtomicReference<String>(null);



	// init
	static {
		Keeper.add(new xVars());
	}



	// ------------------------------------------------------------------------------- //
	// debug mode



	public static boolean isDebug() {
		final Boolean bool = debug.get();
		// default
		if (bool == null)
			return DEFAULT_DEBUG;
		return bool.booleanValue();
	}
	public static boolean notDebug() {
		return ! isDebug();
	}
	public static Boolean peekDebug() {
		return debug.get();
	}
	public static void setDebug(final boolean enable) {
		final Boolean previous =
			debug.getAndSet(
				Boolean.valueOf(enable)
			);
		if (previous != null) {
			if (previous.booleanValue()) {
				xLogRoot.get()
					.info("Enabled debug mode");
			} else {
				final xLogRoot log = xLogRoot.peek();
				if (log != null) {
					log.info("Disabled debug mode");
				}
			}
		}
	}



	// ------------------------------------------------------------------------------- //
	// original out/err/in



	// out stream
	public static PrintStream getOriginalOut() {
		final PrintStream out = originalOut.get();
		return (
			out == null
			? System.out
			: out
		);
	}
	public static void setOriginalOut(final PrintStream out) {
		if (out != null) {
			originalOut.set(out);
		}
	}

	// err stream
	public static PrintStream getOriginalErr() {
		final PrintStream err = originalErr.get();
		return (
			err == null
			? System.err
			: err
		);
	}
	public static void setOriginalErr(final PrintStream err) {
		if (err != null) {
			originalErr.set(err);
		}
	}

	// in stream
	public static InputStream getOriginalIn() {
		final InputStream in = originalIn.get();
		return (
			in == null
			? System.in
			: in
		);
	}
	public static void setOriginalIn(final InputStream in) {
		if (in != null) {
			originalIn.set(in);
		}
	}



	// ------------------------------------------------------------------------------- //
	// console color



	public static boolean isColorEnabled() {
		final Boolean bool = colorEnabled.get();
		// default
		if (bool == null)
			return DEFAULT_CONSOLE_COLOR;
		return bool.booleanValue();
	}
	public static boolean isColorDisabled() {
		return ! isColorEnabled();
	}
	public static Boolean peekColorEnabled() {
		return colorEnabled.get();
	}
	public static void setColorEnabled(final boolean enable) {
		if (enable) {
			if (System.console() == null) {
				final xLogRoot log = xLogRoot.peek();
				if (log != null) {
					log.fine("Cannot enable console color");
				}
			}
		}
		final Boolean previous =
			colorEnabled.getAndSet(
				Boolean.valueOf(enable)
			);
		if (previous != null) {
			final xLogRoot log = xLogRoot.peek();
			if (log != null) {
				if (previous.booleanValue()) {
					log.fine("Enabled console color");
				} else {
					log.fine("Disabled console color");
				}
			}
		}
	}



	// ------------------------------------------------------------------------------- //
	// jline history size



	// history size
	public static int getJLineHistorySize() {
		final Integer size = jlineHistorySize.get();
		// default
		if (size == null)
			return DEFAULT_JLINE_HISTORY_SIZE;
		return size.intValue();
	}
	public static Integer peekJLineHistorySize() {
		return jlineHistorySize.get();
	}
	public static boolean isJLineHistory() {
		return (getJLineHistorySize() != 0);
	}
	public static boolean notJLineHistory() {
		return ! isJLineHistory();
	}
	public static void setJLineHistorySize(final int size) {
//TODO: update print handlers
		final int value;
		if (size < -1) {
			value = 0;
		} else
		if (size > MAX_JLINE_HISTORY_SIZE) {
			value = MAX_JLINE_HISTORY_SIZE;
		} else {
			value = size;
		}
		jlineHistorySize.set(
			Integer.valueOf(value)
		);
	}



	// history file
	public static String getJLineHistoryFileStr() {
		final String fileStr = jlineHistoryFile.get();
		// mark using default
		if (fileStr == null) {
			jlineHistoryFile.set("");
			return DEFAULT_JLINE_HISTORY_FILE;
		}
		// using default
		if (fileStr.length() == 0) {
			return DEFAULT_JLINE_HISTORY_FILE;
		}
		return fileStr;
	}
	public static File getJLineHistoryFile() {
		final String fileStr = getJLineHistoryFileStr();
		return (
			Utils.isEmpty(fileStr)
			? null
			: new File(fileStr)
		);
	}
	public static String peekJLineHistoryFile() {
		return jlineHistoryFile.get();
	}
	public static void setJLineHistoryFile(final String fileStr) {
//TODO: update print handlers
		jlineHistoryFile.set(fileStr);
	}



}
