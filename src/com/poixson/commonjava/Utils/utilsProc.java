package com.poixson.commonjava.Utils;

import java.lang.management.ManagementFactory;


public final class utilsProc {



	public static void init() {
		Keeper.add(new utilsProc());
	}
	private utilsProc() {}



	/**
	 * Get the pid for the jvm process.
	 * @return process id number (pid)
	 */
	// @ SuppressWarnings("restriction")
	public static int getPid() {
		final String str = ManagementFactory.getRuntimeMXBean().getName();
		if(utils.isEmpty(str))
			return -1;
		final String[] parts = str.split("@", 2);
		if(parts == null)
			return -1;
		if(parts.length != 2)
			return -1;
		final int pid = utilsNumbers.toInteger(parts[0], -1);
		return pid;
//another option to try
//final int pid = Integer.parseInt(new File("/proc/self").getCanonicalFile().getName());
//original
//		try {
//			final java.lang.management.RuntimeMXBean runtime =
//					java.lang.management.ManagementFactory.getRuntimeMXBean();
//			final java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
//			jvm.setAccessible(true);
//			final sun.management.VMManagement mgmt =
//					(sun.management.VMManagement) jvm.get(runtime);
//			final java.lang.reflect.Method pid_method =
//					mgmt.getClass().getDeclaredMethod("getProcessId");
//			pid_method.setAccessible(true);
//			return (int) pid_method.invoke(mgmt);
//		} catch (IllegalAccessException | IllegalArgumentException
//				| InvocationTargetException | NoSuchFieldException
//				| SecurityException | NoSuchMethodException e) {
//			xLog.getRoot().trace(e);
//		}
//		return -1;
	}



/*
	// single instance lock
	public static boolean lockInstance(final String filepath) {
		String path = filepath;
		if(utils.isEmpty(path)) {
			final xApp app = xApp.peak();
			if(app != null) {
				final String appName = app.getName();
				path = appName+".lock";
			}
		}
		if(utils.isEmpty(path))
			throw new NullPointerException("filepath argument is required!");
		final File file = new File(path);
		RandomAccessFile access = null;
		try {
			access = new RandomAccessFile(file, "rw");
			final FileLock lock = access.getChannel().tryLock();
			final int pid = getPid();
			if(pid > 0)
				access.write(Integer.toString(pid).getBytes());
			else
				access.writeUTF("<PID>");
			if(lock == null) {
				utils.safeClose(access);
				return false;
			}
			// register shutdown hook
			Runtime.getRuntime().addShutdownHook(
					new LockFileReleaseThread(
							file,
							access
					)
			);
			return true;
		} catch (OverlappingFileLockException e) {
			xLog.getRoot().severe("Unable to create or lock file: "+file.toString());
			xLog.getRoot().severe("File may already be locked!");
			return false;
		} catch (Exception e) {
			xLog.getRoot().severe("Unable to create or lock file: "+file.toString());
			xLog.getRoot().trace(e);
		} finally {
			utils.safeClose(access);
		}
		return false;
	}



	static class LockFileReleaseThread extends Thread {

		private final File file;
		private final RandomAccessFile access;

		public LockFileReleaseThread(final File file,
				final RandomAccessFile access) {
			this.file   = file;
			this.access = access;
			this.setName("LockFileRelease");
		}

		@Override
		public void run() {
			try {
				utils.safeClose(this.access);
				this.file.delete();
			} catch (Exception e) {
				xLog.getRoot().severe("Unable to release lock file: "+this.file.toString());
				xLog.getRoot().trace(e);
			}
		}

	}
*/



}
