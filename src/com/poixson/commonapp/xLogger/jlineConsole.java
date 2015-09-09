package com.poixson.commonapp.xLogger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;
import jline.console.history.History;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.xEvents.xHandler;
import com.poixson.commonjava.xLogger.xConsole;
import com.poixson.commonjava.xLogger.xLog;
import com.poixson.commonjava.xLogger.commands.xCommandEvent;


public class jlineConsole implements xConsole {

	public static final String DEFAULT_PROMPT = " #>";

	private static final Object lock      = new Object();
	private static final Object printLock = new Object();
	private static volatile ConsoleReader reader = null;
	private static volatile Boolean jlineEnabled = null;

	private volatile String   prompt = null;
	private volatile xHandler handler = null;

	private static volatile PrintStream originalOut = null;
	private static volatile PrintStream originalErr = null;

	// console input thread
	private volatile Thread  thread   = null;
	private volatile boolean running  = false;
	private volatile boolean stopping = false;



	// new instance
	public jlineConsole() {
		// console reader
		if(reader == null) {
			jlineEnabled = null;
			// no console
			if(System.console() == null) {
				jlineEnabled = Boolean.FALSE;
				System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
			}
			try {
				reader = new ConsoleReader(System.in, getOriginalOut());
			} catch (IOException ignore) {
				// try again with jline disabled
				try {
					jlineEnabled = Boolean.FALSE;
					System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
					System.setProperty("user.language",  "en");
					reader = new ConsoleReader(System.in, getOriginalOut());
				} catch (IOException e) {
					log().trace(e);
				}
			}
			if(jlineEnabled == null)
				jlineEnabled = Boolean.TRUE;
			try {
				reader.setBellEnabled(false);
				FileHistory history = new FileHistory(new File("./history.txt"));
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
		if(this.running && !this.stopping)
			this.Stop();
	}



	@Override
	public void Start() {
		log().finest("Start jlineConsole");
		if(this.stopping) throw new RuntimeException("Console already stopped");
		if(this.running)  throw new RuntimeException("Console already running");
		synchronized(lock) {
			// capture std out
			originalOut = System.out;
			System.setOut(
				new PrintStream(
					new OutputStream() {
						private final StringBuilder buf = new StringBuilder();
						@Override
						public void write(final int b) throws IOException {
							if(b == '\r') return;
							if(b == '\n') {
								xLog.getRoot()
									.stdout(this.buf.toString());
								this.buf.setLength(0);
								return;
							}
							this.buf.append( Character.toChars(b) );
						}
					}
				)
			);
			// capture std err
			originalErr = System.err;
			System.setErr(
				new PrintStream(
					new OutputStream() {
						private final StringBuilder buf = new StringBuilder();
						@Override
						public void write(final int b) throws IOException {
							if(b == '\r') return;
							if(b == '\n') {
								xLog.getRoot()
									.stderr(this.buf.toString());
								this.buf.setLength(0);
								return;
							}
							this.buf.append( Character.toChars(b) );
						}
					}
				)
			);
			// input listener thread
			if(this.thread == null) {
				this.thread = new Thread(this);
				this.thread.setDaemon(true);
			}
			if(!this.running)
				this.thread.start();
		}
	}
	@Override
	public void Stop() {
		this.stopping = true;
		synchronized(lock) {
			// restore original out/err
			if(originalOut != null)
				System.setOut(originalOut);
			originalOut = null;
			if(originalErr != null)
				System.setErr(originalErr);
			originalErr = null;
			// stop console input thread
			if(this.running && this.thread != null) {
				try {
					this.thread.interrupt();
				} catch (Exception ignore) {}
				try {
					this.thread.notifyAll();
				} catch (Exception ignore) {}
			}
			// save command history
			if(reader != null) {
				try {
					final History history = reader.getHistory();
					if(history != null && history instanceof FileHistory)
						((FileHistory) history).flush();
				} catch (Exception e) {
					log().trace(e);
				}
			}
			// close input
			utils.safeClose(System.in);
			try {
				AnsiConsole.systemUninstall();
			} catch (Exception ignore) {}
			this.setPrompt("");
		}
	}
	@Override
	public boolean isRunning() {
		if(this.stopping)
			return false;
		return this.running;
	}



	// input listener loop
	@Override
	public void run() {
		synchronized(lock) {
			if(this.running) return;
			this.running = true;
		}
		while(!this.stopping) {
			if(this.thread != null && this.thread.isInterrupted()) break;
			String line = null;
			try {
				getOriginalOut().print('\r');
				line = reader.readLine(this.getPrompt());
				flush();
			} catch (IOException e) {
				if("Stream closed".equals(e.getMessage()))
					break;
				log().trace(e);
				break;
			} catch (Exception e) {
				log().trace(e);
				break;
			}
			if(this.stopping) break;
			if(utils.notEmpty(line)) {
				// pass event to command handler
				final xHandler hand = this.handler;
				if(hand == null) {
					log().severe("Command handler not set!");
				} else {
					final xCommandEvent event = new xCommandEvent(line);
					hand.triggerNow(event);
					if(!event.isHandled())
						log().publish("Unknown command: "+event.arg(0));
				}
			}
		}
		this.stopping = true;
		this.running = false;
		this.setPrompt("");
		getOriginalOut().println();
		flush();
		reader.shutdown();
		reader = null;
	}



	protected static PrintStream getOriginalOut() {
		return originalOut == null ? System.out : originalOut;
	}
	protected static PrintStream getOriginalErr() {
		return originalErr == null ? System.err : originalErr;
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
			if(utils.isEmpty(prompt))
				out.print('\r');
			else
				out.print('\r'+utilsString.repeat(prompt.length()+2, ' ')+'\r');
			out.flush();
		}
	}
	// flush buffer
	@Override
	public void flush() {
		try {
			synchronized(printLock) {
				reader.flush();
				//getOriginalOut().flush();
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
			if(str.length() < minLength)
				str.append(utilsString.repeat(minLength - str.length(), " "));
		}
		synchronized(printLock) {
			// print
			AnsiConsole.out().print(
					"\r"+str.append("\r\n").toString()
			);
			// draw command prompt
			this.drawPrompt();
			flush();
		}
	}
	// draw command prompt
	@Override
	public void drawPrompt() {
		if(reader == null) return;
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
		return Ansi.ansi().render(msg).toString();
	}



	// prompt string
	@Override
	public void setPrompt(final String prompt) {
		this.prompt = prompt;
		reader.setPrompt(this.getPrompt());
		this.drawPrompt();
	}
	@Override
	public String getPrompt() {
		final String prompt = this.prompt;
		return utils.isEmpty(prompt)
				? DEFAULT_PROMPT
				: prompt;
	}



	@Override
	public void setCommandHandler(final xHandler handler) {
		this.handler = handler;
	}



	// logger
	public static xLog log() {
		return xLog.getRoot();
	}



}
