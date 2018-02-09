package com.poixson.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.poixson.exceptions.RequiredArgumentException;
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



	public AsciiArtBuilder(final String...lines) {
		if (Utils.isEmpty(lines)) throw new RequiredArgumentException("lines");
		this.lines = lines;
		for (int i=0; i<lines.length; i++) {
			this.colorLocations.put(
				Integer.valueOf(i),
				new HashMap<Integer, String>()
			);
		}
	}



	public String[] build() {
		final int lineCount = this.lines.length;
		String[] result = new String[ lineCount ];
		final String bgColor = this.getBgColor();
		final int indent = this.indent;
		final StringBuilder buf = new StringBuilder();
		final int maxLineSize = StringUtils.FindLongestLine(this.lines);
		boolean withinTag = false;
		LINE_LOOP:
		for (int lineIndex=0; lineIndex<lineCount; lineIndex++) {
			final String line = this.lines[lineIndex];
			// blank line
			if (Utils.isEmpty(line)) {
				result[ lineIndex ] =
					StringUtils.Repeat(
						(indent * 2) + maxLineSize,
						' '
					);
				continue LINE_LOOP;
			}
			buf.setLength(0);
			if (indent > 0) {
				buf.append(
					StringUtils.Repeat(indent, ' ')
				);
			}
			// handle colors
			withinTag = false;
			final HashMap<Integer, String> colorsMap =
				this.colorLocations.get(
					Integer.valueOf(lineIndex)
				);
			// no colors to set
			if (Utils.isEmpty(colorsMap)) {
				if (bgColor != null) {
					withinTag = true;
					buf.append("@|")
						.append(bgColor)
						.append(' ');
				}
				buf.append(line);
			// has colors
			} else {
				// order by position in line
				final List<Integer> ordered =
					new ArrayList<Integer>(
						colorsMap.keySet()
					);
				Collections.sort(ordered);
				int lastX = -1;
				COLOR_LOOP:
				for (final Integer posX : ordered) {
					final String colorStr = colorsMap.get(posX);
					if (Utils.isEmpty(colorStr))
						continue COLOR_LOOP;
					// color doesn't start at front of line
					if (lastX == -1) {
						lastX = 0;
						if (posX > 0) {
							if (bgColor != null) {
								withinTag = true;
								buf.append("@|")
									.append(bgColor)
									.append(' ');
							}
						}
					}
					// fill up to first color
					if (posX > 0) {
						buf.append( line.substring(lastX, posX) );
					}
					if (withinTag) {
						buf.append("|@");
					}
					// set color at position
					withinTag = true;
					buf.append("@|");
					if (bgColor != null) {
						buf.append(bgColor)
							.append(',');
					}
					buf.append(colorStr)
						.append(' ');
					lastX = posX;
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
					StringUtils.Repeat(
						(maxLineSize - line.length()) + indent,
						' '
					)
				);
			}
			result[ lineIndex ] = buf.toString();
		} // end LINE_LOOP
		return result;
	}



	// color alias
	public AsciiArtBuilder aliasColor(final String alias, final String actual) {
		this.colorAliases.put(alias, actual);
		return this;
	}



	// default background color
	public String getBgColor() {
		final String bgColor = this.bgColor;
		return (
			Utils.isEmpty(bgColor)
			? null
			: bgColor
		);
	}
	public AsciiArtBuilder setBgColor(final String bgColor) {
		this.bgColor = StringUtils.ForceStarts("bg_", bgColor);
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
				StringUtils.ReplaceTags(
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
			StringUtils.MergeStrings(
				',',
				existing,
				color
			)
		);
		return this;
	}
	public AsciiArtBuilder setBgColor(final String bgColor,
			final int posX, final int posY) {
		if (Utils.isEmpty(bgColor)) throw new RequiredArgumentException("bgColor");
		return
			this.setColor(
				StringUtils.ForceStarts("bg_", bgColor),
				posX,
				posY
			);
	}



}
