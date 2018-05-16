package com.poixson.app.commands;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

import org.jline.reader.Completer;

import com.poixson.utils.Utils;


public class xCommandHandlerJLine extends xCommandHandler {

	protected final AtomicReference<Completer> completer =
			new AtomicReference<Completer>(null);



	public xCommandHandlerJLine() {
		super();
	}



	@Override
	protected boolean registerMethod(
			final Object object, final Method method, final xCommand anno) {
		if ( ! super.registerMethod(object, method, anno) )
			return false;
		this.completer.set(null);
		return true;
	}



	@Override
	public void unregisterObject(final Object object) {
		if (object == null) return;
		super.unregisterObject(object);
		this.completer.set(null);
	}
	@Override
	public void unregisterMethod(final Object object, final String methodName) {
		if (object == null || Utils.isEmpty(methodName)) return;
		super.unregisterMethod(object, methodName);
		this.completer.set(null);
	}
	@Override
	public void unregisterAll() {
		super.unregisterAll();
		this.completer.set(null);
	}



}
