package com.poixson.utils;

import java.awt.Font;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.xLog;


public final class SwingUtils {
	private SwingUtils() {}



	// change font size
	public static void ChangeFontSize(final JComponent component, final int size) {
		if (component == null) throw new RequiredArgumentException("component");
		final Font font = component.getFont();
		component.setFont(
			new Font(
				font.getFontName(),
				font.getStyle(),
				font.getSize() + size
			)
		);
	}



	// load image file/resource
	public static ImageIcon LoadImageResource(final String path) {
		// open file
		if ((new File(path)).exists()) {
			try {
				final ImageIcon image = new ImageIcon(path);
				log().fine("Loaded image file:", path);
				return image;
			} catch(Exception ignore) {}
		}
		// open resource
		try {
			final ImageIcon image = new ImageIcon(ClassLoader.getSystemResource(path));
			log().fine("Loaded image resource:", path);
			return image;
		} catch(Exception ignore) {}
		log().warning("Failed to load image:", path);
		return null;
	}



	// logger
	public static xLog log() {
		return guiUtils.log();
	}



}
