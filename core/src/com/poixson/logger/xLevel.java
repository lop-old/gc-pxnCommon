package com.poixson.logger;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.utils.NumberUtils;
import com.poixson.utils.Utils;


public class xLevel implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final List<xLevel> knownLevels = new CopyOnWriteArrayList<xLevel>();
	private static volatile int minValue = 0;
	private static volatile int maxValue = 0;

	public static final transient xLevel OFF     = new xLevel("OF", "OFF",     Integer.MAX_VALUE);
	public static final transient xLevel STDERR  = new xLevel("ER", "ERR",     9000);
	public static final transient xLevel STDOUT  = new xLevel("OT", "OUT",     8000);
	public static final transient xLevel TITLE   = new xLevel("TT", "TITLE",   7000);
	public static final transient xLevel FATAL   = new xLevel("FA", "FATAL",   2000);
	public static final transient xLevel SEVERE  = new xLevel("SV", "SEVERE",  1000);
	public static final transient xLevel NOTICE  = new xLevel("NT", "NOTICE",  900);
	public static final transient xLevel WARNING = new xLevel("WN", "WARNING", 800);
	public static final transient xLevel INFO    = new xLevel("IN", "INFO",    700);
	public static final transient xLevel STATS   = new xLevel("ST", "STATS",   600);
	public static final transient xLevel FINE    = new xLevel("FI", "FINE",    500);
	public static final transient xLevel FINER   = new xLevel("FR", "FINER",   400);
	public static final transient xLevel FINEST  = new xLevel("FT", "FINEST",  300);
	public static final transient xLevel DETAIL  = new xLevel("DE", "DETAIL",  100);
	public static final transient xLevel ALL     = new xLevel("AL", "ALL",     Integer.MIN_VALUE);

	public final String name;
	public final String shortName;
	public final int    value;



	private xLevel(final String shortName, final String name, final int value) {
		if (Utils.isEmpty(name))      throw new RequiredArgumentException("name");
		if (Utils.isEmpty(shortName)) throw new RequiredArgumentException("shortName");
		this.name      = name.toUpperCase();
		this.shortName = shortName.toUpperCase();
		this.value     = value;
		if (value != Integer.MIN_VALUE && value < minValue) minValue = value;
		if (value != Integer.MAX_VALUE && value > maxValue) maxValue = value;
		// validate unique
		if ( ! knownLevels.isEmpty()) {
			final Iterator<xLevel> it = knownLevels.iterator();
			while (it.hasNext()) {
				final xLevel level = it.next();
				// duplicate name
				if (name.equals(level.name))
					throw new RuntimeException("Duplicate xLevel named: "+name);
				// duplicate short-name
				if (shortName.equals(level.shortName)) {
					throw new RuntimeException(
						(new StringBuilder())
							.append("Duplicate xLevel short-name: ")
							.append(shortName)
							.append(" - ")
							.append(name)
							.toString()
					);
				}
				// duplicate value
				if (value == level.value) {
					throw new RuntimeException(
						(new StringBuilder())
							.append("Duplicate xLevel value: ")
							.append(value)
							.append(" - ")
							.append(name)
							.toString()
					);
				}
			}
		}
		knownLevels.add(this);
	}
	@Override
	public Object clone() {
		return this;
	}



	public static xLevel[] getKnownLevels() {
		return knownLevels.toArray(new xLevel[0]);
	}
	public static xLevel FindLevel(final String name) {
		if (Utils.isEmpty(name)) return null;
		if (NumberUtils.isNumeric(name)) {
			return FindLevel(NumberUtils.toInteger(name));
		}
		final String nameStr = name.toUpperCase();
		for (final xLevel level : knownLevels) {
			if (nameStr.equalsIgnoreCase(level.name)) {
				return level;
			}
		}
		return null;
	}
	public static xLevel FindLevel(final Integer value) {
		if (value == null) return null;
		final int val = value.intValue();
		if (val == xLevel.ALL.value) return xLevel.ALL;
		if (val == xLevel.OFF.value) return xLevel.OFF;
		xLevel level = xLevel.OFF;
		int offset = xLevel.OFF.value;
		for (final xLevel lvl : knownLevels) {
			if (level.equals(xLevel.OFF) || level.equals(xLevel.ALL))
				continue;
			if (val < lvl.value) continue;
			if (val - lvl.value < offset) {
				offset = val - lvl.value;
				level = lvl;
			}
		}
		if (level == null) {
			return xLevel.OFF;
		}
		return level;
	}
	public static xLevel parse(final String value) {
		return FindLevel(value);
	}



	public boolean isLoggable(final xLevel level) {
		if (level == null) return false;
		// off (disabled)
		if (this.value == xLevel.OFF.value) return false;
		// all (forced)
		if (this.value == xLevel.ALL.value) return true;
		// check level
		if (level.value == xLevel.ALL.value) return true;
		return this.value <= level.value;
	}



	public boolean equals(final xLevel level) {
		if (level == null) return false;
		return (level.value == this.value);
	}



	// to java level
	public java.util.logging.Level getJavaLevel() {
		return java.util.logging.Level.parse(
			Integer.toString(this.value)
		);
	}



	@Override
	public String toString() {
		return this.name;
	}



}
