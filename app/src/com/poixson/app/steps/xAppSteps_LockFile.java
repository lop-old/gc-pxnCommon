package com.poixson.app.steps;

import com.poixson.app.Failure;
import com.poixson.app.xApp;
import com.poixson.app.xAppStep;
import com.poixson.app.xAppStep.StepType;
import com.poixson.logger.xLog;
import com.poixson.tools.LockFile;


public class xAppSteps_LockFile {



	// ------------------------------------------------------------------------------- //
	// startup steps



	// lock file
	@xAppStep( Type=StepType.STARTUP, Title="LockFile", StepValue=70 )
	public void __STARTUP_lockfile(final xApp app) {
		final String filename = app.getName()+".lock";
		final LockFile lock = LockFile.get(filename);
		if ( ! lock.acquire() )
			Failure.fail("Failed to get lock on file:", filename);
	}



	// ------------------------------------------------------------------------------- //
	// shutdown steps



	// release lock file
	@xAppStep( Type=StepType.SHUTDOWN, Title="LockFile", StepValue=20 )
	public void __SHUTDOWN_lockfile(final xApp app, final xLog log) {
		final String filename = app.getName()+".lock";
		LockFile.getRelease(filename);
	}



}
