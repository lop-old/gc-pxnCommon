package com.poixson.commonjava.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.channels.FileLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public final class utilsDirFile {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	private utilsDirFile() {}


	// single instance lock
	public static boolean lockInstance(final String lockFile) {
		try {
			final File file = new File(lockFile);
			final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			final FileLock fileLock = randomAccessFile.getChannel().tryLock();
			final int pid = getPid();
			if(pid > 0)
				randomAccessFile.write(Integer.toString(pid).getBytes());
			if(fileLock == null)
				return false;
			// register shutdown hook
			Runtime.getRuntime().addShutdownHook(new Thread() {
				private volatile FileLock lock;
				public Thread init(final FileLock lock) {
					this.lock = lock;
					return this;
				}
				public void run() {
					try {
						lock.release();
						randomAccessFile.close();
						file.delete();
					} catch (Exception e) {
//						pxnLog.get().severe("Unable to remove lock file: "+lockFile);
//						pxnLog.get().exception(e);
					}
				}
			}.init(fileLock));
			return true;
		} catch (Exception e) {
//			pxnLog.get().severe("Unable to create and/or lock file: "+lockFile);
//			pxnLog.get().exception(e);
		}
		return false;
	}
	// get pid for process (if possible)
	public static int getPid() {
		try {
			return Integer.parseInt(
				(new File("/proc/self")).getCanonicalFile().getName()
			);
		} catch (NumberFormatException e) {
//			pxnLog.get().exception(e);
		} catch (IOException e) {
//			pxnLog.get().exception(e);
		}
		return -1;
	}


	// add lib to paths
	public static void addLibraryPath(final String libDir) {
		if(utils.isEmpty(libDir)) throw new NullPointerException("libDir cannot be null/empty");
		// get lib path
		final File file = new File(libDir);
		if(!file.exists() || !file.isDirectory()) {
System.out.println("Library path not found: "+libDir);
			return;
		}
		final String libPath = file.getAbsolutePath();
		if(utils.isEmpty(libPath)) return;
		// get current paths
		final String currentPaths = System.getProperty("java.library.path");
		if(currentPaths == null) return;
//		pxnLog.get().debug("Adding lib path: "+libDir);
		// set library paths
		if(currentPaths.isEmpty()) {
			System.setProperty("java.library.path", libPath);
		} else {
			if(currentPaths.contains(libPath)) return;
			System.setProperty("java.library.path", currentPaths+( currentPaths.contains(";") ? ";" : ":" )+libPath);
		}
		// force library paths to refresh
		try {
			final Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
		} catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
//			pxnLog.get().exception(e);
		}
	}



	// open file
	public static InputStream OpenFile(final String fileStr) {
		if(utils.isEmpty(fileStr)) return null;
		return OpenFile(new File(fileStr));
	}
	public static InputStream OpenFile(final File file) {
		if(file == null) return null;
		try {
			if(!file.exists()) throw new FileNotFoundException("File not found: "+file.getAbsoluteFile());
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
//			pxnLog.get().warning("Failed to load file: "+file.getAbsoluteFile());
		}
		return null;
	}
	// load resource
	public static InputStream OpenResource(final String fileStr) {
		if(utils.isEmpty(fileStr)) return null;
		try {
			return utilsDirFile.class.getResourceAsStream(fileStr);
		} catch(Exception ignore) {
//			pxnLog.get().debug("Not found as a resource!");
		}
		return null;
	}
	// load yml from jar
	public static InputJar OpenJarResource(final File jarFile, final String fileName) {
		if(jarFile == null) throw new NullPointerException("jarFile cannot be null");
		if(utils.isEmpty(fileName)) throw new NullPointerException("fileName cannot be null/empty");
		try {
			final JarFile jar = new JarFile(jarFile);
			final JarEntry entry = jar.getJarEntry(fileName);
			if(entry == null) return null;
			final InputStream fileInput = jar.getInputStream(entry);
			if(fileInput == null) return null;
			return new InputJar(jar, fileInput);
		} catch (IOException ignore) {}
		return null;
	}
	public static class InputJar {
		public final JarFile jar;
		public final InputStream fileInput;
		public InputJar(final JarFile jar, final InputStream fileInput) {
			this.jar = jar;
			this.fileInput = fileInput;
		}
		@Override
		public void finalize() {
			Close();
		}
		public void Close() {
			if(jar != null) {
				try {
					jar.close();
				} catch (IOException ignore) {}
//				jar = null;
			}
			if(fileInput != null) {
				try {
					fileInput.close();
				} catch (IOException ignore) {}
//				fileInput = null;
			}
		}
	}


	// build path+file
	public static String buildFilePath(final String filePath, String fileName, String ext) {
		if(utils.isEmpty(fileName)) throw new NullPointerException("fileName cannot be null/empty");
		// file extension
		if(utils.isEmpty(ext))
			ext = ".yml";
		if(!ext.startsWith("."))
			ext = "." + ext;
		if(!fileName.endsWith(ext))
			fileName += ext;
		if(filePath == null || filePath.isEmpty())
			return fileName;
		final boolean a = (filePath.endsWith("/")   || filePath.endsWith("\\"));
		final boolean b = (fileName.startsWith("/") || fileName.startsWith("\\"));
		if(a && b) return filePath + fileName.substring(1);
		if(a || b) return filePath + fileName;
		return filePath + File.separator + fileName;
	}


	public static String mergePaths(final String...strings) {
		final StringBuilder merged = new StringBuilder();
		for(String path : strings) {
			if(utils.isEmpty(path)) continue;
			if(path.startsWith("/") || path.startsWith("\\"))
				path = path.substring(1);
			if(path.endsWith("/") || path.endsWith("\\"))
				path = path.substring(0, -1);
			if(merged.length() > 0)
				merged.append(File.separatorChar);
			merged.append(path);
		}
		return merged.toString();
	}


	public static String san(final String text) {
		return utilsSan.FileName(text);
	}


}
