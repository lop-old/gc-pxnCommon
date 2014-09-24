package com.poixson.commonjava.Utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.channels.FileLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.poixson.commonjava.xLogger.xLog;


public final class utilsDirFile {
	private utilsDirFile() {}



	// single instance lock
	@SuppressWarnings("resource")
	public static boolean lockInstance(final String fileStr) {
		try {
			final File lockFile = new File(fileStr);
			final RandomAccessFile randomAccessFile = new RandomAccessFile(lockFile, "rw");
			final FileLock fileLock = randomAccessFile.getChannel().tryLock();
			final int pid = getPid();
			if(pid > 0)
				randomAccessFile.write(Integer.toString(pid).getBytes());
			if(fileLock == null) {
				utils.safeClose(randomAccessFile);
				return false;
			}
			// register shutdown hook
			Runtime.getRuntime().addShutdownHook(new Thread() {
				private volatile File fle = null;
				private volatile RandomAccessFile acc = null;
				private volatile FileLock lck = null;
				public Thread init(final File file, final RandomAccessFile access, final FileLock lock) {
					this.fle = file;
					this.acc = access;
					this.lck = lock;
					return this;
				}
				@Override
				public void run() {
					try {
						this.lck.release();
						utils.safeClose(this.acc);
						this.fle.delete();
					} catch (Exception e) {
						log().trace(e);
//						pxnLog.get().severe("Unable to remove lock file: "+lockFile);
//						pxnLog.get().exception(e);
					}
				}
			}.init(lockFile, randomAccessFile, fileLock));
			return true;
		} catch (Exception e) {
			log().trace(e);
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
			log().trace(e);
		} catch (IOException e) {
			log().trace(e);
		}
		return -1;
	}



	// get current working directory
	public static String cwd() {
		try {
			return (new File(".")).getCanonicalPath().toString();
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
	public static File[] listContents(final File dir, final String[] extensions) {
		if(dir == null) throw new NullPointerException();
		if(!dir.isDirectory()) return null;
		return dir.listFiles(new FileFilter() {
			private String[] exts;
			public FileFilter init(final String[] extens) {
				this.exts = extens;
				return this;
			}
			@Override
			public boolean accept(File path) {
				if(this.exts == null) return true;
				final String pathStr = path.toString();
				for(final String ext : this.exts) {
					if(pathStr.endsWith(ext))
						return true;
				}
				return false;
			}
		}.init(extensions));
	}



	// add lib to paths
	public static void addLibraryPath(final String libDir) {
		if(utils.isEmpty(libDir)) throw new NullPointerException("libDir cannot be null/empty");
		// get lib path
		final File file = new File(libDir);
		if(!file.exists() || !file.isDirectory()) {
			log().warning("Library path not found: "+libDir);
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
		} catch (SecurityException ignore) {
		} catch (NoSuchFieldException ignore) {
		} catch (IllegalArgumentException ignore) {
		} catch (IllegalAccessException ignore) {
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
			log().trace(e);
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
	@SuppressWarnings("resource")
	public static InputJar OpenJarResource(final File jarFile, final String fileName) {
		if(jarFile == null)         throw new NullPointerException("jarFile cannot be null");
		if(utils.isEmpty(fileName)) throw new NullPointerException("fileName cannot be null/empty");
		try {
			final JarFile jar = new JarFile(jarFile);
			final JarEntry entry = jar.getJarEntry(fileName);
			if(entry == null) {
				utils.safeClose(jar);
				return null;
			}
			final InputStream fileInput = jar.getInputStream(entry);
			if(fileInput == null) {
				utils.safeClose(jar);
				return null;
			}
			return new InputJar(jar, fileInput);
		} catch (IOException ignore) {}
		return null;
	}
	public static class InputJar implements Closeable {
		public final JarFile jar;
		public final InputStream fileInput;
		public InputJar(final JarFile jar, final InputStream fileInput) {
			this.jar = jar;
			this.fileInput = fileInput;
		}
		@Override
		public void finalize() {
			close();
		}
		@Override
		public void close() {
			utils.safeClose(this.jar);
			utils.safeClose(this.fileInput);
		}
	}



	// these functions can have inconsistent results. a better class will be needed

	// build path+file
	public static String buildFilePath(final String filePath, final String fileName, final String extension) {
		if(utils.isEmpty(fileName)) throw new NullPointerException("fileName cannot be null/empty");
		// file extension
		final String ext;
		if(utils.isEmpty(extension))
			ext = ".yml";
		else if(!extension.startsWith("."))
			ext = "."+extension;
		else
			ext = extension;
		final String fileStr;
		if(!fileName.endsWith(ext))
			fileStr = fileName + ext;
		else
			fileStr = fileName;
		if(filePath == null || filePath.isEmpty())
			return fileStr;
		final boolean a = (filePath.endsWith("/")  || filePath.endsWith("\\"));
		final boolean b = (fileStr.startsWith("/") || fileStr.startsWith("\\"));
		if(a && b) return filePath + fileStr.substring(1);
		if(a || b) return filePath + fileStr;
		return filePath + File.separator + fileStr;
	}



	public static String mergePaths(final String...strings) {
		final StringBuilder merged = new StringBuilder();
		for(String path : strings) {
			if(utils.isEmpty(path)) continue;
			if(path.equals("."))
				path = cwd();
			else
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



	// logger
	public static xLog log() {
		return utils.log();
	}



}
