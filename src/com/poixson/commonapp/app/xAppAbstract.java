package com.poixson.commonapp.app;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import com.poixson.commonapp.app.annotations.xAppStep;
import com.poixson.commonapp.app.annotations.xAppStep.StepType;
import com.poixson.commonapp.xLogger.jlineConsole;
import com.poixson.commonjava.Failure;
import com.poixson.commonjava.Failure.FailureAction;
import com.poixson.commonjava.xVars;
import com.poixson.commonjava.Utils.Keeper;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsProc;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.xClock;
import com.poixson.commonjava.Utils.xStartable;
import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.Utils.threads.xThreadPool;
import com.poixson.commonjava.xLogger.logHandlerConsole;
import com.poixson.commonjava.xLogger.xConsole;
import com.poixson.commonjava.xLogger.xLog;
import com.poixson.commonjava.xLogger.xNoConsole;
import com.poixson.commonjava.xLogger.formatters.xLogFormatter_Color;


public abstract class xAppAbstract implements xStartable, FailureAction {

	// app instance
	protected static volatile xAppAbstract instance = null;
	protected static final Object instanceLock = new Object();
	// just to prevent gc
	@SuppressWarnings("unused")
	private static final Keeper keeper = Keeper.get();

	protected final AtomicBoolean running = new AtomicBoolean(false);
	protected final AtomicBoolean stopped = new AtomicBoolean(false);

	protected final List<StepDAO> steps;
	public volatile AtomicInteger nextStep = null;
	protected final int minStep;
	protected final int maxStep;

	private volatile long startTime = -1;



	/**
	 * Get a single instance of the app.
	 */
	public static xAppAbstract get() {
		return instance;
	}
	public static xAppAbstract peak() {
		return instance;
	}



	// new instance
	public xAppAbstract() {
		Keeper.add(this);
		xVars.init();
		// find startup/shutdown steps
		final Class<? extends xAppAbstract> clss = this.getClass();
		if(clss == null) throw new RuntimeException("Failed to get app class!");
		// get method annotations
		final Method[] methods = clss.getMethods();
		if(utils.isEmpty(methods)) throw new RuntimeException("Failed to get app methods!");

		final List<StepDAO> steps = new ArrayList<StepDAO>();
		for(final Method method : methods) {
			final xAppStep anno = method.getAnnotation(xAppStep.class);
			if(anno == null) continue;
			// found step method
			final StepDAO dao = new StepDAO(anno, method);
			steps.add(dao);
		}
		this.steps = Collections.unmodifiableList(steps);
		// find min/max priority
		{
			int min = -1;
			int max = -1;
			for(final StepDAO step : this.steps) {
				final int p = step.step;
				if(min == -1 || p < min)
					min = p;
				if(max == -1 || p > max)
					max = p;
			}
			if(min == -1 || max == -1)
				throw new RuntimeException("No startup steps found!");
			this.minStep = min;
			this.maxStep = max;
		}
		Failure.register(this);
	}



	protected abstract void processArgs(final List<String> args);
	protected void processDefaultArgs(final List<String> args) {
		final Iterator<String> it = args.iterator();
		while(it.hasNext()) {
			final String arg = it.next();
			switch(arg) {
			case "--debug":
				xVars.debug(true);
				it.remove();
			}
		}
	}



	// initialize console and enable colors
	protected void initConsole() {
		xConsole console = xLog.peekConsole();
		if(console == null || console instanceof xNoConsole) {
			if(!utils.isJLineAvailable())
				Failure.fail("jline library not found");
			console = new jlineConsole();
			xLog.setConsole(console);
		}
		// enable console color
		get().log().setFormatter(
			new xLogFormatter_Color(),
			logHandlerConsole.class
		);
	}



	// ------------------------------------------------------------------------------- //
	// startup



	@Override
	public void Start() {
		if(this.stopped.get())
			throw new IllegalStateException("App already stopped!");
		if(!this.running.compareAndSet(false, true))
			throw new IllegalStateException("App already started!");
		this.log().title(
				(new StringBuilder())
				.append("Starting ")
				.append(this.getTitle())
				.append("..")
				.toString()
		);
		// startup task
		final StartupTask task = new StartupTask(this);
		xThreadPool.getMainPool()
			.runLater(task);
	}



	// ensure not root
	@xAppStep(type=StepType.STARTUP, title="RootCheck", priority=5)
	public void __STARTUP_rootcheck() {
		final String user = System.getProperty("user.name");
		if("root".equals(user))
			this.log().warning("It is recommended to run as a non-root user");
		else
		if("administrator".equalsIgnoreCase(user) || "admin".equalsIgnoreCase(user))
			this.log().warning("It is recommended to run as a non-administrator user");
	}



	// clock
	@xAppStep(type=StepType.STARTUP, title="Clock", priority=11)
	public void __STARTUP_clock() {
		this.startTime = xClock.get(true).millis();
	}



	// ------------------------------------------------------------------------------- //
	// shutdown



	@Override
	public void Stop() {
		if(!this.stopped.compareAndSet(false, true) ||
				!this.running.compareAndSet(true,  false)) {
			this.log().finest("Already stopping..");
			return;
		}
		this.log().title(
				new String[] {
					(new StringBuilder())
						.append("Stopping ")
						.append(this.getTitle())
						.append("..")
						.toString(),
					(new StringBuilder())
						.append("Uptime: ")
						.append(this.getUptimeString())
						.toString()
				}
		);
		// shutdown task
		final ShutdownTask task = new ShutdownTask(this);
		xThreadPool.getMainPool()
			.runLater(task);
	}
	@Override
	public void doFailAction() {
		this.Stop();
	}



	// total time running
	@xAppStep(type=StepType.SHUTDOWN, title="UptimeStats", priority=100)
	public void __SHUTDOWN_uptimestats() {
//TODO: display total time running
	}



	// ------------------------------------------------------------------------------- //



	@Override
	public abstract void run();
	@Override
	public boolean isRunning() {
		if(Failure.hasFailed())
			return false;
		return this.running.get()
			&& !this.stopped.get();
	}
	public boolean isStopping() {
		if(Failure.hasFailed())
			return true;
		return this.stopped.get();
	}



	public long getUptime() {
		if(this.startTime == -1)
			return 0;
		return xClock.get(true).millis() - this.startTime;
	}
	public String getUptimeString() {
		final xTime time = xTime.get(this.getUptime());
		if(time == null)
			return null;
		return time.toFullString();
	}



	public abstract String getName();
	public abstract String getTitle();
	public abstract String getFullTitle();
	public abstract String getVersion();
	public abstract String getCommitHash();
	public abstract String getCommitHashFull();
	public abstract String getURL();
	public abstract String getOrgName();
	public abstract String getOrgURL();
	public abstract String getIssueName();
	public abstract String getIssueURL();



	// ------------------------------------------------------------------------------- //



	// ascii header
	protected abstract void displayLogo();



	protected void displayColors() {
		final PrintStream out = AnsiConsole.out;
		out.println(Ansi.ansi().reset());
		for(final Ansi.Color color : Ansi.Color.values()) {
			final String name = utilsString.padCenter(7, color.name(), ' ');
			out.println(Ansi.ansi()
				.a("   ")
				.fg(color).a(name)
				.a("   ")
				.bold().a("BOLD-"+name)
				.a("   ")
				.boldOff().fg(Ansi.Color.WHITE).bg(color).a(name)
				.reset()
			);
		}
		out.println(Ansi.ansi().reset());
		out.println();
		out.flush();
	}
	public void displayStartupVars() {
		final PrintStream out = AnsiConsole.out;
		final String hash;
		out.println();
		out.println(" Pid: "+utilsProc.getPid());
		out.println(" Version: "+this.getVersion());
		out.println(" Commit:  "+this.getCommitHash());
		out.println(" Running as:  "+System.getProperty("user.name"));
		out.println(" Current dir: "+System.getProperty("user.dir"));
		out.println(" java home:   "+System.getProperty("java.home"));
//		out.println(" Terminal:    "+System.getProperty("jline.terminal"));
		if(xVars.debug())
			out.println(" Forcing Debug: true");
//		if(utils.notEmpty(args)) {
//			out.println();
//			out.println(utilsString.addStrings(" ", args));
//		}
		out.println();
		out.flush();
	}



	protected void displayLogoLine(final PrintStream out,
			final Map<Integer, String> colors, final String line) {
		final StringBuilder str = new StringBuilder();
		int last = 0;
		boolean hasColor = false;
		for(final Entry<Integer, String> entry : colors.entrySet()) {
			final Integer posInt = entry.getKey();
			final int pos = posInt.intValue() - 1;
			if(pos > last)
				str.append(line.substring(last, pos));
			last = pos;
			if(hasColor)
				str.append("|@");
			hasColor = true;
			str.append("@|");
			str.append(entry.getValue());
			str.append(" ");
		}
		if(last < line.length())
			str.append(line.substring(last));
		if(hasColor)
			str.append("|@");
		out.println(
				Ansi.ansi().a(" ")
				.render(str.toString())
				.reset().a(" ")
		);
	}



	// logger
	private volatile xLog _log = null;
	public xLog log() {
		if(this._log == null)
			this._log = xLog.getRoot();
		return this._log;
	}



}
