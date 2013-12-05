package com.poixson.commonjava.xLogger.console;

import java.io.IOException;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.xLogger.xConsole;
import com.poixson.commonjava.xLogger.xLog;


public class jlineConsole implements xConsole {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	protected static final Object lock = new Object();
	protected static volatile xConsole console = null;
	protected static volatile ConsoleReader reader = null;
	protected static volatile Boolean jlineEnabled = null;

	private volatile String prompt = null;

	// console input thread
	protected volatile Thread thread = null;
	private volatile boolean running = false;
	private volatile boolean stopping = false;


	// get console instance
	public static xConsole get() {
		if(console == null) {
			synchronized(lock) {
				if(console == null)
					console = new xConsole();
			}
		}
		return console;
	}
	public static void set(xConsole console) {
		synchronized(lock) {
			xConsole.console = console;
		}
	}
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


	public void start() {
		if(running || stopping) return;
		synchronized(lock) {
			if(thread == null)
				thread = new Thread(this);
			if(!running)
				thread.start();
		}
	}
	public void stop() {
		stopping = true;
		if(running && thread != null)
			thread.notify();
		try {
			System.in.close();
		} catch (Exception ignore) {}
	}
	public void shutdown() {
		synchronized(lock) {
			// stop console input
			if(console != null)
				console.stop();
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
			if(running)
				return;
			running = true;
		}
		while(!stopping) {
			if(thread != null && thread.isInterrupted()) break;
			String line = null;
			try {
				System.out.print('\r');
				line = reader.readLine(getPrompt());
			} catch (IOException e) {
				e.printStackTrace();
				utilsThread.Sleep(200);
				continue;
			} catch (Exception e) {
				log().trace(e);
				break;
			}
			if(line == null) continue;
			doCommand(line);
		}
		running = false;
		System.out.println();
		System.out.println();
	}


	public void doCommand(String line) {
//TODO: send line to main thread
System.out.println("***>"+line+"<***");
	}


	// clear screen
	public void clear() {
		AnsiConsole.out.println(
			Ansi.ansi()
				.eraseScreen()
				.cursor(0, 0)
		);
	}
	// flush buffer
	public void flush() {
		try {
			reader.flush();
		} catch (Exception ignore) {}
	}
	// print then restore prompt
	public void print(String msg) {
		// render jAnsi
		msg = renderAnsi(msg);
		// be sure to overwrite prompt
		if(msg.length() < getPrompt().length() + 2)
			msg += "    ";
		// print
		System.out.print("\r"+msg+"\r\n");
		// redraw prompt
		redraw();
	}
	// redraw prompt
	public void redraw() {
		if(reader == null) return;
		try {
			reader.drawLine();
		} catch (IOException ignore) {}
	}
	// render jAnsi
	public static String renderAnsi(String msg) {
		return Ansi.ansi().render(msg).toString();
	}


	// prompt string
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}
	public String getPrompt() {
		if(prompt == null || prompt.isEmpty())
			return ">";
		return prompt;
	}


	// logger
	public static xLog log() {
		return xVars.getLog();
	}


}
