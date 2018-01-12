package com.poixson.tools.threadpool;

import java.util.Iterator;

import com.poixson.app.Failure;
import com.poixson.logger.xLog;
import com.poixson.tools.HangCatcher;
import com.poixson.tools.Keeper;
import com.poixson.tools.xTime;
import com.poixson.tools.threadpool.types.xThreadPool_Main;
import com.poixson.utils.ThreadUtils;


public class ShutdownTask implements Runnable {

	protected final xThreadPool pool;

	protected final HangCatcher hangCatch;

	// hang timeout
	public final long hangTimeout = xTime.getNew("10s").getMS();
	// sleep interval
	public final long hangSleep = 100L;



	public ShutdownTask() {
		this.pool = xThreadPool_Main.get();
		this.hangCatch =
			new HangCatcher(
				this.hangTimeout,
				this.hangSleep,
				"Shutdown",
				new Runnable() {
					@Override
					public void run() {
//TODO:
						System.out.println();
						System.out.println(" ************************ ");
						System.out.println(" *  Shutdown has hung!  * ");
						System.out.println(" ************************ ");
						System.out.println();
//						final PrintStream out = AnsiConsole.out;
//						out.println();
//						out.println(Ansi.ansi().a(" ").fg(Ansi.Color.RED).a("************************").reset().a(" "));
//						out.println(Ansi.ansi().a(" ").fg(Ansi.Color.RED).a("*  Shutdown has hung!  *").reset().a(" "));
//						out.println(Ansi.ansi().a(" ").fg(Ansi.Color.RED).a("************************").reset().a(" "));
//						out.println();
						ThreadUtils.DisplayStillRunning();
						System.exit(1);
					}
				}
			);
		this.hangCatch.start();
	}



	@Override
	public void run() {
		int totalCount = 0;
		OUTER_LOOP:
		while (true) {
			int count = 0;
			final Iterator<Runnable> it =
				xThreadPool.shutdownHooks.iterator();
			//INNER_LOOP:
			while (it.hasNext()) {
				final Runnable run = it.next();
				this.pool.runTaskLazy(run);
				count++;
				this.resetTimeout();
			}
			if (count == 0)
				break OUTER_LOOP;
			totalCount += count;
			xLog.getRoot().fine("Running {} shutdown hooks..", count);
			ThreadUtils.Sleep(20L);
		}
		// queue another shutdown task (to wait for things to finish)
		if (totalCount > 0) {
			// run this again
			this.pool.runTaskLazy(
				new ShutdownTask()
			);
		// finished running hooks
		} else {
			final Thread kill = new KillTask();
			Keeper.removeAll();
			Keeper.add(kill);
			kill.start();
		}
	}



	protected static class KillTask extends Thread {

		@Override
		public void run() {
			// wait for thread pools to stop
			ThreadUtils.Sleep(10L);
			xThreadPool.StopAllAndWait();
			ThreadUtils.Sleep(50L);
			// end
			System.exit(
				Failure.hasFailed()
				? 1
				: 0
			);
		}

	}



	public void resetTimeout() {
		this.hangCatch.resetTimeout();
	}



}
