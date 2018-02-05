package com.poixson.utils;

import java.awt.Component;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import com.poixson.exceptions.ContinueException;
import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.xLog;
import com.poixson.logger.xLogRoot;
import com.poixson.tools.remapped.RemappedMethod;


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
					log().finer("Loaded image file: ", path);
					return image;
				} catch(Exception ignore) {}
			}
		}
		// open resource
		try {
			final ImageIcon image = new ImageIcon(ClassLoader.getSystemResource(path));
			log().finer("Loaded image resource: ", path);
			return image;
		} catch(Exception ignore) {}
		log().warning("Failed to load image: ", path);
		return null;
	}



	/**
	 * Forces a method to be called from the event dispatch thread.
	 * @param callingFrom Class object which contains the method.
	 * @param methodName The method which is being called.
	 * @param args Arguments being passed to the method.
	 * @return resulting return value if not in the event dispatch thread.
	 *   this will queue a task to run in the event dispatch thread.
	 *   if already in the event dispatch thread, ContinueException is
	 *   throws to signal to continue running the method following.
	 * Example:
	 * public boolean getSomething() {
	 *     try {
	 *         return guiUtils
	 *             .forceDispatchResult(this, "getSomething");
	 *     } catch (ContinueException ignore) {}
	 *     // do something here
	 *     return result;
	 * }
	 */
	public <V> V forceDispatchResult(final Object callingFrom,
			final String methodName, final Object...args)
			throws ContinueException {
		if (callingFrom == null)       throw new RequiredArgumentException("callingFrom");
		if (Utils.isEmpty(methodName)) throw new RequiredArgumentException("methodName");
		// already running from event dispatch thread
		if (SwingUtilities.isEventDispatchThread())
			throw new ContinueException();
		// queue to run in event dispatch thread
		final RemappedMethod<V> run =
			new RemappedMethod<V>(
				callingFrom,
				methodName,
				args
			);
		try {
			SwingUtilities.invokeAndWait(run);
			return run.getResult();
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Forces a method to be called from the event dispatch thread.
	 * @param callingFrom Class object which contains the method.
	 * @param methodName The method which is being called.
	 * @param now wait for the result.
	 * @param args Arguments being passed to the method.
	 * @return false if already in the event dispatch thread;
	 *   true if calling from some other thread. this will queue
	 *   a task to call the method in the event dispatch thread and return
	 *   true to signal bypassing the method following.
	 * Example:
	 * public void getSomething() {
	 *     if (guiUtils.forceDispatch(this, "getSomething"))
	 *             return;
	 *     // do something here
	 * }
	 */
	public static boolean forceDispatch(final Object callingFrom,
			final String methodName, final boolean now, final Object...args) {
		if (callingFrom == null)       throw new RequiredArgumentException("callingFrom");
		if (Utils.isEmpty(methodName)) throw new RequiredArgumentException("methodName");
		// already running from event dispatch thread
		if (SwingUtilities.isEventDispatchThread())
			return false;
		// queue to run in event dispatch thread
		final RemappedMethod<Object> run =
			new RemappedMethod<Object>(
				callingFrom,
				methodName,
				args
			);
		try {
			if (now) {
				SwingUtilities.invokeAndWait(run);
			} else {
				SwingUtilities.invokeLater(run);
			}
		} catch (InvocationTargetException e) {
			log().trace(e);
		} catch (InterruptedException e) {
			log().trace(e);
		}
		return true;
	}



	// logger
	public static xLog log() {
		return xLogRoot.get()
				.get(LOG_NAME);
	}



}
