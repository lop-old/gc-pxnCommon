package com.poixson.app.steps;

import com.poixson.app.xApp;
import com.poixson.app.xAppStep;
import com.poixson.app.xAppStep.StepType;


public class xAppSteps_Tool {



	// ------------------------------------------------------------------------------- //
	// startup steps



	// ensure not root
	@xAppStep(type=StepType.STARTUP, title="RootCheck", priority=10)
	public void __STARTUP_rootcheck(final xApp app) {
		final String user = System.getProperty("user.name");
		if ("root".equals(user)) {
			app.warning("It is recommended to run as a non-root user");
		} else
		if ("administrator".equalsIgnoreCase(user)
		|| "admin".equalsIgnoreCase(user)) {
			app.warning("It is recommended to run as a non-administrator user");
		}
	}



	// ------------------------------------------------------------------------------- //
	// shutdown steps



}
