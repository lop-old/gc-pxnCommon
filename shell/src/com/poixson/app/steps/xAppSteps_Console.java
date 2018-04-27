package com.poixson.app.steps;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;

import com.poixson.app.Failure;
import com.poixson.app.xApp;
import com.poixson.app.xAppStep;
import com.poixson.app.xAppStep.StepType;
import com.poixson.app.xVars;
import com.poixson.exceptions.IORuntimeException;
import com.poixson.logger.xConsole;
import com.poixson.logger.xLogRoot;
import com.poixson.tools.Keeper;
import com.poixson.utils.FileUtils;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.Utils;


public class xAppSteps_Console implements xConsole {
	protected static final String HISTORY_FILE = "history.txt";
	protected static final int    HISTORY_SIZE = 1000;
	protected static final String DEFAULT_PROMPT = " #>";
	protected static final boolean BELL_ENABLED = true;
	protected static final String THREAD_NAME = "Console-Input";

	protected static final AtomicReference<Terminal>   terminal = new AtomicReference<Terminal>(null);
	protected static final AtomicReference<LineReader> reader   = new AtomicReference<LineReader>(null);
	protected static final AtomicReference<History>    history  = new AtomicReference<History>(null);

	protected final AtomicReference<Thread> thread = new AtomicReference<Thread>(null);
	protected final AtomicBoolean running = new AtomicBoolean(false);
	protected final AtomicBoolean reading = new AtomicBoolean(false);
	protected volatile boolean stopping = false;



	public xAppSteps_Console() {
		Keeper.add(this);
	}
	public void unload() {
		Keeper.remove(this);
	}



	// terminal
	public static Terminal getTerminal() {
		{
			final Terminal term = terminal.get();
			if (term != null)
				return term;
		}
		synchronized (terminal) {
			final Terminal term;
			try {
				term = TerminalBuilder.builder()
					.system(true)
					.streams(
						xVars.getOriginalIn(),
						xVars.getOriginalOut()
					)
					.build();
			} catch (IOException e) {
				throw new IORuntimeException(e);
			}
			if ( ! terminal.compareAndSet(null, term) )
				terminal.get();
			AnsiConsole.systemInstall();
			return term;
		}
	}
	// line reader
	public static LineReader getReader() {
		{
			final LineReader read = reader.get();
			if (read != null)
				return read;
		}
		synchronized (reader) {
			final Terminal term = getTerminal();
			final LineReader read =
				LineReaderBuilder.builder()
					.terminal(term)
					.build();
			if ( ! reader.compareAndSet(null, read) )
				return reader.get();
			read.setVariable(
				LineReader.BELL_STYLE,
				BELL_ENABLED ? "audible" : "visible"
			);
			getHistory();
			return read;
		}
	}
	// history
	public static History getHistory() {
		{
			final History hist = history.get();
			if (hist != null)
				return hist;
		}
		{
			final String historyFile =
				FileUtils.MergePaths(",", HISTORY_FILE);
			final LineReader read = getReader();
			read.setVariable(LineReader.HISTORY_FILE, historyFile);
			read.setVariable(LineReader.HISTORY_SIZE, HISTORY_SIZE);
			final History hist = new DefaultHistory(read);
			if ( ! history.compareAndSet(null, hist) )
				return history.get();
			return hist;
		}
	}



	// ------------------------------------------------------------------------------- //
	// console input



	@Override
	public void start() {
		if (this.running.get())
			return;
		this.stopping = false;
		final Thread thread = new Thread(this);
		thread.setName(THREAD_NAME);
		thread.setDaemon(true);
		thread.start();
	}
	@Override
	public void stop() {
		this.stopping = true;
		final Thread thread = this.thread.get();
		if (thread != null) {
			try {
				thread.interrupt();
			} catch (Exception ignore) {}
			try {
				thread.notifyAll();
			} catch (Exception ignore) {}
		}
	}



	@Override
	public void run() {
		if (this.stopping) return;
		if ( ! this.running.compareAndSet(false, true) )
			throw new RuntimeException("Console thread already running!");
		xVars.setConsole(this);
		if (this.thread.get() == null) {
			this.thread.compareAndSet(null, Thread.currentThread());
		}
		final Thread thread = this.thread.get();
		thread.setName(THREAD_NAME);
		if (xVars.isDebug()) {
			xLogRoot.get()
				.detail("Starting console input thread..");
		}
		final LineReader read = getReader();
		READER_LOOP:
		while (true) {
			if (this.stopping)          break READER_LOOP;
			if (thread.isInterrupted()) break READER_LOOP;
			final String line;
			try {
				synchronized (this.reading) {
					this.reading.set(true);
				}
				// read console input
				line = read.readLine(
					this.getPrompt(),
					this.getMask()
				);
			} catch (UserInterruptException ignore) {
				break READER_LOOP;
			} catch (Exception e) {
				xLogRoot.get()
				.trace(e);
				try {
					Thread.sleep(100L);
				} catch (InterruptedException ignore) {
					break READER_LOOP;
				}
				continue READER_LOOP;
			} finally {
				this.reading.set(false);
			}
			// handle line
			if (Utils.notBlank(line)) {
			}
		} // end READER_LOOP
		this.stopping = true;
		xVars.setConsole(null);
		final PrintStream out = xVars.getOriginalOut();
		out.println();
		out.flush();
		this.running.set(false);
		this.thread.set(null);
		// save command history
		{
			final History hist = history.get();
			if (hist != null) {
				if (hist instanceof DefaultHistory) {
					try {
						((DefaultHistory) hist)
							.save();
					} catch (IOException e) {
						xLogRoot.get()
							.trace(e);
					}
				}
			}
		}
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



	// ------------------------------------------------------------------------------- //
	// startup steps



	@xAppStep( Type=StepType.STARTUP, Title="Console", StepValue=90 )
	public void __STARTUP_console(final xApp app) {
		// initialize console and enable colors
		if (System.console() != null) {
			if ( ! Utils.isJLineAvailable() ) {
				Failure.fail("jline library not found");
				return;
			}
		}
		// start reading console input
		this.start();
	}



	// ------------------------------------------------------------------------------- //
	// shutdown steps



	// stop console input
	@xAppStep( Type=StepType.SHUTDOWN, Title="Console", StepValue=30)
	public void __SHUTDOWN_console() {
		// stop reading console input
		this.stop();
	}



	// ------------------------------------------------------------------------------- //
	// publish to console



	@Override
	public void doPublish(final String line) {
		try {
			final PrintStream out = xVars.getOriginalOut();
			RETRY_LOOP:
			for (int i=0; i<5; i++) {
				if (this.stopping)
					break RETRY_LOOP;
				if (this.reading.get()) {
					try {
						final LineReader read = getReader();
						read.callWidget(LineReader.CLEAR);
						out.println(line);
						this.drawPrompt();
						break RETRY_LOOP;
					} catch (Exception ignore) {}
					if (this.stopping)
						break RETRY_LOOP;
					ThreadUtils.Sleep(20L);
				} else {
					out.println(
						(new StringBuilder())
							.append('\r')
							.append(line)
					);
					break RETRY_LOOP;
				}
			} // end RETRY_LOOP
		} catch (Exception ignore) {}
	}



	@Override
	public void doClearScreen() {
		try {
			RETRY_LOOP:
			for (int i=0; i<5; i++) {
				if (this.stopping)
					break RETRY_LOOP;
				if (this.reading.get()) {
					try {
						final Terminal   term = getTerminal();
						final LineReader read = getReader();
						read.callWidget(LineReader.CLEAR_SCREEN);
						term.writer().flush();
						break RETRY_LOOP;
					} catch (Exception ignore) {}
					if (this.stopping)
						break RETRY_LOOP;
					ThreadUtils.Sleep(20L);
				} else {
					final PrintStream out = xVars.getOriginalOut();
					out.print(
						Ansi.ansi()
							.eraseScreen()
							.cursor(0, 0)
							.toString()
					);
					out.flush();
					break RETRY_LOOP;
				}
			} // end RETRY_LOOP
		} catch (Exception ignore) {}
	}
	@Override
	public void doFlush() {
		try {
			getTerminal().flush();
		} catch (Exception ignore) {}
	}
	@Override
	public void doBeep() {
		try {
			if (this.reading.get()) {
				getReader().callWidget(LineReader.BEEP);
			} else {
				getTerminal().puts(Capability.bell);
			}
		} catch (Exception ignore) {}
	}



	// ------------------------------------------------------------------------------- //
	// settings



	// prompt
	public String getPrompt() {
		return DEFAULT_PROMPT;
	}
	@Override
	public void setPrompt(final String prompt) {
//TODO:
throw new UnsupportedOperationException("Unfinished");
	}

	@Override
	public void drawPrompt() {
		if (this.reading.get()) {
			final Terminal   term = getTerminal();
			final LineReader read = getReader();
			read.callWidget(LineReader.REDRAW_LINE);
			read.callWidget(LineReader.REDISPLAY);
			term.writer().flush();
		}
	}



	// mask
	public Character getMask() {
		return null;
	}
	@Override
	public void setMask(final Character mask) {
//TODO:
throw new UnsupportedOperationException("Unfinished");
	}



}
