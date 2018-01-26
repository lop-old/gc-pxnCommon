package com.poixson.tools;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.xLog;
import com.poixson.logger.xLogPrintStream;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class AsciiArtBuilder {

	protected final String[] lines;
	protected final HashMap<Integer, HashMap<Integer, String>> colorLocations =
			new HashMap<Integer, HashMap<Integer, String>>();

	protected String bgColor = null;
	protected final HashMap<String, String> colorAliases =
			new HashMap<String, String>();

	protected int indent = 0;

	protected final PrintStream out;



	public AsciiArtBuilder(final String...lines) {
		this(null, lines);
	}
	public AsciiArtBuilder(final PrintStream out, final String...lines) {
		if (Utils.isEmpty(lines)) throw new RequiredArgumentException("lines");
		this.out = (
			out == null
			? new xLogPrintStream( xLog.getRoot() )
			: out
		);
		this.lines = lines;
		for (int i=0; i<lines.length; i++) {
			this.colorLocations.put(
				Integer.valueOf(i),
				new HashMap<Integer, String>()
			);
		}
	}



	public void display() {
		final String bgColor = this.bgColor;
		final boolean hasBgColor = Utils.notBlank(bgColor);
		final int indent = this.indent;
		LINES_LOOP:
		for (int posY=0; posY<this.lines.length; posY++) {
			String line = this.lines[posY];
			if (Utils.isEmpty(line)) {
				this.out.println();
				continue LINES_LOOP;
			}
			// handle colors
			final HashMap<Integer, String> colors =
				this.colorLocations.get( Integer.valueOf(posY) );
			boolean withinTag = false;
			final StringBuilder buf = new StringBuilder();
			// no colors to set
			if (Utils.isEmpty(colors)) {
				if (indent > 0) {
					buf.append(
						StringUtils.Repeat(indent, ' ')
					);
				}
				if (hasBgColor) {
					buf.append("@|")
						.append(bgColor)
						.append(' ');
					withinTag = true;
				}
				buf.append(line);
			// has colors
			} else {
				final List<Integer> ordered =
					new ArrayList<Integer>(
						colors.keySet()
					);
				Collections.sort(ordered);
				int lastX = 0;
				COLOR_LOOP:
				for (final Integer posX : ordered) {
					final String colorStr = colors.get(posX);
					if (Utils.isEmpty(colorStr))
						continue COLOR_LOOP;
					if (posX > lastX) {
						if (lastX == 0) {
							if (indent > 0) {
								buf.append(
									StringUtils.Repeat(indent, ' ')
								);
							}
							if (hasBgColor) {
								buf.append("@|")
									.append(bgColor)
									.append(' ');
								withinTag = true;
							}
						}
						buf.append(
							line.substring(lastX, posX)
						);
					}
					lastX = posX;
					if (withinTag) {
						buf.append("|@");
					}
					withinTag = true;
					buf.append("@|");
					if (hasBgColor) {
						buf.append("bg_")
							.append(bgColor)
							.append(',');
					}
					buf.append(colorStr)
						.append(' ');
				} // end COLOR_LOOP
				if (lastX < line.length()) {
					buf.append(
						line.substring(lastX)
					);
				}
			} // end has colors
			if (withinTag) {
				buf.append("|@");
			}
			if (indent > 0) {
				buf.append(
					StringUtils.Repeat(indent, ' ')
				);
			}
			this.out.println( buf.toString() );
		} // end LINES_LOOP
	}



	// color alias
	public AsciiArtBuilder aliasColor(final String alias, final String actual) {
		this.colorAliases.put(alias, actual);
		return this;
	}



	// default background color
	public String getBgColor() {
		return this.bgColor;
	}
	public AsciiArtBuilder setBgColor(final String bgColor) {
		this.bgColor = bgColor;
		return this;
	}



	public int getIndent() {
		return this.indent;
	}
	public AsciiArtBuilder setIndent(final int indent) {
		this.indent = indent;
		return this;
	}



	// color location
	public AsciiArtBuilder setColor(final String color,
			final int posX, final int posY) {
		if (Utils.isEmpty(color)) throw new RequiredArgumentException("color");
		if (posX < 0) throw new IllegalArgumentException("posX is out of range: "+Integer.toString(posX));
		if (posY < 0) throw new IllegalArgumentException("posY is out of range: "+Integer.toString(posY));
		if (posY > this.colorLocations.size()) {
			throw new IllegalArgumentException(
				StringUtils.FormatMessage(
					"posY is out of range: {} > {}",
					posY,
					this.colorLocations.size()
				)
			);
		}
		final HashMap<Integer, String> entry =
			this.colorLocations
				.get(Integer.valueOf(posY));
		final String existing = entry.get( Integer.valueOf(posX) );
		entry.put(
			Integer.valueOf(posX),
			StringUtils.AddStrings(
				",",
				existing,
				color
			)
		);
		return this;
	}
	public AsciiArtBuilder setBgColor(final String color,
			final int posX, final int posY) {
		if (Utils.isEmpty(color)) throw new RequiredArgumentException("color");
		return
			this.setColor(
				"bg_"+color,
				posX,
				posY
			);
	}



}
