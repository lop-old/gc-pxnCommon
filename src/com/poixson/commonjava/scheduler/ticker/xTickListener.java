package com.poixson.commonjava.scheduler.ticker;

import com.poixson.commonjava.EventListener.xListener;


public interface xTickListener extends xListener {


	public void onTick(final xTickEvent event);


}
