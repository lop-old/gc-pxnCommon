package com.poixson.utils;

import java.io.PrintStream;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;


public class HangCatcher implements xStartable {

	private static final String DEFAULT_TIMEOUT = "10s";
	private static final String DEFAULT_SLEEP   = "100n";

	protected volatile Thread thread = null;
	protected volatile boolean cancel = false;

	protected final CoolDown cool;
	protected final xTime sleep;



	public HangCatcher() {
		this(
			DEFAULT_TIMEOUT,
			DEFAULT_SLEEP
		);
	}
	public HangCatcher(final String timeoutStr, final String sleepStr) {
		this(
			xTime.get(
				Utils.isEmpty(timeoutStr)
				? DEFAULT_TIMEOUT
				: timeoutStr
			),
			xTime.get(
				Utils.isEmpty(sleepStr)
				? DEFAULT_SLEEP
				: sleepStr
			)
		);
	}
	public HangCatcher(final xTime timeout, final xTime sleep) {
		this.cool = CoolDown.get(
			timeout == null
			? xTime.get(DEFAULT_TIMEOUT)
			: timeout
		);
		this.sleep = (
			sleep == null
			? xTime.get(DEFAULT_SLEEP)
			: sleep
		);
	}



	@Override
	public void Start() {
		if (this.cancel) return;
		this.resetTimeout();
		if (this.thread == null) {
			this.thread = new Thread(this);
			this.thread.setName("HangCatcher");
			this.thread.setDaemon(true);
			this.thread.start();
		}
	}
	@Override
	public void Stop() {
		this.cancel = true;
	}



	@Override
	public void run() {
		while (!this.cancel) {
			if (this.cool.runAgain()) {
				final PrintStream out = AnsiConsole.out;
				out.println();
				out.println(Ansi.ansi().a(" ").fg(Ansi.Color.RED).a("************************").reset().a(" "));
				out.println(Ansi.ansi().a(" ").fg(Ansi.Color.RED).a("*  Shutdown has hung!  *").reset().a(" "));
				out.println(Ansi.ansi().a(" ").fg(Ansi.Color.RED).a("************************").reset().a(" "));
				out.println();
				ThreadUtils.displayStillRunning();
				System.exit(1);
			}
			ThreadUtils.Sleep(this.sleep);
		}
		this.cancel = true;
		this.thread = null;
	}
	@Override
	public boolean isRunning() {
		return (this.thread != null);
	}
	public void resetTimeout() {
		this.cool.resetRun();
	}



}
