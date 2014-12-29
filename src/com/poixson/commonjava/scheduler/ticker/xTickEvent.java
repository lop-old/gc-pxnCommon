package com.poixson.commonjava.scheduler.ticker;

import com.poixson.commonjava.EventListener.xEventData;
import com.poixson.commonjava.Utils.utils;


public class xTickEvent extends xEventData {

	public final long id;
	public final long time;



	public xTickEvent(final long id) {
		super();
		this.id = id;
		this.time = utils.getSystemMillis();
	}



}
