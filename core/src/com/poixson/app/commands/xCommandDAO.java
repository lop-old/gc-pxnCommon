package com.poixson.app.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.xLogRoot;
import com.poixson.threadpool.types.xThreadPool_Main;
import com.poixson.tools.events.xEventListenerDAO;
import com.poixson.utils.Utils;


public class xCommandDAO extends xEventListenerDAO {

	public final String   name;
	public final String[] aliases;



	public xCommandDAO(final String name, final String[] aliases,
			final Object object, final Method method) {
		super(object, method);
		if (Utils.isEmpty(name)) throw new RequiredArgumentException("name");
		this.name = name;
		this.aliases = (
			Utils.isEmpty(aliases)
			? null
			: aliases
		);
	}



	public void invoke(final String line) {
		// ensure main thread
		if (xThreadPool_Main.get().force(this, "invoke", line))
			return;
		xLogRoot.get()
			.finest(
				"Invoking command: {}->{} {}",
				super.object.getClass().getName(),
				super.method.getName(),
				line
			);
		// method(object, line)
		try {
			this.method.invoke(this.object, line);
			return;
		} catch (IllegalAccessException ignore) {
		} catch (IllegalArgumentException ignore) {
		} catch (InvocationTargetException ignore) {}
		// method(object)
		try {
			this.method.invoke(this.object);
			return;
		} catch (IllegalAccessException ignore) {
		} catch (IllegalArgumentException ignore) {
		} catch (InvocationTargetException ignore) {}
		// method with arguments not found/supported
		throw new RuntimeException(
			(new StringBuilder())
				.append("Method arguments not supported: ")
				.append(super.method.getName())
				.toString()
		);
	}



	public boolean isCommand(final String cmd) {
		if (cmd == null)
			return false;
		return cmd.equals(this.name);
	}
	public boolean isAlias(final String cmd) {
		if (cmd == null)
			return false;
		if (Utils.isEmpty(aliases))
			return false;
		for (final String alias : this.aliases) {
			if (cmd.equals(alias))
				return true;
		}
		return false;
	}



}
