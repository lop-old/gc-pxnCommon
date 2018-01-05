package com.poixson.tools;

import com.poixson.abstractions.xStartable;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.Utils;


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
			xTime.getNew(
				Utils.isEmpty(timeoutStr)
				? DEFAULT_TIMEOUT
				: timeoutStr
			),
			xTime.getNew(
				Utils.isEmpty(sleepStr)
				? DEFAULT_SLEEP
				: sleepStr
			)
		);
	}
	public HangCatcher(final xTime timeout, final xTime sleep) {
		this.cool = CoolDown.getNew(
			timeout == null
			? xTime.getNew(DEFAULT_TIMEOUT)
			: timeout
		);
		this.sleep = (
			sleep == null
			? xTime.getNew(DEFAULT_SLEEP)
			: sleep
		);
	}



	@Override
	public void start() {
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
	public void stop() {
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
				ThreadUtils.DisplayStillRunning();
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
	@Override
	public boolean isStopping() {
		return ! this.isRunning();
	}



	public void resetTimeout() {
		this.cool.resetRun();
	}



}
