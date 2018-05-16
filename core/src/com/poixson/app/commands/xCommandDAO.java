package com.poixson.app.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.utils.Utils;


public class xCommandDAO {

	public final String   name;
	public final String[] aliases;

	public final Object obj;
	public final Method method;



	public xCommandDAO(final String name, final String[] aliases,
			final Object obj, final Method method) {
		if (Utils.isEmpty(name)) throw new RequiredArgumentException("name");
		if (obj    == null)      throw new RequiredArgumentException("obj");
		if (method == null)      throw new RequiredArgumentException("method");
		this.name = name;
		this.aliases = (
			Utils.isEmpty(aliases)
			? null
			: aliases
		);
		this.obj    = obj;
		this.method = method;
	}



	public boolean invoke(final String line) {
		try {
			this.method.invoke(this.obj, line);
			return true;
		} catch (IllegalAccessException ignore) {
		} catch (IllegalArgumentException ignore) {
		} catch (InvocationTargetException ignore) {}
		try {
			this.method.invoke(this.obj);
			return true;
		} catch (IllegalAccessException ignore) {
		} catch (IllegalArgumentException ignore) {
		} catch (InvocationTargetException ignore) {}
		return false;
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
