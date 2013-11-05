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
			int pid = getPid();
			if(pid > 0)
				randomAccessFile.write(Integer.toString(pid).getBytes());
			if(fileLock == null)
				return false;
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						fileLock.release();
						randomAccessFile.close();
						file.delete();
					} catch (Exception e) {
//						pxnLog.get().severe("Unable to remove lock file: "+lockFile);
//						pxnLog.get().exception(e);
					}
				}
			});
			return true;
		} catch (Exception e) {
//			pxnLog.get().severe("Unable to create and/or lock file: "+lockFile);
//			pxnLog.get().exception(e);
		}
		return false;
	}
	// get pid for process (if possible)
	public static int getPid() {
		int pid = -1;
		try {
			pid = Integer.parseInt( ( new File("/proc/self")).getCanonicalFile().getName() );
		} catch (NumberFormatException e) {
//			pxnLog.get().exception(e);
		} catch (IOException e) {
//			pxnLog.get().exception(e);
		}
		return pid;
	}


	// add lib to paths
	public static void addLibraryPath(String libDir) {
		if(libDir == null) throw new NullPointerException("libDir cannot be null");
		// get lib path
		File file = new File(libDir);
		if(file==null || !file.exists() || !file.isDirectory()) return;
		String libPath = file.getAbsolutePath();
		if(libPath == null || libPath.isEmpty()) return;
		// get current paths
		String currentPaths = System.getProperty("java.library.path");
		if(currentPaths == null) return;
//		pxnLog.get().debug("Adding lib path: "+libDir);
		// set library paths
		if(currentPaths.isEmpty()) {
			System.setProperty("java.library.path", libPath);
		} else {
			if(currentPaths.contains(libPath)) return;
			System.setProperty("java.library.path", currentPaths+(currentPaths.contains(";")?";":":")+libPath);
		}
		// force library paths to refresh
		try {
			Field fieldSysPath;
			fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
		} catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
//			pxnLog.get().exception(e);
		}
	}



	// open file
	public static InputStream OpenFile(String fileStr) {
		if(fileStr == null | fileStr.isEmpty()) return null;
		return OpenFile(new File(fileStr));
	}
	public static InputStream OpenFile(File file) {
		if(file == null) return null;
		try {
			if(!file.exists()) throw new FileNotFoundException("File not found! "+file.getAbsoluteFile());
			return new FileInputStream(file);
		} catch (FileNotFoundException ignore) {
//			pxnLog.get().warning("Failed to load config file: "+file.getAbsoluteFile());
		}
		return null;
	}
	// load resource
	public static InputStream OpenResource(String fileStr) {
		if(fileStr == null || fileStr.isEmpty()) return null;
		try {
			return utilsDirFile.class.getResourceAsStream(fileStr);
		} catch(Exception ignore) {
//			pxnLog.get().debug("Not found as a resource!");
		}
		return null;
	}
	// load yml from jar
	public static InputJar OpenJarResource(File jarFile, String fileName) {
		if(jarFile  == null)   throw new NullPointerException("jarFile cannot be null!");
		if(fileName == null)   throw new NullPointerException("fileName cannot be null!");
		if(fileName.isEmpty()) throw new NullPointerException("fileName cannot be empty!");
		JarFile jar = null;
		InputStream fileInput = null;
		try {
			jar = new JarFile(jarFile);
			JarEntry entry = jar.getJarEntry(fileName);
			if(entry != null)
				fileInput = jar.getInputStream(entry);
		} catch (IOException ignore) {}
		if(fileInput == null)
			return null;
		return new InputJar(jar, fileInput);
	}
	public static class InputJar {
		public JarFile jar;
		public InputStream fileInput;
		public InputJar(JarFile jar, InputStream fileInput) {
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
				jar = null;
			}
			if(fileInput != null) {
				try {
					fileInput.close();
				} catch (IOException ignore) {}
				fileInput = null;
			}
		}
	}


	// build path+file
	public static String BuildFilePath(String filePath, String fileName) {
		if(fileName == null)   throw new NullPointerException("fileName cannot be null!");
		if(fileName.isEmpty()) throw new NullPointerException("fileName cannot be empty!");
		if(!fileName.endsWith(".yml")) fileName += ".yml";
		if(filePath == null || filePath.isEmpty())
			return fileName;
		if(filePath.endsWith("/") || filePath.endsWith("\\") || fileName.startsWith("/") || fileName.startsWith("\\"))
			return filePath+fileName;
		return filePath+File.separator+fileName;
	}


	public static String mergePaths(String... strings) {
		StringBuilder merged = new StringBuilder();
		for(String path : strings) {
			if(path == null || path.isEmpty()) continue;
			if(merged.length() > 0)
				merged.append(File.separatorChar);
			merged.append(path);
		}
		return merged.toString();
	}


}
