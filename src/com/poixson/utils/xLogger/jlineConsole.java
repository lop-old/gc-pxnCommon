package com.poixson.utils.xLogger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import com.poixson.utils.ApacheSystemUtils;
import com.poixson.utils.StringUtils;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.xVars;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;
import jline.console.history.History;


public class jlineConsole implements xConsole {
	protected static final String DEFAULT_PROMPT = " #>";
	protected static final boolean OVERRIDE_STDIO = false;

	private static final Object printLock = new Object();
	private static volatile ConsoleReader reader = null;
	private static volatile Boolean jlineEnabled = null;

	private volatile String prompt = null;
	private volatile xCommandHandler handler = null;

	// console input thread
	private volatile Thread  thread = null;
	private volatile boolean stopping = false;
	private final AtomicBoolean running  = new AtomicBoolean(false);
	private final AtomicBoolean starting = new AtomicBoolean(false);



	public static enum BellType {
		NONE,
		AUDIBLE,
		VISIBLE;

		public static BellType byString(final String bellStr) {
			if (Utils.isEmpty(bellStr))
				return null;
			switch (bellStr.toLowerCase()) {
			case "audible":
			case "beep":
			case "audio":
			case "sound":
				return BellType.AUDIBLE;
			case "visible":
			case "visual":
			case "screen":
				return BellType.VISIBLE;
			default:
				break;
			}
			return BellType.NONE;
		}

	};

	private volatile BellType bellStyle = BellType.VISIBLE;



	public jlineConsole() {
		// console reader
		if (reader == null) {
			jlineEnabled = null;
			// no console
			if (System.console() == null) {
				jlineEnabled = Boolean.FALSE;
				System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
				return;
			}
			// set os type
			if (ApacheSystemUtils.IS_OS_LINUX) {
				System.setProperty("jline.terminal", "jline.UnixTerminal");
			} else
			if (ApacheSystemUtils.IS_OS_WINDOWS) {
				System.setProperty("jline.terminal", "jline.WindowsTerminal");
			} else {
				System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
			}
			// get console reader
			try {
				reader = new ConsoleReader(
					System.in,
					getOriginalOut()
				);
			} catch (IOException ignore) {
				// try again with jline disabled
				try {
					jlineEnabled = Boolean.FALSE;
					System.setProperty("user.language",  "en");
					reader = new ConsoleReader(
						System.in,
						getOriginalOut()
					);
				} catch (IOException e) {
					log().trace(e);
				}
			}
			if (jlineEnabled == null) {
				jlineEnabled = Boolean.TRUE;
			}
			try {
				reader.setBellEnabled(false);
				final FileHistory history =
					new FileHistory(
						new File("./history.txt")
					);
				history.setMaxSize(200);
				reader.setHistory(history);
				reader.setHistoryEnabled(true);
				reader.setExpandEvents(true);
			} catch (Exception e) {
				log().trace(e);
			}
		}
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	@Override
	public void finalize() {
		if (this.isRunning()) {
			if (!this.isStopping()) {
				this.Stop();
			}
		}
	}



	@Override
	public void Start() {
		if (xVars.debug()) {
			log().finest("Start jlineConsole");
		}
		if (this.isStopping()) throw new RuntimeException("Console already stopped");
		if (this.isRunning())  throw new RuntimeException("Console already running");
		if (!this.starting.compareAndSet(false, true)) {
			return;
		}
		// capture std out
		if (OVERRIDE_STDIO) {
			xVars.setOriginalOut(System.out);
			System.setOut(
				new PrintStream(
					new OutputStream() {
						private final StringBuilder buf = new StringBuilder();
						@Override
						public void write(final int b) throws IOException {
							if (b == '\r') return;
							if (b == '\n') {
								log().stdout(this.buf.toString());
								this.buf.setLength(0);
								return;
							}
							this.buf.append( Character.toChars(b) );
						}
					}
						)
			);
			// capture std err
			xVars.setOriginalErr(System.err);
			System.setErr(
				new PrintStream(
					new OutputStream() {
						private final StringBuilder buf = new StringBuilder();
						@Override
						public void write(final int b) throws IOException {
							if (b == '\r') return;
							if (b == '\n') {
								log().stderr(this.buf.toString());
								this.buf.setLength(0);
								return;
							}
							this.buf.append( Character.toChars(b) );
						}
					}
				)
			);
		}
		if (this.isStopping()) {
			throw new RuntimeException("Console already stopped");
		}
		// input listener thread
		if (this.thread == null) {
			this.thread = new Thread(this);
			this.thread.setDaemon(true);
		}
		// start console input thread
		this.thread.start();
		if (this.isStopping()) {
			throw new RuntimeException("Console already stopped");
		}
	}
	@Override
	public void Stop() {
		this.stopping = true;
		ThreadUtils.Sleep(20L);
		if (this.running.get()) {
			if (!this.running.compareAndSet(true, false)) {
				return;
			}
		}
		synchronized(printLock) {
			// restore out
			System.setOut(xVars.getOriginalOut());
			xVars.setOriginalOut(null);
			// restore err
			System.setErr(xVars.getOriginalErr());
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
		// save command history
		if (reader != null) {
			try {
				final History history = reader.getHistory();
				if (history != null) {
					if (history instanceof FileHistory) {
						((FileHistory) history).flush();
					}
				}
			} catch (Exception e) {
				log().trace(e);
			}
		}
		this.setPrompt("");
	}



	// input listener loop
	@Override
	public void run() {
		if (!this.running.compareAndSet(false, true)) {
			return;
		}
		final PrintStream out = getOriginalOut();
		while (!this.isStopping()) {
			if (this.thread != null) {
				if (this.thread.isInterrupted()) {
					break;
				}
			}
			String line = null;
			try {
				out.print('\r');
				line = getReader()
					.readLine(
						this.getPrompt()
					);
				out.flush();
			} catch (Exception e) {
				log().trace(e);
				break;
			}
			if (this.isStopping())
				break;
			if (Utils.notEmpty(line)) {
				// pass event to command handler
				final xCommandHandler handler = this.handler;
				if (handler == null) {
					log().severe("Command handler not set!");
				} else {
					final xCommandEvent event = new xCommandEvent(line);
					handler.trigger(event);
					if (!event.isHandled()) {
						log().publish("Unknown command: "+event.getArg(0));
					}
				}
			}
		}
		this.stopping = true;
		this.running.set(false);
		this.setPrompt("");
		out.println();
		out.flush();
		reader = null;
		try {
			AnsiConsole.systemUninstall();
		} catch (Exception ignore) {}
		this.Stop();
	}
	@Override
	public boolean isRunning() {
		if (this.isStopping())
			return false;
		return this.running.get();
	}
	public boolean isStopping() {
		return this.stopping;
	}



	protected static PrintStream getOriginalOut() {
		return xLog.getOriginalOut();
	}
	protected static PrintStream getOriginalErr() {
		return xLog.getOriginalErr();
	}



	// clear screen
	@Override
	public void clear() {
		AnsiConsole.out.println(
			Ansi.ansi()
				.eraseScreen()
				.cursor(0, 0)
		);
		flush();
	}
	public void clearLine() {
		final PrintStream out = getOriginalOut();
		final String prompt = this.getPrompt();
		synchronized(printLock) {
			if (Utils.isEmpty(prompt)) {
				out.print('\r');
			} else {
				out.print(
					(new StringBuilder())
						.append('\r')
						.append(StringUtils.repeat(
								prompt.length() + 2,
								' '
						))
						.append('\r')
				);
			}
			out.flush();
		}
	}
	// flush buffer
	@Override
	public void flush() {
		try {
			synchronized(printLock) {
				getOriginalOut()
					.flush();
			}
		} catch (Exception ignore) {}
	}



	// print then restore prompt
	@Override
	public void print(final String msg) {
		// render jAnsi
		final StringBuilder str = new StringBuilder();
		str.append(renderAnsi(msg));
		// be sure to overwrite prompt
		{
			final int minLength = this.getPrompt().length() + 2;
			if (str.length() < minLength) {
				str.append(
					StringUtils.repeat(
						minLength - str.length(),
						" "
					)
				);
			}
		}
		str.append("\r\n");
		synchronized(printLock) {
			// print
			AnsiConsole.out().print(
				"\r"+
				str.toString()
			);
			// draw command prompt
			this.drawPrompt();
			flush();
		}
	}



	// draw command prompt
	@Override
	public void drawPrompt() {
		if (reader == null)
			return;
		try {
			synchronized(printLock) {
				this.clearLine();
				reader.drawLine();
				reader.flush();
			}
		} catch (IOException ignore) {}
	}
	// render jAnsi
	public static String renderAnsi(final String msg) {
		return
			Ansi.ansi()
				.render(msg)
				.toString();
	}



	@Override
	public void setPrompt(final String prompt) {
		this.prompt = prompt;
		reader.setPrompt(this.getPrompt());
		this.drawPrompt();
	}
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
	public String getBellStr() {
		return this.bellStyle
				.toString();
	}
	@Override
	public BellType getBell() {
		return this.bellStyle;
	}
	@Override
	public void setBell(final String bellStr) {
		this.bellStyle = BellType.byString(bellStr);
	}



	@Override
	public void setCommandHandler(final xCommandHandler handler) {
		this.handler = handler;
	}



	// logger
	private volatile SoftReference<xLog> _log = null;
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null) {
				return log;
			}
		}
		final xLog log = xLog.getRoot();
		this._log = new SoftReference<xLog>(log);
		return log;
	}



}
