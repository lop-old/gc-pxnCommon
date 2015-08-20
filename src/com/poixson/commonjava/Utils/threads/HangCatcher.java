package com.poixson.commonjava.Utils.threads;

import java.io.PrintStream;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import com.poixson.commonjava.Utils.CoolDown;
import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xStartable;
import com.poixson.commonjava.Utils.xTime;


public class HangCatcher implements xStartable {

	static final xTime TIMEOUT = xTime.get("10s");
	static final xTime SLEEP   = xTime.get("100n");

	protected volatile Thread thread = null;
	protected volatile boolean cancel = false;

	protected final CoolDown cool;



	public static HangCatcher get() {
		final HangCatcher catcher = new HangCatcher();
		Keeper.add(catcher);
		return catcher;
	}
	public HangCatcher() {
		this.cool = CoolDown.get(TIMEOUT);
	}



	@Override
	public void Start() {
		if(this.cancel)
			return;
		this.resetTimeout();
		if(this.thread == null) {
			this.thread = new Thread(this);
			this.thread.setName("HangCatcher");
			this.thread.setDaemon(true);
			this.thread.start();
		}
	}
	@Override
	public void Stop() {
		this.cancel = true;
		Keeper.remove(this);
	}



	@Override
	public void run() {
		while(!this.cancel) {
			if(this.cool.runAgain()) {
				final PrintStream out = AnsiConsole.out;
				out.println();
				out.println(Ansi.ansi().a(" ").fg(Ansi.Color.RED).a("************************").reset().a(" "));
				out.println(Ansi.ansi().a(" ").fg(Ansi.Color.RED).a("*  Shutdown has hung!  *").reset().a(" "));
				out.println(Ansi.ansi().a(" ").fg(Ansi.Color.RED).a("************************").reset().a(" "));
				out.println();
				utilsThread.displayStillRunning();
				System.exit(1);
			}
			utilsThread.Sleep(SLEEP);
		}
		this.cancel = true;
		this.thread = null;
		Keeper.remove(this);
	}
	@Override
	public boolean isRunning() {
		return (this.thread != null);
	}
	public void resetTimeout() {
		this.cool.resetRun();
	}



}
