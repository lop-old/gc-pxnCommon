/*
package com.poixson.logger.commands;

import com.poixson.threadpool.xThreadPoolQueue.TaskPriority;
import com.poixson.threadpool.types.xThreadPool_Main;
import com.poixson.tools.events.xEventData;
import com.poixson.tools.events.xEventListener;
import com.poixson.tools.events.xHandlerSimple;


public class xCommandHandler extends xHandlerSimple {
	private static final String LISTENER_METHOD_NAME = "onCommand";



	public xCommandHandler() {
		super();
	}



	// listener type
	@Override
	protected Class<? extends xEventListener> getEventListenerType() {
		return xCommandListener.class;
	}
	// event type
	@Override
	protected Class<? extends xEventData> getEventDataType() {
		return xCommandEvent.class;
	}



	// fixed method name
	@Override
	protected String getMethodName() {
		return LISTENER_METHOD_NAME;
	}



	// trigger event
	@Override
	public void trigger(final xEventData event) {
		if ( ! (event instanceof xCommandEvent) )
			throw new IllegalArgumentException("event must be instance of xCommandEvent!");
		final xCommandEvent cmdEvent = (xCommandEvent) event;
		// pass to main thread
		final Runnable run =
			new Runnable() {
				private volatile xCommandHandler handler = null;
				private volatile xCommandEvent   cmdEvent = null;
				public Runnable init(final xCommandHandler handler, final xCommandEvent cmdEvent) {
					this.handler  = handler;
					this.cmdEvent = cmdEvent;
					return this;
				}
				@Override
				public void run() {
					this.handler
						.doTrigger(this.cmdEvent);
				}
			}.init(this, cmdEvent);
		xThreadPool_Main.get()
			.addTask(run, TaskPriority.NORM);
	}
	public void doTrigger(final xCommandEvent event) {
		// pass to handler
		super.trigger(event);
		// unknown command
		if (!event.isHandled()) {
			final xCommandEvent cmdEvent = (xCommandEvent) event;
			this.log()
				.publish(
					null,
					"Unknown command:",
					cmdEvent.getArg(0)
				);
		}
	}



}
*/
