package com.poixson.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import com.poixson.utils.exceptions.RequiredArgumentException;


public final class FileUtils {
	private FileUtils() {}
	{ Keeper.add(new FileUtils()); }

	private static volatile String cwd = null;



	// get current working directory
	public static String cwd() {
		if (Utils.notEmpty(cwd))
			return cwd;
		try {
			cwd =
				(new File("."))
					.getCanonicalPath()
					.toString();
		} catch (IOException ignore) {}
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
	public static String BuildFilePath(final String pathStr,
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



	public static String MergePaths(final String...strings) {
		final StringBuilder merged = new StringBuilder();
		if (strings.length > 0) {
			if (".".equals(strings[0])) {
				merged
					.append(cwd())
					.append(File.separatorChar);
			}
		}
		for (String path : strings) {
			if (Utils.isEmpty(path)) continue;
			if (path.equals("."))    continue;
			path = StringUtils.trims(path, "/", "\\", " ", "\t", "\r", "\n");
			if (Utils.isBlank(path)) continue;
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
