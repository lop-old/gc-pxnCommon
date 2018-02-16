package com.poixson.app;

import com.poixson.app.xAppStep.StepType;
import com.poixson.tools.xClock;
import com.poixson.tools.xTime;
import com.poixson.tools.xTimeU;
import com.poixson.utils.Utils;


public abstract class xAppStandard extends xApp {

	protected final xTime startTime = xTime.getNew();



	public xAppStandard() {
	}



	// ------------------------------------------------------------------------------- //
	// startup steps



	// sync clock
	@xAppStep( Type=StepType.STARTUP, Title="Sync Clock", StepValue=100 )
	public void __STARTUP_clock() {
		xClock.get(true);
	}



	// start time
	@xAppStep( Type=StepType.STARTUP, Title="Startup Time", StepValue=200 )
	public void __STARTUP_startuptime() {
		this.startTime
			.set( Utils.getSystemMillis(), xTimeU.MS )
			.lock();
	}



	// ------------------------------------------------------------------------------- //
	// shutdown steps



}
