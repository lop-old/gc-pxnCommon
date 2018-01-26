package com.poixson.app.steps;

import com.poixson.app.Failure;
import com.poixson.app.xApp;
import com.poixson.app.xAppStep;
import com.poixson.app.xAppStep.StepType;
import com.poixson.logger.xLog;
import com.poixson.logger.console.jLineConsole;
import com.poixson.utils.Utils;


public class xAppSteps_Console {



	public xAppSteps_Console() {
		jLineConsole.init();
	}



	// ------------------------------------------------------------------------------- //
	// startup steps



	@xAppStep(type=StepType.STARTUP, title="", priority=90)
	public void __STARTUP_console(final xApp app) {
		if (Failure.hasFailed())
			return;
		// initialize console and enable colors
		if (System.console() != null) {
			if ( ! Utils.isJLineAvailable()) {
				Failure.fail("jline library not found");
				return;
			}
		}
	}



	// ------------------------------------------------------------------------------- //
	// shutdown steps



	// stop console input
	@xAppStep(type=StepType.SHUTDOWN, title="Console", priority=30)
	public void __SHUTDOWN_console() {
		xLog.Shutdown();
	}



}
