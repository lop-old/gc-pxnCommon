package com.poixson.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.poixson.utils.exceptions.RequiredArgumentException;


public final class ioUtils {
	private ioUtils() {}



	// open file
	public static InputStream OpenFile(final String fileStr) {
		if (Utils.isEmpty(fileStr))
			return null;
		return OpenFile(
				new File(fileStr)
		);
	}
	public static InputStream OpenFile(final File file) {
		if (file == null)   return null;
		if (!file.exists()) return null;
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException ignore) {}
		return null;
	}



	// load resource
	public static InputStream OpenResource(final String fileStr) {
		if (Utils.isEmpty(fileStr)) return null;
		try {
			return DirsFiles.class.getResourceAsStream(
				StringUtils.ForceStarts("/", fileStr)
			);
		} catch(Exception ignore) {}
		return null;
	}
	// load resource from jar reference
	public static InputStream OpenResource(final Class<? extends Object> clss, final String fileStr) {
		if (clss == null)           throw new RequiredArgumentException("clss");
		if (Utils.isEmpty(fileStr)) throw new RequiredArgumentException("fileStr");
		final InputStream in =
			clss.getResourceAsStream(
				StringUtils.ForceStarts(
					"/",
					fileStr
				)
			);
		return in;
	}
//TODO: is this useful?
/*
	// load yml from jar
	public static InputJar OpenResource(final File jarFile, final String fileStr) {
		if (jarFile == null)        throw new RequiredArgumentException("jarFile");
		if (Utils.isEmpty(fileStr)) throw new RequiredArgumentException("fileStr");
		try {
			final JarFile  jar   = new JarFile(jarFile);
			final JarEntry entry = jar.getJarEntry(fileStr);
			if (entry == null) {
				Utils.safeClose(jar);
				return null;
			}
			final InputStream in = jar.getInputStream(entry);
			if (in == null) {
				Utils.safeClose(jar);
				return null;
			}
			return new InputJar(jar, in);
		} catch (IOException ignore) {}
		return null;
	}
	public static class InputJar implements Closeable {
		public final JarFile jar;
		public final InputStream fileInput;
		public InputJar(final JarFile jar, final InputStream fileInput) {
			this.jar       = jar;
			this.fileInput = fileInput;
		}
		@Override
		public void finalize() {
			this.close();
		}
		@Override
		public void close() {
			Utils.safeClose(this.jar);
			Utils.safeClose(this.fileInput);
		}
	}
*/



}
