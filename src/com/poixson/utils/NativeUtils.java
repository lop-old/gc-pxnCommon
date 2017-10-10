package com.poixson.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLevel;
import com.poixson.utils.xLogger.xLog;


public final class NativeUtils {
	private NativeUtils() {}

	private static final int EXTRACT_BUFFER_SIZE = 4096;



	public static void LoadLibrary(final String...filePath)
			throws SecurityException, UnsatisfiedLinkError {
		final String pathStr =
			FileUtils.MergePaths(filePath);
		if (log().isLoggable(xLevel.DETAIL)) {
			log().detail(
				"NativeUtils::LoadLibrary(path={})",
				pathStr
			);
		}
		System.load(pathStr);
	}



	public static void ExtractLibrary(
			final String outputDir, final String resourcePath,
			final String fileName, final Class<?> classRef)
			throws IOException {
		if (Utils.isEmpty(fileName)) throw new RequiredArgumentException("fileName");
		if (classRef == null       ) throw new RequiredArgumentException("classRef");
		// prepare paths
		final String resPath =
			FileUtils.MergePaths(
				resourcePath,
				fileName
			);
		final String outFilePath =
			FileUtils.MergePaths(
				outputDir,
				fileName
			);
		final File outFile = new File(outFilePath);
		InputStream      in  = null;
		FileOutputStream out = null;
		// open resource
		in = classRef.getResourceAsStream(resPath);
		if (in == null)
			throw new IOException("Resource file not found: "+resPath);
		if (outFile.isFile()) {
			log().info("Removing existing library file: {}", outFilePath);
			if (!outFile.delete())
				throw new IOException("Failed to remove library file: "+outFilePath);
		}
		// write file
		try {
			out = new FileOutputStream(outFile);
			byte[] buf = new byte[EXTRACT_BUFFER_SIZE];
			while (true) {
				final int read = in.read(buf);
				if (read == -1)
					break;
				out.write(buf, 0, read);
			}
			log().info("Extracted library file: {}", outFilePath);
		} catch (FileNotFoundException e) {
			throw new IOException("Cannot write to file: "+outFilePath, e);
		} finally {
			Utils.safeClose(out);
			Utils.safeClose(in);
		}
	}



	public static xLog log() {
		return
			xLog.getRoot()
				.get("LibUtils");
	}



}
