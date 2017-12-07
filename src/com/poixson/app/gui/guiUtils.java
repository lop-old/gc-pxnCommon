package com.poixson.app.gui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.exceptions.RequiredArgumentException;
import com.poixson.commonjava.xLogger.xLog;


public final class guiUtils {
	private guiUtils() {}
	private static final String LOG_NAME = "GUI";



	// load image file/resource
	public static ImageIcon loadImageResource(final String path) {
		// open file
		final File file = new File(path);
		if (file.exists()) {
			try {
				final ImageIcon image = new ImageIcon(path);
				log().finer("Loaded image file: "+path);
				return image;
			} catch(Exception ignore) {}
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
			final String callingMethod, final Object...args) {
		if (callingFrom == null)          throw new RequiredArgumentException("callingFrom");
		if (utils.isEmpty(callingMethod)) throw new RequiredArgumentException("callingMethod");
		// already running from event dispatch thread
		if (SwingUtilities.isEventDispatchThread()) {
			return false;
		}
		// get calling class
		final Class<?> clss = callingFrom.getClass();
		// get calling method
		final Method method;
		try {
			switch (args.length) {
			case 0:
				method = clss.getMethod(
					callingMethod
				);
				break;
			case 1:
				method = clss.getMethod(
					callingMethod,
					args[0].getClass()
				);
				break;
			case 2:
				method = clss.getMethod(
					callingMethod,
					args[0].getClass(),
					args[1].getClass()
				);
				break;
			case 3:
				method = clss.getMethod(
					callingMethod,
					args[0].getClass(),
					args[1].getClass(),
					args[2].getClass()
				);
				break;
			case 4:
				method = clss.getMethod(
					callingMethod,
					args[0].getClass(),
					args[1].getClass(),
					args[2].getClass(),
					args[3].getClass()
				);
				break;
			default:
				throw new IllegalArgumentException("Too many arguments");
			}
		} catch (NoSuchMethodException e) {
			log()
				.trace(e);
			throw new IllegalArgumentException("Method not found");
		}
		// pass to dispatch thread
		try {
			SwingUtilities.invokeAndWait(
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
							switch (this.a.length) {
							case 0:
								this.m.invoke(
									this.o
								);
								break;
							case 1:
								this.m.invoke(
									this.o,
									this.a[0]
								);
								break;
							case 2:
								this.m.invoke(
									this.o,
									this.a[0],
									this.a[1]
								);
								break;
							case 3:
								this.m.invoke(
									this.o,
									this.a[0],
									this.a[1],
									this.a[2]
								);
								break;
							case 4:
								this.m.invoke(
									this.o,
									this.a[0],
									this.a[1],
									this.a[2],
									this.a[3]
								);
								break;
							default:
								throw new IllegalArgumentException("Too many arguments");
							}
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
				}.init(callingFrom, method, args)
			);
		} catch (InvocationTargetException e) {
			log().trace(e);
		} catch (InterruptedException e) {
			log().trace(e);
		}
		return true;
	}



	// logger
	public static xLog log() {
		return xLog.getRoot(LOG_NAME);
	}



}
