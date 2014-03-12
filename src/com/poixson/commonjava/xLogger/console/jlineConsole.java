package com.poixson.commonjava.xLogger.console;

import java.io.IOException;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.app.xApp;
import com.poixson.commonjava.xLogger.xConsole;
import com.poixson.commonjava.xLogger.xLog;


public class jlineConsole implements xConsole {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public static final String DEFAULT_PROMPT = " #>";

	private static final Object lock = new Object();
	private static volatile ConsoleReader reader = null;
	private static volatile Boolean jlineEnabled = null;

	private volatile String prompt = null;

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
				jlineEnabled = false;
				System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
			}
			try {
				reader = new ConsoleReader(System.in, System.out);
			} catch (IOException ignore) {
				// try again with jline disabled
				try {
					jlineEnabled = false;
					System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
					System.setProperty("user.language", "en");
					reader = new ConsoleReader(System.in, System.out);
				} catch (IOException e) {
					log().trace(e);
				}
			}
			if(jlineEnabled == null)
				jlineEnabled = true;
			try {
				reader.setBellEnabled(false);
//				FileHistory history = new FileHistory(new File("history.txt"));
//				history.setMaxSize(200);
//				reader.setHistory(history);
//				reader.setHistoryEnabled(true);
				reader.setExpandEvents(false);
			} catch (Exception ignore) {}
		}
	}


	@Override
	public void start() {
		log().finest("Start jlineConsole");
		if(running || stopping) return;
		synchronized(lock) {
			if(thread == null)
				thread = new Thread(this);
			if(!running)
				thread.start();
		}
	}
	@Override
	public void stop() {
		stopping = true;
		if(running && thread != null)
			thread.notify();
		try {
			System.in.close();
		} catch (Exception ignore) {}
	}
	@Override
	public void shutdown() {
		synchronized(lock) {
			// stop console input
			this.stop();
			if(reader != null) {
				// save command history
				try {
					((FileHistory) reader.getHistory()).flush();
				} catch (Exception ignore) {}
				// close reader
				try {
					reader.flush();
				} catch (Exception ignore) {}
				try {
					reader.shutdown();
				} catch (Exception ignore) {}
				reader = null;
			}
			try {
				AnsiConsole.systemUninstall();
			} catch (Exception ignore) {}
		}
	}


	// input listener loop
	@Override
	public void run() {
		synchronized(lock) {
			if(running) return;
			running = true;
		}
		while(!stopping) {
			if(thread != null && thread.isInterrupted()) break;
			String line = null;
			try {
				System.out.print('\r');
				line = reader.readLine(getPrompt());
				flush();
			} catch (IOException e) {
				e.printStackTrace();
				utilsThread.Sleep(200);
				continue;
			} catch (Exception e) {
				log().trace(e);
				break;
			}
			if(!utils.isEmpty(line))
				xApp.get().processCommand(line);
		}
		running = false;
		System.out.println();
		System.out.println();
		flush();
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
			System.out.flush();
		} catch (Exception ignore) {}
	}
	// print then restore prompt
	@Override
	public void print(String msg) {
		// render jAnsi
		msg = renderAnsi(msg);
		// be sure to overwrite prompt
		{
			final int minLength = getPrompt().length() + 2;
			if(msg.length() < minLength)
				msg += utilsString.repeat(minLength - msg.length(), " ");
		}
		// print
		System.out.print("\r"+msg+"\r\n");
		// draw prompt
		drawPrompt();
		flush();
	}
	// draw prompt
	@Override
	public void drawPrompt() {
		if(reader == null) return;
		try {
			reader.drawLine();
			reader.flush();
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
		if(prompt == null || prompt.isEmpty())
			return DEFAULT_PROMPT;
		return prompt;
	}


	// logger
	public static xLog log() {
		return xLog.getRoot();
	}


}
