package com.poixson.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.yaml.snakeyaml.Yaml;

import com.poixson.utils.exceptions.RequiredArgumentException;


public final class ioUtils {
	private ioUtils() {}



	/**
	 * Open a file and provide the InputStream.
	 * @param fileStr Path to the file.
	 * @return InputStream of the open file, or null on failure.
	 * @throws FileNotFoundException
	 */
	public static InputStream OpenFile(final String fileStr)
			throws FileNotFoundException {
		if (Utils.isEmpty(fileStr)) throw new RequiredArgumentException("fileStr");
		final File file = new File(fileStr);
		if (!file.exists()) return null;
		final InputStream in =
			new FileInputStream(file);
		return in;
	}



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
		if (Utils.isEmpty(targetFileStr))
			throw new RequiredArgumentException("outputFileStr");
		if (in == null) throw new RequiredArgumentException("in");
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



	/**
	 * Load and parse yaml data from an input stream. 
	 * @param in InputStream to read from.
	 * @return Map<String, Object> datamap contents of yml file.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> LoadYamlFromStream(final InputStream in) {
		if (in == null) throw new RequiredArgumentException("in");
		final Yaml yml = new Yaml();
		final Map<String, Object> datamap =
			yml.loadAs(in, Map.class);
		return datamap;
	}



}
