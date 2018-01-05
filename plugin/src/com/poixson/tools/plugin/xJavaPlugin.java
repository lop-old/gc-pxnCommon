package com.poixson.tools.plugin;

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicBoolean;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.AttachedLogger;
import com.poixson.logger.xLog;
import com.poixson.utils.ThreadUtils;


public abstract class xJavaPlugin implements AttachedLogger {

	private volatile  xPluginManager<?> manager = null;
	private volatile xPluginYML yml= null;

	// plugin state
	private final AtomicBoolean inited = new AtomicBoolean(false);
	private final AtomicBoolean running = new AtomicBoolean(false);
	private volatile boolean stopping = false;
	private volatile boolean unloaded = false;
	private volatile boolean failed   = false;



	public xJavaPlugin() {
	}
	public void init(final xPluginManager<?> manager, final xPluginYML yml) {
		if (manager == null) throw RequiredArgumentException.getNew("manager");
		if (yml     == null) throw RequiredArgumentException.getNew("yml");
		this.manager = manager;
		this.yml     = yml;
	}



	protected void onInit()   {}
	protected void onUnload() {}
	protected void onFailed() {}

	protected abstract void onEnable();
	protected abstract void onDisable();



	public void doInit() {
		if (this.failed)
			return;
		if (!this.inited.compareAndSet(false, true))
			return;
		this.onInit();
	}
	public void doUnload() {
		if (this.unloaded)
			return;
		if (this.running.get()) {
			this.doDisable();
		}
		this.onUnload();
		this.unloaded = true;
	}



	public boolean doEnable() {
		if (this.failed)
			return false;
		if (this.stopping)
			return false;
		if (!this.running.compareAndSet(false, true))
			return false;
		this.onEnable();
		if (this.failed)
			return false;
		return true;
	}
	public boolean doDisable() {
		if (this.failed)
			return false;
		if (this.stopping)
			return false;
		this.stopping = true;
		this.onDisable();
		if (this.failed)
			return false;
		return true;
	}



	public boolean isInited() {
		return this.inited.get();
	}
	public boolean isRunning() {
		if (this.stopping || this.failed)
			return false;
		return this.running.get();
	}
	public boolean isStop() {
		return this.stopping;
	}
	public boolean isFailed() {
		return this.failed;
	}
	public void setFailed() {
		if (this.failed)
			return;
		this.failed = true;
		if (this.running.get()) {
			this.onDisable();
			ThreadUtils.Sleep(50L);
		}
		if (!this.unloaded) {
			this.onUnload();
		}
	}



	public xPluginManager<?> getPluginManager() {
		return this.manager;
	}
	public xPluginYML getPluginYML() {
		return this.yml;
	}



	public String getPluginName() {
		return this.yml.getPluginName();
	}
	public String getPluginVersion() {
		return this.yml.getPluginVersion();
	}
	public String getPluginAuthor() {
		return this.yml.getPluginAuthor();
	}
	public String getPluginWebsite() {
		return this.yml.getPluginWebsite();
	}



	// logger
	private volatile SoftReference<xLog> _log = null;
	@Override
	public xLog log() {
		if (this._log != null) {
			final xLog log = this._log.get();
			if (log != null)
				return log;
		}
		final xLog log =
			xLog.getRoot()
				.get("Plugin:"+this.getPluginName());
		this._log = new SoftReference<xLog>(log);
		return log;
	}



}
