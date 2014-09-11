package com.poixson.commonapp.gui;

import java.awt.Font;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsNumbers;
import com.poixson.commonjava.Utils.xString;
import com.poixson.commonjava.xLogger.xLog;


public class xFont {

	protected volatile String family = null;
	protected volatile Style  style  = Style.PLAIN;
	protected final    int base = 12;
	protected volatile int size = 0;



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
		if(utils.notEmpty(format))
			this.apply(format);
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
		if(utils.isEmpty(format))
			return this.getFont();
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
		if(utils.isEmpty(format))
			return this;
		final xString str = xString.get(format);
		while(str.delim(",").hasNext()) {
			// get part
			final String part;
			{
				final String prt = str.part();
				if(prt == null) break;
				part = prt.trim().toLowerCase();
				if(utils.isEmpty(part)) continue;
			}
			// font style
			switch(part) {
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
			if(part.startsWith("family") || part.startsWith("fam")) {
				String tmp = (part.startsWith("family") ? part.substring(6) : part.substring(3) ).trim();
				if(tmp.startsWith("=") || tmp.startsWith(":"))
					tmp = tmp.substring(1).trim();
				this.family = tmp;
				continue;
			}
			// font size
			if(part.startsWith("size")) {
				String tmp = part.substring(4).trim();
				if(tmp.startsWith("=") || tmp.startsWith(":"))
					tmp = tmp.substring(1).trim();
				if(tmp.startsWith("+"))
					tmp = tmp.substring(1).trim();
				final Integer i = utilsNumbers.toInteger(tmp);
				if(i == null) {
					xLog.getRoot().warning("Invalid font size value: "+part);
					continue;
				}
				this.size = i.intValue();
				continue;
			}
			// unknown format
			xLog.getRoot().warning("Unknown font formatting: "+part);
		}
		return this;
	}



}
