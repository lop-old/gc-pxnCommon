package com.poixson.commonjava.scheduler.ticker;

import java.util.concurrent.atomic.AtomicInteger;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.xLogger.xConsole;
import com.poixson.commonjava.xLogger.xLog;


public class xTickPrompt implements xTickListener {

	protected final String[] frames = new String[] {
			" [|]>",
			" [/]>",
			" [-]>",
			" [\\]>"
	};



	protected final AtomicInteger num = new AtomicInteger(0);



	public xTickPrompt() {
		xTickHandler.get()
			.register(this);
	}
	@Override
	public String getName() {
		return "TickPrompt";
	}
	@Override
	public String toString() {
		return this.getName();
	}



	@Override
	public void onTick(final xTickEvent event) {
		final xConsole console = xLog.getConsole();
		{
			final String old = console.getPrompt();
			if(utils.notEmpty(old) && !" #>".equals(old)) {
				boolean found = false;
				for(final String str : this.frames) {
					if(str.equals(old)) {
						found = true;
						break;
					}
				}
				if(!found) return;
			}
		}
		final int num = this.num.getAndIncrement();
		if(num+1 >= this.frames.length)
			this.num.set(0);
		console.setPrompt(this.frames[num]);
//		final String str;
//		switch( this.num.getAndIncrement() ) {
//		case 0:
//			str = " |>";
//			break;
//		case 1:
//			str = " />";
//			break;
//		case 2:
//			str = " ->";
//			break;
//		case 3:
//			str = " \\>";
//			this.num.set(0);
//			break;
//		default:
//			return;
//		}
//		console.setPrompt(str);
	}



}
