package com.poixson.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;

import com.poixson.utils.exceptions.RequiredArgumentException;


public final class FileUtils {
	private FileUtils() {}
	{ Keeper.add(new FileUtils()); }

	private static volatile String cwd = null;



	// get current working directory
	public static String cwd() {
		if (Utils.notEmpty(cwd))
			return cwd;
		{
			final String path = System.getProperty("user.dir");
			if (Utils.notEmpty(path)) {
				cwd = path;
				return path;
			}
		}
		try {
			final File dir = new File(".");
			cwd = dir.getCanonicalPath();
		} catch (IOException ignore) {
			cwd = null;
		}
		return cwd;
	}



	public static boolean isDir(final String pathStr) {
		if (Utils.isEmpty(pathStr))
			return false;
		final File path = new File(pathStr);
		return (
			path.exists() &&
			path.isDirectory()
		);
	}
	public static boolean isFile(final String fileStr) {
		if (Utils.isEmpty(fileStr))
			return false;
		final File file = new File(fileStr);
		return (
			file.exists() &&
			file.isFile()
		);
	}
	public static boolean isReadable(final String pathStr) {
		if (Utils.isEmpty(pathStr))
			return false;
		final File path = new File(pathStr);
		return (
			path.exists() &&
			path.canRead()
		);
	}
	public static boolean isWritable(final String pathStr) {
		if (Utils.isEmpty(pathStr))
			return false;
		final File path = new File(pathStr);
		return (
			path.exists() &&
			path.canWrite()
		);
	}



	/**
	 * List contents of a directory.
	 * @param dir The directory path to query.
	 * @param extensions File extensions to include, filtering out all others.
	 * To list all contents, set this to null.
	 * @return
	 */
	public static File[] ListDirContents(final File dir, final String[] extensions) {
		if (dir == null) throw RequiredArgumentException.getNew("dir");
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
		if (utils.isEmpty(libDir)) throw RequiredArgumentException.getNew("libDir");
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



/*
	// build path+file+ext
	public static String BuildFilePath(final String pathStr,
			final String fileName, final String extension) {
		if (Utils.isEmpty(fileName)) throw RequiredArgumentException.getNew("fileName");
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
				.append(File.separatorChar)
				.append(fileStr)
				.toString();
	}
*/



	public static String MergePaths(final String...strings) {
		if (strings.length == 0)
			return null;
		boolean isAbsolute = false;
		// maintain absolute
		for (int index=0; index<strings.length; index++) {
			if (Utils.isEmpty(strings[index])) continue;
			if (strings[index].startsWith("/")
			||  strings[index].startsWith("\\")) {
				isAbsolute = true;
			}
			break;
		}
		// split further
		final LinkedList<String> list = new LinkedList<String>();
		int count = 0;
		for (int index=0; index<strings.length; index++) {
			final String[] strs =
				StringUtils.SplitByDelims(strings[index], "/", "\\");
			// remove nulls/blanks
			for (final String str : strs) {
				if (Utils.isEmpty(str)) continue;
				final String s =
					StringUtils.Trim(
						str,
						" ", "\t", "\r", "\n"
					);
				if (count > 0) {
					if (".".equals(s)) continue;
				}
				if (Utils.isEmpty(s)) continue;
				list.add(s);
				count++;
			}
		}
		if (list.isEmpty())
			return null;
		final String first = list.getFirst();
		// relative to absolute
		if (".".equals(first)) {
			list.removeFirst();
			isAbsolute = true;
			// prepend cwd
			final String[] cwdArray =
				StringUtils.SplitByDelims(cwd(), "/", "\\");
			for (int index=cwdArray.length-1; index>=0; index--) {
				list.addFirst(cwdArray[index]);
			}
		}
		// resolve ..
		for (int index=0; index<list.size(); index++) {
			final String entry = list.get(index);
			if ("..".equals(entry)) {
				list.remove(index);
				if (index > 0) {
					index--;
					list.remove(index);
				}
				index--;
			}
		}
		// build path
		final String path =
			StringUtils.AddStrings(
				File.separator,
				list.toArray(new String[0])
			);
		if (isAbsolute) {
			return
				StringUtils.ForceStarts(
					File.separator,
					path
				);
		}
		return path;
	}



}
