package com.poixson.utils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;

import com.poixson.utils.exceptions.IORuntimeException;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLog;


public class NativeAutoLoader {

	public static final ErrorMode DEFAULT_ERROR_MODE = ErrorMode.LOG;
	protected ErrorMode errorMode = null;

	public static boolean DEFAULT_ENABLE_EXTRACT = false;
	public static boolean DEFAULT_ENABLE_REPLACE = false;

	protected Class<?> classRef   = null;

	protected String fileName = null;
	protected String libPath  = null;
	protected String resPath  = null;

	protected Boolean enableExtract = null;
	protected Boolean enableReplace = null;



	public static NativeAutoLoader get() {
		return new NativeAutoLoader();
	}
	public NativeAutoLoader() {
	}



	// extract/load library
	public boolean Load(final String fileName) {
		if (Utils.notEmpty(fileName)) {
			this.setFileName(fileName);
		}
		return this.Load();
	}
	public boolean Load() {
		final ErrorMode errorMode = this.getErrorMode();
		final String fileName = this.getFileName();
		if (Utils.isEmpty(fileName)) {
			if (ErrorMode.EXCEPTION.equals(errorMode)) {
				throw new RequiredArgumentException("fileName");
			} else
			if (ErrorMode.LOG.equals(errorMode)) {
				this.log().severe("Library fileName is missing!");
			}
			return false;
		}
		final Class<?> classRef   = this.getClassRef();
		final String libPath = this.getLocalLibPath();
		final String resPath = this.getResourcesPath();
		final boolean enableExtract = this.getEnableExtract();
		final boolean enableReplace = this.getEnableReplace();
		final String filePath =
			FileUtils.MergePaths(
				".",
				libPath,
				fileName
			);
		final File outFile = new File(filePath);
		// create library directory
		{
			final File dir = new File(libPath);
			if (!dir.isDirectory()) {
				if (dir.mkdirs()) {
					this.log().info("Created libraries dir: {}", libPath);
				} else {
					if (ErrorMode.EXCEPTION.equals(errorMode)) {
						throw new IORuntimeException("Failed to create directory: "+libPath);
					} else
					if (ErrorMode.LOG.equals(errorMode)) {
						this.log().severe("Failed to create directory: {}", libPath);
					}
					return false;
				}
			}
		}
		// remove existing file
		if (enableExtract && enableReplace) {
			final boolean exists = outFile.isFile();
			if (exists) {
				if (enableReplace && enableExtract) {
					this.log().fine("Replacing existing library file: {}", fileName);
					outFile.delete();
				}
			}
		}
		// extract file
		if (enableExtract) {
			final boolean exists = outFile.isFile();
			if (!exists) {
				try {
					NativeUtils.ExtractLibrary(
						libPath,
						resPath,
						fileName,
						classRef
					);
				} catch (IOException e) {
					this.log().severe(e.getMessage());
					if (ErrorMode.EXCEPTION.equals(errorMode)) {
						throw new IORuntimeException("Failed to extract library file: "+filePath, e);
					} else
					if (ErrorMode.LOG.equals(errorMode)) {
						this.log().severe("Failed to extract library file: {}", filePath);
					}
					return false;
				}
			}
		}
		// load library
		{
			final boolean exists = outFile.isFile();
			if (!exists) {
				if (ErrorMode.EXCEPTION.equals(errorMode)) {
					throw new IORuntimeException("Library file not found: "+filePath);
				} else
				if (ErrorMode.LOG.equals(errorMode)) {
					this.log().severe("Library file not found: {}", filePath);
				}
				return false;
			}
			try {
				NativeUtils.LoadLibrary(filePath);
			} catch (SecurityException e) {
				this.log().severe(e.getMessage());
				if (ErrorMode.EXCEPTION.equals(errorMode)) {
					throw e;
				} else
				if (ErrorMode.LOG.equals(errorMode)) {
					this.log().severe("Failed to load library: {}  {}", filePath, e.getMessage());
				}
				return false;
			} catch (UnsatisfiedLinkError e) {
				this.log().severe(e.getMessage());
				if (ErrorMode.EXCEPTION.equals(errorMode)) {
					throw e;
				} else
				if (ErrorMode.LOG.equals(errorMode)) {
					this.log().severe("Failed to load library: {}  {}", filePath, e.getMessage());
				}
				return false;
			}
		}
		return true;
	}



	// error mode
	public ErrorMode getErrorMode() {
		final ErrorMode mode = this.errorMode;
		return (
			mode == null
			? DEFAULT_ERROR_MODE
			: mode
		);
	}
	public NativeAutoLoader setErrorMode(final ErrorMode mode) {
		this.errorMode = mode;
		return this;
	}



	// library file name
	public String getFileName() {
		String fileName = this.fileName;
		if (Utils.isEmpty(fileName))
			return null;
//TODO: append file extension per os
//		switch (os) {
//		case LINUX:
//			fileName = StringUtils.ForceEnds(".so", fileName);
//			break;
//		}
		return fileName;
	}
	public NativeAutoLoader setFileName(final String fileName) {
		this.fileName = fileName;
		return this;
	}



	// class reference
	public Class<?> getClassRef() {
		final Class<?> clss = this.classRef;
		return (
			clss == null
			? NativeAutoLoader.class
			: clss
		);
	}
	public NativeAutoLoader setClassRef(final Class<?> clss) {
		this.classRef = clss;
		return this;
	}



	// resource library path
	public String getResourcesPath() {
		final String path = this.resPath;
		return (
			Utils.isEmpty(path)
			? null
			: StringUtils.ForceStarts("/", path)
		);
	}
	public NativeAutoLoader setResourcesPath(final String path) {
		this.resPath = path;
		return this;
	}



	// extracted library path
	public String getLocalLibPath() {
		final String path = this.libPath;
		return (
			Utils.isEmpty(path)
			? "."
			: path
		);
	}
	public NativeAutoLoader setLocalLibPath(final String path) {
		this.libPath = path;
		return this;
	}



	// enable extract file
	public boolean getEnableExtract() {
		final Boolean enabled = this.enableExtract;
		return (
			enabled == null
			? DEFAULT_ENABLE_EXTRACT
			: enabled.booleanValue()
		);
	}
	public NativeAutoLoader enableExtract() {
		return this.enableExtract(true);
	}
	public NativeAutoLoader disableExtract() {
		return this.enableExtract(false);
	}
	public NativeAutoLoader enableExtract(final boolean enable) {
		this.enableExtract = Boolean.valueOf(enable);
		return this;
	}



	// enable replace file
	public boolean getEnableReplace() {
		final Boolean enabled = this.enableReplace;
		return (
			enabled == null
			? DEFAULT_ENABLE_REPLACE
			: enabled.booleanValue()
		);
	}
	public NativeAutoLoader enableReplace() {
		return this.enableReplace(true);
	}
	public NativeAutoLoader disableReplace() {
		return this.enableReplace(false);
	}
	public NativeAutoLoader enableReplace(final boolean enable) {
		this.enableReplace = Boolean.valueOf(enable);
		return this;
	}



	// logger
	private volatile SoftReference<xLog> _log = null;
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null)
				return log;
		}
		final xLog log =
			xLog.getRoot()
				.get("LibLoader");
		this._log = new SoftReference<xLog>(log);
		return log;
	}



}
