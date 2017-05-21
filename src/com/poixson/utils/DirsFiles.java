package com.poixson.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.ref.SoftReference;

import com.poixson.utils.exceptions.RequiredArgumentException;


public final class DirsFiles {
	private DirsFiles() {}

	private static volatile SoftReference<String> cwd = null;


	public static void init() {
		Keeper.add(new DirsFiles());
	}



	// get current working directory
	public static String cwd() {
		if (cwd != null) {
			final SoftReference<String> ref = cwd;
			if (ref != null) {
				final String path = ref.get();
				if (Utils.notEmpty(path))
					return path;
			}
		}
		String path = null;
		try {
			path =
				(new File("."))
					.getCanonicalPath()
					.toString();
		} catch (IOException ignore) {}
		if (path != null) {
			cwd = new SoftReference<String>(path);
			return path;
		}
		return null;
	}



	/**
	 * List contents of a directory.
	 * @param dir The directory path to query.
	 * @param extensions File extensions to include, filtering out all others.
	 * To list all contents, set this to null.
	 * @return
	 */
	public static File[] ListDirContents(final File dir, final String[] extensions) {
		if (dir == null) throw new RequiredArgumentException("dir");
		if (!dir.isDirectory()) return null;
		return dir.listFiles(
			new FileFilter() {
				private String[] exts;
				public FileFilter init(final String[] extens) {
					this.exts = extens;
					return this;
				}
				@Override
				public boolean accept(File path) {
					if (this.exts == null)
						return true;
					final String pathStr = path.toString();
					for (final String ext : this.exts){
						if (pathStr.endsWith(ext))
							return true;
					}
					return false;
				}
			}.init(extensions)
		);
	}
	public static File[] ListDirContents(final File dir, final String extension) {
		return ListDirContents(dir, new String[] {extension});
	}
	public static File[] ListDirContents(final File dir) {
		return ListDirContents(dir, (String[]) null);
	}



/*
//TODO: does this work?
	// add lib to paths
	public static void addLibraryPath(final String libDir) {
		if (utils.isEmpty(libDir)) throw new RequiredArgumentException("libDir");
		// get lib path
		final File file = new File(libDir);
		if (!file.exists() || !file.isDirectory()) {
			log().warning("Library path not found: "+libDir);
			return;
		}
		final String libPath = file.getAbsolutePath();
		if (utils.isEmpty(libPath)) return;
		// get current paths
		final String currentPaths = System.getProperty("java.library.path");
		if (currentPaths == null) return;
//		pxnLog.get().debug("Adding lib path: "+libDir);
		// set library paths
		if (currentPaths.isEmpty()) {
			System.setProperty("java.library.path", libPath);
		} else {
			if (currentPaths.contains(libPath)) return;
			System.setProperty("java.library.path", currentPaths+( currentPaths.contains(";") ? ";" : ":" )+libPath);
		}
		// force library paths to refresh
		try {
			final Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
		} catch (SecurityException ignore) {
		} catch (NoSuchFieldException ignore) {
		} catch (IllegalArgumentException ignore) {
		} catch (IllegalAccessException ignore) {
		}
	}
*/



// TODO: these functions can have inconsistent results. a better class will be needed



	// build path+file+ext
	public static String buildFilePath(final String pathStr,
			final String fileName, final String extension) {
		if (Utils.isEmpty(fileName)) throw new RequiredArgumentException("fileName");
		// file extension
		String ext = "";
		if (Utils.notEmpty(extension)) {
			ext = (
				extension.startsWith(".")
				? extension
				: "."+extension
			);
		}
		final String fileStr = StringUtils.ForceEnds(ext, fileName);
		if (Utils.isEmpty(pathStr))
			return fileStr;
		final boolean a = ( pathStr.endsWith("/")   || pathStr.endsWith("\\")   );
		final boolean b = ( fileStr.startsWith("/") || fileStr.startsWith("\\") );
		if (a && b) return pathStr + fileStr.substring(1);
		if (a || b) return pathStr + fileStr;
		return
			(new StringBuilder())
				.append(pathStr)
				.append(File.separator)
				.append(fileStr)
				.toString();
	}



	public static String mergePaths(final String...strings) {
		final StringBuilder merged = new StringBuilder();
		for (String path : strings) {
			if (Utils.isEmpty(path)) continue;
			if (path.equals(".")) {
				path = cwd();
			}
			path = StringUtils.trims(path, "/", "\\", " ", "\t", "\r", "\n");
			if (Utils.isEmpty(path))
				continue;
			merged
				.append(path)
				.append(File.separatorChar);
		}
		if (merged.length() == 0)
			return null;
		return merged.toString();
	}



//	// logger
//	public static xLog log() {
//		return Utils.log();
//	}



}
