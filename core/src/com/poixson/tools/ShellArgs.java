package com.poixson.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.poixson.app.xVars;
import com.poixson.utils.NumberUtils;
import com.poixson.utils.Utils;


public class ShellArgs {

	protected boolean allowShortFlagValues = false;
//TODO:
//	protected Boolean hasUnknownFlags      = null;

	protected final Map<String, String> flags;
	protected final List<String> commands;

//TODO:
//	protected final List<String> knownFlags = new ArrayList<String>();



	public static ShellArgs Init(final String[] argsArray) {
		final ShellArgs argsTool = new ShellArgs(argsArray);
		// debug flag
		{
			final Boolean debugVal = argsTool.getFlagBoolean("-d", "--debug");
			if (debugVal != null) {
				xVars.setDebug( debugVal.booleanValue() );
			}
		}
//TODO:
/*
		// ansi color flags
		{
			final Boolean colorVal = argsTool.getFlagBoolean("--color");
			final Boolean nocolVal = argsTool.getFlagBoolean("--no-color");
			if (nocolVal != null) {
				xVars.setConsoleColor( ! nocolVal.booleanValue() );
			} else
			if (colorVal != null) {
				xVars.setConsoleColor( colorVal.booleanValue() );
			}
		}
		// detect color support
//TODO:
		// clear screen
		if (xVars.isConsoleColorEnabled()) {
			xLog.getConsole()
				.clear();
		}
*/
		return argsTool;
	}



	public ShellArgs(final String[] argsArray) {
		if (Utils.isEmpty(argsArray)) {
			this.flags    = Collections.unmodifiableMap(  new HashMap<String, String>(0) );
			this.commands = Collections.unmodifiableList( new ArrayList<String>(0)       );
			return;
		}
		final HashMap<String, String> flags = new HashMap<String, String>();
		final List<String> commands = new ArrayList<String>();
		final ListIterator<String> it = Arrays.asList(argsArray).listIterator();
		boolean allCommands = false;
		while (it.hasNext()) {
			final String arg = it.next();
			if (Utils.isEmpty(arg)) continue;
			// --
			if (allCommands) {
				commands.add(arg);
				continue;
			}
			if ("--".equals(arg)) {
				allCommands = true;
				continue;
			}
			// --flag
			if (arg.startsWith("--")) {
				// --flag=value
				if (arg.contains("=")) {
					final String[] parts = arg.split("=", 2);
					flags.put(
						parts[0],
						parts[1]
					);
					continue;
				}
				// --flag value
				if (it.hasNext()) {
					// peek next arg
					final String nextArg = it.next();
					// blank value
					if (Utils.isBlank(nextArg)) {
						flags.put(
							arg,
							"true"
						);
					} else
					// next arg is a flag
					if (nextArg.startsWith("-")) {
						flags.put(
							arg,
							"true"
						);
						it.previous();
					// flag has a value
					} else {
						flags.put(
							arg,
							nextArg
						);
					}
					continue;
				}
				// --flag
				if (!flags.containsKey(arg)) {
					flags.put(
						arg,
						"true"
					);
				}
				continue;
			}
			// -flag
			if (arg.startsWith("-")) {
				if (arg.length() > 2) {
					final String key = arg.substring(0, 2);
					final String val = arg.substring(2);
					flags.put(
						key,
						val.startsWith("=")
						? val.substring(1)
						: val
					);
					continue;
				}
				// -f value
				if (this.allowShortFlagValues) {
					if (it.hasNext()) {
						final String val = it.next();
						if (val.startsWith("-")) {
							flags.put(
								arg,
								"true"
							);
							it.previous();
							continue;
						}
					}
				}
				// -f
				if (!flags.containsKey(arg)) {
					flags.put(
						arg,
						"true"
					);
					continue;
				}
			}
			// not flag, must be command
			commands.add(arg);
		}
		// finished preparing
		this.flags    = Collections.unmodifiableMap(  flags    );
		this.commands = Collections.unmodifiableList( commands );
	}



	public String[] getCommands() {
		return this.commands.toArray(new String[0]);
	}
	public Map<String, String> getFlags() {
		return this.flags;
	}



	public String getCommand(final int index) {
		return this.commands.get(index);
	}
	public boolean hasCommand(final String cmd) {
		return this.commands.contains(cmd);
	}



	public String getFlag(final String... keys) {
		for (final String k : keys) {
			final String val = this.getFlag_Single(k);
			if (val != null) {
				return val;
			}
		}
		return null;
	}
	protected String getFlag_Single(final String key) {
		if (!this.flags.containsKey(key)) {
			return null;
		}
		// don't allow "-x value"
		if (!this.allowShortFlagValues) {
			if (!key.startsWith("--")) {
				return "true";
			}
		}
		return this.flags.get(key);
	}



	public boolean getFlagBool(final boolean defaultValue, final String... keys) {
		for (final String k : keys) {
			final Boolean val = this.getFlagBoolean_Single(k);
			if (val != null) {
				return val.booleanValue();
			}
		}
		return defaultValue;
	}
	public Boolean getFlagBoolean(final String... keys) {
		for (final String k : keys) {
			final Boolean val = this.getFlagBoolean_Single(k);
			if (val != null) {
				return val;
			}
		}
		return null;
	}
	protected Boolean getFlagBoolean_Single(final String key) {
		final String str = this.getFlag(key);
		if (str == null) {
			return null;
		}
		return
			Boolean.valueOf(
				NumberUtils.toBoolean(
					str,
					true
				)
			);
	}



	public boolean hasFlag(final String... keys) {
		for (final String k : keys) {
			final boolean exists = this.hasFlag_Single(k);
			if (exists) {
				return true;
			}
		}
		return false;
	}
	protected boolean hasFlag_Single(final String key) {
		return this.flags.containsKey(key);
	}



	public boolean isHelp() {
		return this.hasFlag(
			"-h",
			"--help"
		);
	}



}
