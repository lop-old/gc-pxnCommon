package com.poixson.tools;

import java.awt.Font;

import com.poixson.logger.xLog;
import com.poixson.utils.NumberUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.guiUtils;


public class xFont {

	protected volatile String family = null;
	protected volatile Style  style  = Style.PLAIN;
	protected final    int    base   = 12;
	protected volatile int    size   = 0;



	public enum Style {
		PLAIN (Font.PLAIN),
		BOLD  (Font.BOLD),
		ITALIC(Font.ITALIC);
		protected final int value;
		Style(final int value) {
			this.value = value;
		}
	}



	public xFont(final String format) {
		this();
		if (Utils.notEmpty(format)) {
			this.apply(format);
		}
	}
	public xFont(final xFont font) {
		this();
		this.family = font.family;
		this.style  = font.style;
		this.size   = font.size;
	}
	public xFont() {
	}



	public Font getFont(final String format) {
		if (Utils.isEmpty(format)) {
			return this.getFont();
		}
		final xFont font = new xFont(this);
		font.apply(format);
		return font.getFont();
	}
	public Font getFont() {
		return new Font(
			this.family,
			this.style.value,
			this.base + this.size
		);
	}



	public xFont apply(final String format) {
		if (Utils.isEmpty(format))
			return this;
		final xString str = xString.getNew(format);
		str.delim(",");
		while (str.hasNext()) {
			// get part
			final String part;
			{
				final String prt = str.part();
				if (prt == null)
					break;
				part = prt.trim().toLowerCase();
				if (Utils.isEmpty(part))
					continue;
			}
			// font style
			switch (part) {
			case "b":
			case "bold":
				this.style = Style.BOLD;
				continue;
			case "i":
			case "italic":
				this.style = Style.ITALIC;
				continue;
			case "p":
			case "plain":
				this.style = Style.PLAIN;
				continue;
			}
			// font family
			if (part.startsWith("fam")) {
				String tmp = (
					part.startsWith("family")
					? part.substring(6)
					: part.substring(3)
				).trim();
				if (tmp.startsWith("=") || tmp.startsWith(":"))
					tmp = tmp.substring(1).trim();
				this.family = tmp;
				continue;
			}
			// font size
			if (part.startsWith("size")) {
				String tmp = part.substring(4).trim();
				if (tmp.startsWith("=") || tmp.startsWith(":")) {
					tmp = tmp.substring(1).trim();
				}
				if (tmp.startsWith("+")) {
					tmp = tmp.substring(1).trim();
				}
				final Integer i = NumberUtils.toInteger(tmp);
				if (i == null) {
					this.log().warning("Invalid font size value: ", part);
					continue;
				}
				this.size = i.intValue();
				continue;
			}
			// unknown format
			this.log().warning("Unknown font formatting: ", part);
		}
		return this;
	}



	// logger
	public xLog log() {
		return guiUtils.log();
	}



}
