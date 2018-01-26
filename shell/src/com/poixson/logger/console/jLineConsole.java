package com.poixson.logger.console;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import com.poixson.app.xVars;
import com.poixson.logger.xLog;
import com.poixson.logger.commands.xCommandEvent;
import com.poixson.logger.commands.xCommandHandler;
import com.poixson.tools.remapped.OutputStreamLineRemapper;
import com.poixson.utils.StringUtils;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.Utils;

import jline.Terminal;
import jline.console.ConsoleReader;
import jline.console.history.FileHistory;
import jline.console.history.History;


public class jLineConsole implements xConsole {
	protected static final String  CONSOLE_NAME   = "jLineConsole";
	protected static final String  DEFAULT_PROMPT = " #>";
	protected static final boolean OVERRIDE_STDIO = true;
	protected static final boolean BELL_ENABLED   = true;
	protected static final String  HISTORY_FILE   = "./history.txt";
	protected static final int     HISTORY_SIZE   = 1000;

	private static final AtomicBoolean inited = new AtomicBoolean(false);
	private static volatile ConsoleReader reader = null;
	private static volatile Terminal      term   = null;

	private volatile String prompt  = null;
	private volatile Character mask = null;

	private final xCommandHandler handler;
	private final Object printLock = new Object();

	// console input thread
	private volatile Thread thread = null;
	private final AtomicBoolean running  = new AtomicBoolean(false);
	private final AtomicBoolean starting = new AtomicBoolean(false);
	private volatile boolean stopping = false;



	public static void init() {
		if (!inited.compareAndSet(false, true))
			return;
		System.setProperty("user.language",  "en");
		// no console
		if (System.console() == null) {
			System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
			return;
		}
		// get console reader
		try {
			reader = new ConsoleReader(
				getOriginalIn(),
				getOriginalOut()
			);
		} catch (IOException ignore) {
			reader = null;
		}
		// try once more
		if (reader == null) {
			System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
			try {
				reader = new ConsoleReader(
					getOriginalIn(),
					getOriginalOut()
				);
			} catch (IOException e) {
				reader = null;
				log().trace(e);
			}
		}
		if (reader == null)
			return;
		term = reader.getTerminal();
		// jline logger
		jline.internal.Log.setOutput(
			OutputStreamLineRemapper.toPrintStream(
				new OutputStreamLineRemapper() {
					@Override
					public void line(String line) {
						log().publish(line);
					}
				}
			)
		);
		// configure jline
		try {
			reader.setBellEnabled(BELL_ENABLED);
			reader.setExpandEvents(true);
			final FileHistory history =
				new FileHistory(
					new File(HISTORY_FILE)
				);
			history.setMaxSize(HISTORY_SIZE);
			reader.setHistory(history);
			reader.setHistoryEnabled(true);
//TODO: use --color --no-color
//term.isAnsiSupported();
		} catch (Exception e) {
			log().trace(e);
		}
	}
	public jLineConsole() {
		init();
		this.handler = new xCommandHandler();
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	@Override
	public void finalize() {
		if (this.isRunning()) {
			if (!this.isStopping()) {
				this.stop();
			}
		}
	}



	@Override
	public String getName() {
		return CONSOLE_NAME;
	}



	public static ConsoleReader getReader() {
		return reader;
	}
	public static Terminal getTerm() {
		return term;
	}



	public static PrintStream getOriginalOut() {
		return xVars.getOriginalOut();
	}
	public static PrintStream getOriginalErr() {
		return xVars.getOriginalErr();
	}
	public static InputStream getOriginalIn() {
		return xVars.getOriginalIn();
	}



	@Override
	public void start() {
		if (xVars.debug()) {
			log().finest("Start jlineConsole");
		}
		if (this.isStopping()) throw new RuntimeException("Console already stopped");
		if (this.isRunning())  throw new RuntimeException("Console already running");
		if (!this.starting.compareAndSet(false, true)) {
			return;
		}
		// override std out/err
		if (OVERRIDE_STDIO) {
			synchronized(this.printLock) {
				// capture std out
				xVars.setOriginalOut(System.out);
				System.setOut(
					OutputStreamLineRemapper.toPrintStream(
						new OutputStreamLineRemapper() {
							@Override
							public void line(final String line) {
								xLog.getRoot()
									.stdout(line);
							}
						}
					)
				);
				// capture std err
				xVars.setOriginalErr(System.err);
				System.setErr(
					OutputStreamLineRemapper.toPrintStream(
						new OutputStreamLineRemapper() {
							@Override
							public void line(final String line) {
								xLog.getRoot()
									.stderr(line);
							}
						}
					)
				);
			}
		}
		if (this.isStopping())
			throw new RuntimeException("Console already stopped");
		// input listener thread
		if (this.thread == null) {
			this.thread = new Thread(this);
			this.thread.setDaemon(true);
		}
		// start console input thread
		this.thread = new Thread(this);
		this.thread.setDaemon(true);
		this.thread.start();
		if (this.isStopping())
			throw new RuntimeException("Console already stopped");
	}
	@Override
	public void stop() {
		if (this.stopping) return;
		this.stopping = true;
		if (this.running.get()) {
			if (!this.running.compareAndSet(true, false)) {
				return;
			}
		}
		ThreadUtils.Sleep(20L);
		// restore original std out/err
		synchronized(this.printLock){
			// restore out
			System.setOut( xVars.getOriginalOut() );
			xVars.setOriginalOut(null);
			// restore err
			System.setErr( xVars.getOriginalErr() );
			xVars.setOriginalErr(null);
		}
		// stop console input thread
		if (this.thread != null) {
			try {
				this.thread.interrupt();
			} catch (Exception ignore) {}
			try {
				this.thread.notifyAll();
			} catch (Exception ignore) {}
		}
	}



	// input listener loop
	@Override
	public void run() {
		if (!this.running.compareAndSet(false, true)) {
			return;
		}
		final PrintStream out = getOriginalOut();
		final Thread thread = (
				this.thread == null
				? Thread.currentThread()
				: this.thread
			);
		{
			final ConsoleReader readr = getReader();
			if (readr != null) {
				readr.setPrompt(
					this.getPrompt()
				);
			}
		}
		while (true) {
			if (this.isStopping())
				break;
			if (thread.isInterrupted())
				break;
			String line = null;
			try {
				out.print('\r');
				final ConsoleReader readr = getReader();
				if (readr == null) {
					log().warning("reader not set");
					break;
				}
				line = readr.readLine(
					this.getPrompt(),
					this.getMask()
				);
			} catch (Exception e) {
//TODO: is this right?
//				if ("Stream closed".equals(e.getMessage()))
//					break;
				log().trace(e);
				break;
			}
			if (this.isStopping())
				break;
			if (Utils.isBlank(line))
				continue;
			// handle command
			final xCommandHandler handler = this.getCommandHandler();
			if (handler == null) {
				log().severe("Command handler not set!");
				continue;
			}
			final xCommandEvent event = new xCommandEvent(line);
			handler.trigger(event);
		}
		if (!this.stopping) {
			this.stop();
		}
		this.running.set(false);
		// save command history
		{
			final ConsoleReader readr = getReader();
			if (readr != null) {
				final History history = readr.getHistory();
				if (history != null) {
					if (history instanceof FileHistory) {
						try {
							((FileHistory)history).flush();
						} catch (Exception e) {
							log().trace(e);
						}
					}
				}
			}
		}
		this.setPrompt("");
		try {
			getReader().close();
		} catch (Exception ignore) {}
		reader = null;
		out.println();
		out.flush();
		try {
			AnsiConsole.systemUninstall();
		} catch (Exception ignore) {}
	}



	@Override
	public boolean isRunning() {
		if (this.stopping)
			return false;
		return this.running.get();
	}
	@Override
	public boolean isStopping() {
		return this.stopping;
	}



	@Override
	public Object getPrintLockObject() {
		return this.printLock;
	}



	// clear screen
	@Override
	public void clear() {
		final PrintStream out = getOriginalOut();
		synchronized(this.printLock) {
			out.println(
				Ansi.ansi()
					.eraseScreen()
					.cursor(0, 0)
			);
			out.flush();
		}
	}
	@Override
	public void clearLine() {
		final PrintStream out = getOriginalOut();
		final String prompt = this.getPrompt();
		synchronized(this.printLock) {
			if (Utils.isEmpty(prompt)) {
				out.print('\r');
				this.flush();
				return;
			}
			final ConsoleReader readr = getReader();
			int wipeLen = prompt.length() + 2;
			if (readr != null) {
				final int bufLen = readr.getCursorBuffer().length();
				wipeLen += bufLen;
			}
			out.print(
				(new StringBuilder())
					.append('\r')
					.append(
						StringUtils.Repeat(
							wipeLen,
							' '
						)
					)
					.append('\r')
					.toString()
			);
			this.flush();
		}
	}
	@Override
	public void flush() {
		try {
			final PrintStream out = getOriginalOut();
			synchronized(this.printLock) {
				out.flush();
			}
		} catch (Exception ignore) {}
	}



	// print then restore prompt
	@Override
	public void print(final String msg) {
		// render jAnsi
		final String str = renderAnsi(msg);
		synchronized(this.printLock){
			this.clearLine();
			// print line
			getOriginalOut()
				.println(
					(new StringBuilder())
						.append('\r')
						.append(str)
						.toString()
				);
			// print command prompt
			this.drawPrompt();
		}
	}



	// command prompt string
	@Override
	public String getPrompt() {
		final String prompt = this.prompt;
		return (
			Utils.isEmpty(prompt)
			? DEFAULT_PROMPT
			: prompt
		);
	}
	@Override
	public void setPrompt(final String prompt) {
		this.prompt = prompt;
		final ConsoleReader readr = getReader();
		if (readr != null) {
			readr.setPrompt(
				this.getPrompt()
			);
			this.drawPrompt();
		}
	}
	@Override
	public void drawPrompt() {
		final ConsoleReader readr = getReader();
		if (readr != null) {
			try {
				synchronized(this.printLock){
					this.clearLine();
					readr.drawLine();
					readr.flush();
				}
			} catch (IOException e) {
				log().trace(e);
			}
		}
	}



	@Override
	public Character getMask() {
		return this.mask;
	}
	@Override
	public void setMask(final Character mask) {
		this.mask = (
			Utils.isEmpty(mask)
			? null
			: mask
		);
	}



	// render jAnsi
	public static String renderAnsi(final String msg) {
		return
			Ansi.ansi()
				.render(msg)
				.toString();
	}



	@Override
	public xCommandHandler getCommandHandler() {
		return this.handler;
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
		final xLog log =
			xLog.getRoot()
				.get("jLine");
		_log = new SoftReference<xLog>(log);
		return log;
	}



}
