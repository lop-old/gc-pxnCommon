package com.poixson.app.steps;

import com.poixson.app.xAppStep;
import com.poixson.app.xAppStep.StepType;
import com.poixson.logger.xLog;


public class xAppSteps_Tool {



	// ------------------------------------------------------------------------------- //
	// startup steps



	// ensure not root
	@xAppStep( Type=StepType.STARTUP, Title="Root Check", StepValue=10 )
	public void __STARTUP_rootcheck(final xLog log) {
		final String user = System.getProperty("user.name");
		if ("root".equals(user)) {
			log.warning("It is recommended to run as a non-root user");
		} else
		if ("administrator".equalsIgnoreCase(user)
		|| "admin".equalsIgnoreCase(user)) {
			log.warning("It is recommended to run as a non-administrator user");
		}
	}



	// ------------------------------------------------------------------------------- //
	// shutdown steps



}
