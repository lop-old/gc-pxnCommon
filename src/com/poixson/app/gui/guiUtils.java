package com.poixson.app.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import com.poixson.utils.ReflectUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLog;


public final class guiUtils {
	private guiUtils() {}
	private static final String LOG_NAME = "GUI";



	public static void RotateFont(final Component comp, final int degrees) {
		RotateFont(
			comp,
			Math.toRadians(degrees)
		);
	}
	public static void RotateFont(final Component comp, final double rad) {
		comp.setFont(
			RotateFont(
				comp.getFont(),
				rad
			)
		);
	}
	public static Font RotateFont(final Font font, final int degrees) {
		return RotateFont(
			font,
			Math.toRadians(degrees)
		);
	}
	public static Font RotateFont(final Font font, final double rad) {
		final AffineTransform transform = new AffineTransform();
		transform.rotate(rad);
		return font.deriveFont(transform);
	}



	// load image file/resource
	public static ImageIcon loadImageResource(final String path) {
		// open file
		{
			final File file = new File(path);
			if (file.exists()) {
				try {
					final ImageIcon image = new ImageIcon(path);
					log().finer("Loaded image file: "+path);
					return image;
				} catch(Exception ignore) {}
			}
		}
		// open resource
		try {
			final ImageIcon image = new ImageIcon(ClassLoader.getSystemResource(path));
			log().finer("Loaded image resource: "+path);
			return image;
		} catch(Exception ignore) {}
		log().warning("Failed to load image: "+path);
		return null;
	}



	/**
	 * Forces a function to be called from the event dispatch thread.
	 * @param callingFrom Class object which contains the function.
	 * @param callingMethod The function which is being called.
	 * @param args Arguments being passed to the function.
	 * @return false if already in the event dispatch thread;
	 *   if not calling from the event dispatch thread, this will
	 *   create a new Runnable instance, calling the provided function
	 *   later from the proper thread.
	 */
	public static boolean forceDispatchThread(final Object callingFrom,
			final String methodStr, final Object...args) {
		if (callingFrom == null)      throw RequiredArgumentException.getNew("callingFrom");
		if (Utils.isEmpty(methodStr)) throw RequiredArgumentException.getNew("methodStr");
		// already running from event dispatch thread
		if (SwingUtilities.isEventDispatchThread()) {
			return false;
		}
		// get calling method
		final Method method;
		try {
			final Class<?> clss = callingFrom.getClass();
			final Class<?>[] params = ReflectUtils.ArgsToClasses(args);
			method = clss.getMethod(methodStr, params);
		} catch (NoSuchMethodException e) {
			log().trace(e);
			throw new IllegalArgumentException("Method not found");
		}
		// pass to dispatch thread
		{
			final Runnable run =
				new Runnable() {
					private volatile Object   o = null;
					private volatile Method   m = null;
					private volatile Object[] a = null;
					public Runnable init(final Object ob,
							final Method mth, final Object[] ags) {
						this.o = ob;
						this.m = mth;
						this.a = ags;
						return this;
					}
					@Override
					public void run() {
						try {
							this.m.invoke(
								this.o,
								this.a
							);
						} catch (IllegalAccessException e) {
							log().trace(e);
						} catch (IllegalArgumentException e) {
							log().trace(e);
						} catch (InvocationTargetException e) {
							log().trace(e);
						} catch (Exception e) {
							log().trace(e);
						}
					}
				}.init(callingFrom, method, args);
			try {
				SwingUtilities.invokeAndWait(run);
			} catch (InvocationTargetException e) {
				log().trace(e);
			} catch (InterruptedException e) {
				log().trace(e);
			}
		}
		return true;
	}



	// logger
	public static xLog log() {
		return xLog.getRoot()
				.get(LOG_NAME);
	}



}
