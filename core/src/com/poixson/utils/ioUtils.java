package com.poixson.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.poixson.exceptions.RequiredArgumentException;


public final class ioUtils {
	private ioUtils() {}



	/**
	 * Open a resource file which has been compiled into the app jar.
	 * @param clss Reference class contained in the same jar.
	 * @param fileStr Package path to the file.
	 * @return InputStream of the open file, or null on failure.
	 */
	public static InputStream OpenResource(final Class<? extends Object> clssRef, final String fileStr) {
		if (Utils.isEmpty(fileStr)) throw new RequiredArgumentException("fileStr");
		final Class<? extends Object> clss = (
			clssRef == null
			? ioUtils.class
			: clssRef
		);
		final InputStream in =
			clss.getResourceAsStream(
				StringUtils.ForceStarts("/", fileStr)
			);
		return in;
	}



	/**
	 * Open a file contained in an external jar.
	 * @param jarFile The jar file containing the file to open.
	 * @param fileStr Package path to the file.
	 * @return InputStream of the open file, or null on failure.
	 * note: The jarFile object must be closed when no longer needed.
	 * @throws IOException
	 */
	public static InputStream OpenFileFromJar(final JarFile jarFile, final String fileStr)
			throws IOException {
		if (jarFile == null)        throw new RequiredArgumentException("jarFile");
		if (Utils.isEmpty(fileStr)) throw new RequiredArgumentException("fileStr");
		final JarEntry jarEntry =
			jarFile.getJarEntry(fileStr);
		if (jarEntry == null)
			return null;
		final InputStream in =
			jarFile.getInputStream(jarEntry);
		return in;
	}



	// copy jar resource to file
	public static void ExportResource(
			final String targetFileStr, final InputStream in)
			throws IOException {
		if (Utils.isEmpty(targetFileStr)) throw new RequiredArgumentException("targetFileStr");
		if (in == null)                   throw new RequiredArgumentException("in");
		final File file = new File(targetFileStr);
		try {
			Files.copy(
				in,
				file.toPath()
			);
		} finally {
			Utils.safeClose(in);
		}
	}



}
