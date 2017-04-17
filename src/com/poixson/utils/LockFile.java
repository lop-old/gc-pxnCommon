package com.poixson.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.HashMap;
import java.util.Map;

import com.poixson.utils.exceptions.LockFileException;
import com.poixson.utils.exceptions.RequiredArgumentException;


public class LockFile {
	private static final String LOG_NAME = "LockFile";

	private static final Map<String, LockFile> instances = new HashMap<String, LockFile>();
	private static final Object instanceLock = new Object();

	public final String filename;
	public final File   file;
	private final FileLock         fileLock;
	private final RandomAccessFile randFile;
	private final FileChannel      channel;



	public static LockFile get(final String filename) {
		if (Utils.isEmpty(filename)) throw new RequiredArgumentException("filename");
		synchronized(instanceLock) {
			// existing lock
			if (instances.containsKey(filename))
				throw new LockFileException(filename);
			// new lock
			final LockFile lock;
			try {
				lock = new LockFile(filename);
			} catch (LockFileException e) {
				log().trace(e);
				return null;
			} catch (IOException e) {
				log().trace(e);
				return null;
			}
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
							this.lock.release();
						}
					}.init(lock)
			);
			instances.put(filename, lock);
			log().info("Locked file: "+filename);
			Keeper.add(lock);
			return lock;
		}
	}
	public static LockFile peak(final String filename) {
		if (Utils.isEmpty(filename))
			throw new RequiredArgumentException("filename");
		return instances.get(filename);
	}
	public static void releaseLock(final String filename) {
		final LockFile lock = get(filename);
		if (lock == null)
			return;
		lock.release();
	}



	private LockFile(final String filename) throws LockFileException, IOException {
		if (Utils.isEmpty(filename))
			throw new RequiredArgumentException("filename");
		this.filename = filename;
		// get lock on file
		this.file = new File(filename);
		if (this.file.isFile())
			throw new LockFileException(filename);
		this.randFile = new RandomAccessFile(this.file, "rw");
		this.channel  = this.randFile.getChannel();
		try {
			this.fileLock = this.channel.tryLock();
		} catch (OverlappingFileLockException e) {
			throw new LockFileException(filename);
		}
	}
	public void release() {
		try {
			this.fileLock.release();
		} catch (Exception ignore) {}
		try {
			this.fileLock.close();
		} catch (Exception ignore) {}
		Utils.safeClose(this.channel);
		Utils.safeClose(this.randFile);
		log().info("Released file lock: "+this.filename);
		Keeper.remove(this);
		try {
			this.file.delete();
		} catch (Exception ignore) {}
	}



	// logger
	public static xLog log() {
		return xLog.getRoot(LOG_NAME);
	}



}
