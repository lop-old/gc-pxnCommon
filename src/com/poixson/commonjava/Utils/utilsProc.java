package com.poixson.commonjava.Utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
			return Integer.parseInt(
				(new File("/proc/self")).getCanonicalFile().getName()
			);
		} catch (NumberFormatException | IOException e) {
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
