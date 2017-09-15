package com.poixson.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLog;


public final class NativeUtils {
	private NativeUtils() {}



	public static boolean LoadLibrary(final String fileName) {
		return LoadLibrary(
			null,
			fileName
		);
	}
	public static boolean LoadLibrary(
			final String dirStr, final String fileName) {
		try {
			log().finest("Loading library.. {}", fileName);
			final String pathStr =
				FileUtils.MergePaths(
					(Utils.isEmpty(dirStr) ? "." : dirStr),
					fileName
				);
			log().detail("LoadLibrary::pathStr = {}", pathStr);
			System.load(pathStr);
		} catch (SecurityException e) {
			log().trace(e);
			return false;
		} catch (UnsatisfiedLinkError e) {
			log().trace(e);
			return false;
		}
		return true;
	}



	public static boolean ExtractLibrary(
			final String outputDir, final String resourcePath,
			final String fileName, final Class<?> classRef) {
		if (Utils.isEmpty(fileName)    ) throw new RequiredArgumentException("fileName");
		if (classRef == null           ) throw new RequiredArgumentException("classRef");
		final File file =
			new File(
				FileUtils.MergePaths(
					(Utils.isEmpty(outputDir) ? "." : outputDir),
					fileName
				)
			);
		if (file.exists() && file.isFile()) {
			log().info("Removing existing library file: {}", fileName);
			file.delete();
		}
		InputStream      in  = null;
		FileOutputStream out = null;
		try {
			final String resPath =
				FileUtils.MergePaths(
					(Utils.isEmpty(resourcePath) ? "/" : resourcePath),
					fileName
				);
			in = classRef
				.getResourceAsStream(
					resPath
				);
			if (in == null)
				return false;
			out = new FileOutputStream(file);
			byte[] buf = new byte[4096];
			while (true) {
				final int read = in.read(buf);
				if (read == -1)
					break;
				out.write(buf, 0, read);
			}
			log().info("Extracted library file: {}", fileName);
		} catch (FileNotFoundException e) {
			log().trace(e);
			if (file.isFile()) {
				file.delete();
			}
			return false;
		} catch (IOException e) {
			log().trace(e);
			if (file.isFile()) {
				file.delete();
			}
			return false;
		} finally {
			Utils.safeClose(out);
			Utils.safeClose(in);
		}
		return true;
	}



	public static boolean LoadExtractLibrary(
			final String fileName) {
		return LoadExtractLibrary(
			null,
			null,
			fileName,
			null
		);
	}
	public static boolean LoadExtractLibrary(
			final String fileName, final Class<?> classRef) {
		return LoadExtractLibrary(
			null,
			null,
			fileName,
			classRef
		);
	}
	public static boolean LoadExtractLibrary(
			final String outputDir,
			final String fileName, final Class<?> classRef) {
		return LoadExtractLibrary(
			outputDir,
			null,
			fileName,
			classRef
		);
	}
	public static boolean LoadExtractLibrary(
			final String outputDir, final String resourcePath,
			final String fileName, final Class<?> classRef) {
//TODO: is this useful?
//String jsscLibPath = System.getProperty("jssc.library.path");
		final String pathStr =
			FileUtils.MergePaths(
				(Utils.isEmpty(outputDir) ? "." : outputDir),
				fileName
			);
		log().detail("LoadExtractLibrary::pathStr = {}", pathStr);
		final File file = new File(pathStr);
		// load existing library file
		if (file.exists() && file.isFile()) {
			log().finest("Found library: {}", fileName);
			return LoadLibrary(
				outputDir,
				fileName
			);
		}
		// extract library file from jar
		{
			final boolean result =
				ExtractLibrary(
					outputDir,
					resourcePath,
					fileName,
					classRef
				);
			if (!result)
				return false;
		}
		// load extracted library
		{
			final boolean result =
				LoadLibrary(
					outputDir,
					fileName
				);
			if (!result)
				return false;
		}
		return true;
	}



	public static xLog log() {
		return xLog.getRoot();
	}



}
