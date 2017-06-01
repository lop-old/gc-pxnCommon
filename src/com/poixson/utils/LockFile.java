package com.poixson.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.SoftReference;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLog;


public class LockFile {

	private static final ConcurrentMap<String, LockFile> instances =
			new ConcurrentHashMap<String, LockFile>();

	public final String filename;
	public final File   file;

	private volatile FileLock         fileLock;
	private volatile RandomAccessFile handle;
	private volatile FileChannel      channel;



	public static LockFile get(final String filename) {
		if (Utils.isBlank(filename))          throw new RequiredArgumentException("filename");
		if (!SanUtils.safeFileName(filename)) throw new IllegalArgumentException("Invalid lock file name: "+filename);
		// existing lock instance
		{
			final LockFile lock = instances.get(filename);
			if (lock != null)
				return lock;
		}
		// new lock instance
		{
			final LockFile lock = new LockFile(filename);
			final LockFile existing = instances.putIfAbsent(filename, lock);
			if (existing != null)
				return existing;
			Keeper.add(lock);
			return lock;
		}
	}
	public static LockFile peek(final String filename) {
		if (Utils.isBlank(filename))          throw new RequiredArgumentException("filename");
		if (!SanUtils.safeFileName(filename)) throw new IllegalArgumentException("Invalid lock file name: "+filename);
		return instances.get(filename);
	}



	public static LockFile getLock(final String filename) {
		final LockFile lock = get(filename);
		if (lock == null)
			return null;
		if (lock.acquire())
			return lock;
		return null;
	}
	public static boolean getRelease(final String filename) {
		final LockFile lock = get(filename);
		if (lock == null)
			return false;
		return lock.release();
	}



	public boolean isLocked() {
		return (this.fileLock != null);
	}



	private LockFile(final String filename) {
		if (Utils.isBlank(filename))
			throw new RequiredArgumentException("filename");
		if (!SanUtils.safeFileName(filename))
			throw new IllegalArgumentException("Invalid lock file name: "+filename);
		this.filename = filename;
		this.file = new File(filename);
		// register shutdown hook
		Runtime.getRuntime().addShutdownHook(
			new Thread() {
				private volatile LockFile lock = null;
				public Thread init(final LockFile lock) {
					this.lock = lock;
					return this;
				}
				@Override
				public void run() {
					this.lock
						.release();
				}
			}.init(this)
		);
	}



	// get lock on file
	public boolean acquire() {
		if (this.fileLock != null)
			return true;
		try {
			this.handle = new RandomAccessFile(this.file, "rw");
		} catch (FileNotFoundException e) {
			log().trace(e);
			return false;
		}
		this.channel  = this.handle.getChannel();
		try {
			this.fileLock = this.channel.tryLock();
		} catch (OverlappingFileLockException e) {
			log().trace(e);
			return false;
		} catch (IOException e) {
			log().trace(e);
			return false;
		}
		if (this.fileLock == null)
			return false;
		final int pid = ProcUtils.getPid();
		try {
			this.handle.write(
				Integer.toString(pid)
					.getBytes()
			);
		} catch (IOException e) {
			log().trace(e);
			this.fileLock = null;
			return false;
		}
		log().fine("Locked file: {}", this.filename);
		return true;
	}
	// release file lock
	public boolean release() {
		if (this.fileLock == null)
			return false;
		try {
			this.fileLock.release();
		} catch (Exception ignore) {}
		try {
			this.fileLock.close();
		} catch (Exception ignore) {}
		Utils.safeClose(this.channel);
		Utils.safeClose(this.handle);
		this.fileLock = null;
		this.channel = null;
		this.handle  = null;
		log().fine("Released file lock: {}", this.filename);
		try {
			this.file.delete();
		} catch (Exception ignore) {}
		Keeper.remove(this);
		return true;
	}



	// logger
	private volatile SoftReference<xLog> _log = null;
	private volatile String _className = null;
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null)
				return log;
		}
		if (this._className == null) {
			this._className =
				ReflectUtils.getClassName(
					this.getClass()
				);
		}
		final xLog log =
			xLog.getRoot()
				.get(this._className);
		this._log = new SoftReference<xLog>(log);
		return log;
	}



}
