package com.poixson.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import com.poixson.tools.Keeper;


public final class ProcUtils {
	private ProcUtils() {}
	{ Keeper.add(new ProcUtils()); }

	private static volatile Boolean debugWireEnabled = null;

	private static volatile int pid = Integer.MIN_VALUE;



	public static boolean isDebugWireEnabled() {
		if (debugWireEnabled == null) {
			final RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
			if (bean == null) {
				debugWireEnabled = Boolean.FALSE;
				return false;
			}
			final List<String> args = bean.getInputArguments();
			if (args == null) {
				debugWireEnabled = Boolean.FALSE;
				return false;
			}
			final String argsStr = args.toString();
			if (argsStr.indexOf("jdwp") >= 0) {
				debugWireEnabled = Boolean.TRUE;
			} else {
				debugWireEnabled = Boolean.FALSE;
			}
		}
		return debugWireEnabled.booleanValue();
	}



	/**
	 * Get the pid for the jvm process.
	 * @return process id number (pid)
	 */
	public static int getPid() {
		if (pid == Integer.MIN_VALUE) {
			final RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
			if (bean == null) {
				pid = -1;
				return pid;
			}
			final String procName = bean.getName();
			if (Utils.isEmpty(procName)) {
				pid = -1;
				return pid;
			}
			final String[] parts = procName.split("@", 2);
			if (parts == null) {
				pid = -1;
				return pid;
			}
			if (parts.length != 2) {
				pid = -1;
				return pid;
			}
			pid = NumberUtils.toInteger(parts[0], -1);
		}
		return pid;
//TODO:
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
//			xLog.getRoot()
//				.trace(e);
//		}
//		return -1;
	}



//TODO:
/*
	// single instance lock
	public static boolean lockInstance(final String filepath) {
		String path = filepath;
		if (Utils.isEmpty(path)) {
			final xApp app = xApp.peek();
			if (app != null) {
				final String appName = app.getName();
				path = appName+".lock";
			}
		}
		if (Utils.isEmpty(path)) throw new RequiredArgumentException("path");
		final File file = new File(path);
		RandomAccessFile access = null;
		try {
			access = new RandomAccessFile(file, "rw");
			final FileLock lock = access.getChannel().tryLock();
			final int pid = getPid();
			if (pid > 0) {
				access.write(Integer.toString(pid).getBytes());
			} else {
				access.writeUTF("<PID>");
			}
			if (lock == null) {
				Utils.safeClose(access);
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
			final xLog log = xLog.getRoot();
			log.severe("Unable to create or lock file: "+file.toString());
			log.severe("File may already be locked!");
			return false;
		} catch (Exception e) {
			final xLog log = xLog.getRoot();
			log.severe("Unable to create or lock file: "+file.toString());
			log.trace(e);
		} finally {
			Utils.safeClose(access);
		}
		return false;
	}



	protected static class LockFileReleaseThread extends Thread {

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
				final xLog log = xLog.getRoot();
				log.severe("Unable to release lock file: "+this.file.toString());
				log.trace(e);
			}
		}

	}
*/



}
