package com.poixson.commonjava.Utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileLock;

import com.poixson.commonjava.xLogger.xLog;


public final class utilsProc {
	private utilsProc() {}



	/**
	 * Get the pid for the jvm process.
	 * @return process id number (pid)
	 */
	public static int getPid() {
		try {
			final java.lang.management.RuntimeMXBean runtime =
					java.lang.management.ManagementFactory.getRuntimeMXBean();
			final java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
			jvm.setAccessible(true);
			final sun.management.VMManagement mgmt =
					(sun.management.VMManagement) jvm.get(runtime);
			final java.lang.reflect.Method pid_method =
					mgmt.getClass().getDeclaredMethod("getProcessId");
			pid_method.setAccessible(true);
			return (int) pid_method.invoke(mgmt);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchFieldException
				| SecurityException | NoSuchMethodException e) {
			xLog.getRoot().trace(e);
		}
		return -1;
	}



	// single instance lock
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
					this.setName("LockFileRelease");
					return this;
				}
				@Override
				public void run() {
					try {
						this.lck.release();
						utils.safeClose(this.acc);
						this.fle.delete();
					} catch (Exception e) {
						xLog.getRoot().trace(e);
//						pxnLog.get().severe("Unable to remove lock file: "+lockFile);
//						pxnLog.get().exception(e);
					}
				}
			}.init(lockFile, randomAccessFile, fileLock));
			return true;
		} catch (Exception e) {
			xLog.getRoot().trace(e);
//			pxnLog.get().severe("Unable to create and/or lock file: "+lockFile);
//			pxnLog.get().exception(e);
		}
		return false;
	}



}
