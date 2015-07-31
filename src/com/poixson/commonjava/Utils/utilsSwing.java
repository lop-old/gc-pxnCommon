package com.poixson.commonjava.Utils;

import java.awt.Font;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.poixson.commonjava.xLogger.xLog;


public final class utilsSwing {
	private utilsSwing() {}



	// change font size
	public static void changeFontSize(final JComponent component, final int size) {
		if(component == null) throw new NullPointerException("component argument is required!");
		final Font font = component.getFont();
		component.setFont(new Font(
			font.getFontName(),
			font.getStyle(),
			font.getSize() + size
		));
	}



	// load image file/resource
	public static ImageIcon loadImageResource(final String path) {
		// open file
		if((new File(path)).exists()) {
			try {
				final ImageIcon image = new ImageIcon(path);
				log().fine("Loaded image file: "+path);
				return image;
			} catch(Exception ignore) {}
		}
		// open resource
		try {
			final ImageIcon image = new ImageIcon(ClassLoader.getSystemResource(path));
			log().fine("Loaded image resource: "+path);
			return image;
		} catch(Exception ignore) {}
		log().warning("Failed to load image: "+path);
		return null;
	}



	// logger
	public static xLog log() {
		return utils.log();
	}



}
