package com.poixson.commonapp.xLogger;

import java.io.File;
import java.io.IOException;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;
import jline.console.history.History;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import com.poixson.commonjava.EventListener.xHandler;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.xLogger.xConsole;
import com.poixson.commonjava.xLogger.xLog;
import com.poixson.commonjava.xLogger.handlers.xCommandEvent;


public class jlineConsole implements xConsole {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public static final String DEFAULT_PROMPT = " #>";

	private static final Object lock = new Object();
	private static final Object printLock = new Object();
	private static volatile ConsoleReader reader = null;
	private static volatile Boolean jlineEnabled = null;

	private volatile String prompt = null;
	private volatile xHandler handler = null;

	// console input thread
	private volatile Thread thread = null;
	private volatile boolean running = false;
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
				reader = new ConsoleReader(System.in, System.out);
			} catch (IOException ignore) {
				// try again with jline disabled
				try {
					jlineEnabled = Boolean.FALSE;
					System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
					System.setProperty("user.language", "en");
					reader = new ConsoleReader(System.in, System.out);
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
	public void start() {
		log().finest("Start jlineConsole");
		if(this.running || this.stopping) return;
		synchronized(lock) {
			if(this.thread == null) {
				this.thread = new Thread(this);
				this.thread.setDaemon(true);
			}
			if(!this.running)
				this.thread.start();
		}
	}
	@Override
	public void stop() {
		this.stopping = true;
		synchronized(lock) {
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
			try {
				System.in.close();
			} catch (IOException ignore) {}
			try {
				AnsiConsole.systemUninstall();
			} catch (Exception ignore) {}
			System.out.print("\r  "+utilsString.repeat(DEFAULT_PROMPT.length(), " "));
			System.out.println();
		}
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
				System.out.print('\r');
				line = reader.readLine(getPrompt());
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
		this.running = false;
		System.out.println();
		System.out.println();
		flush();
		reader.shutdown();
		reader = null;
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
	// flush buffer
	@Override
	public void flush() {
		try {
			synchronized(printLock) {
				reader.flush();
				//System.out.flush();
			}
		} catch (Exception ignore) {}
	}
	// print then restore prompt
	@Override
	public void print(final String msg) {
		// render jAnsi
		String str = renderAnsi(msg);
		// be sure to overwrite prompt
		{
			final int minLength = getPrompt().length() + 2;
			if(str.length() < minLength)
				str += utilsString.repeat(minLength - str.length(), " ");
		}
		synchronized(printLock) {
			// print
			System.out.print("\r"+str+"\r\n");
			// draw command prompt
			drawPrompt();
			flush();
		}
	}
	// draw command prompt
	@Override
	public void drawPrompt() {
		if(reader == null) return;
		try {
			synchronized(printLock) {
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
	}
	@Override
	public String getPrompt() {
		if(this.prompt == null || this.prompt.isEmpty())
			return DEFAULT_PROMPT;
		return this.prompt;
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
