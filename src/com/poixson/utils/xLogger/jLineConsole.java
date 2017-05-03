package com.poixson.utils.xLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.jline.reader.EndOfFileException;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import com.poixson.app.xApp;
import com.poixson.utils.StringUtils;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.xVars;


public class jlineConsole implements xConsole {
	protected static final String  DEFAULT_PROMPT = " #>";
	protected static final boolean OVERRIDE_STDIO = false;

	private static final Object printLock = new Object();
	private static volatile Terminal   term   = null;
	private static volatile LineReader reader = null;

	private volatile String prompt = null;
	private volatile xCommandHandler handler = null;

	// console input thread
	private volatile Thread  thread = null;
	private volatile boolean stopping = false;
	private final AtomicBoolean running  = new AtomicBoolean(false);
	private final AtomicBoolean starting = new AtomicBoolean(false);



	public jlineConsole() {
		InitReader();
	}
	private void InitReader() {
		if (reader != null) return;
		// get console reader
		try {
			term = TerminalBuilder.builder()
					.streams(
						xLog.getOriginalIn(),
						xLog.getOriginalOut()
					)
					.system(true)
					.build();
			reader = LineReaderBuilder.builder()
					.terminal(term)
					.appName(xApp.get().getTitle())
//TODO: .completer(completer)
					.build();
		} catch (IOException e) {
			log().trace(e);
		}
		try {
			reader.setVariable(
				LineReader.HISTORY_FILE,
				xVars.getJLineHistoryFile()
			);
			reader.setVariable(
				LineReader.HISTORY_SIZE,
				xVars.getJLineHistorySize()
			);
		} catch (Exception e) {
			log().trace(e);
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



	public static LineReader getReader() {
		return reader;
	}
	public static Terminal getTerm() {
		return term;
	}



	protected static PrintStream getOriginalOut() {
		return xLog.getOriginalOut();
	}
	protected static PrintStream getOriginalErr() {
		return xLog.getOriginalErr();
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
		final LineReader reader = getReader();
		if (reader != null) {
			try {
				final History history = reader.getHistory();
				if (history != null) {
					history.save();
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
			} catch (EndOfFileException e) {
				if ("Stream closed".equals(e.getMessage()))
					break;
				log().trace(e);
				break;
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



	// clear screen
	@Override
	public void clear() {
		synchronized(printLock) {
			AnsiConsole.out.println(
				Ansi.ansi()
					.eraseScreen()
					.cursor(0, 0)
			);
			flush();
		}
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



	// command prompt
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
		this.drawPrompt();
	}
	@Override
	public void drawPrompt() {
		final LineReader reader = getReader();
		if (reader == null) return;
		synchronized(printLock) {
			this.clearLine();
			if (Utils.notEmpty(this.prompt)) {
				final PrintStream out = getOriginalOut();
				out.print(this.prompt);
				out.flush();
			}
		}
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
