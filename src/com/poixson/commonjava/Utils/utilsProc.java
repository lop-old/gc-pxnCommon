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
		final java.lang.management.RuntimeMXBean runtime =
				java.lang.management.ManagementFactory.getRuntimeMXBean();
		final java.lang.reflect.Field jvm;
		try {
			jvm = runtime.getClass().getDeclaredField("jvm");
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			return -1;
		}
		jvm.setAccessible(true);
		final sun.management.VMManagement mgmt;
		try {
			mgmt = (sun.management.VMManagement) jvm.get(runtime);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return -1;
		}
		final java.lang.reflect.Method pid_method;
		try {
			pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return -1;
		}
		pid_method.setAccessible(true);
		final int pid;
		try {
			pid = (int) pid_method.invoke(mgmt);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return -1;
		}
		return pid;
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
				public Thread init(final File file,
						final RandomAccessFile access, final FileLock lock) {
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
