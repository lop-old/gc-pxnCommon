package com.poixson.app.plugin;

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicReference;

import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLog;


public abstract class xJavaPlugin {

	private volatile  xPluginManager<?> manager = null;
	private volatile xPluginYML yml= null;

	public static enum PLUGIN_STATE {
		INITED,
		RUNNING,
		STOPPED,
		UNLOADED,
		FAILED
	}
	protected final AtomicReference<PLUGIN_STATE> state =
		new AtomicReference<PLUGIN_STATE>(null);



	public xJavaPlugin() {
	}
	public void init(final xPluginManager<?> manager, final xPluginYML yml) {
		if (manager == null) throw new RequiredArgumentException("manager");
		if (yml     == null) throw new RequiredArgumentException("yml");
		this.manager = manager;
		this.yml     = yml;
	}



	protected void onInit()   {}
	protected void onUnload() {}
	protected void onFailed() {}

	protected abstract void onEnable();
	protected abstract void onDisable();



	public void doInit() {
		this.state.compareAndSet(null, PLUGIN_STATE.INITED);
		this.onInit();
	}
	public void doUnload() {
		this.state.set(PLUGIN_STATE.UNLOADED);
		this.onUnload();
	}
	public void setFailed() {
		final PLUGIN_STATE state = this.state.get();
		switch (state) {
		case RUNNING:
			this.doDisable();
		case INITED:
			this.doUnload();
		default:
			break;
		}
		this.state.set(PLUGIN_STATE.FAILED);
	}



	public boolean doEnable() {
		if (!this.state.compareAndSet(PLUGIN_STATE.INITED, PLUGIN_STATE.RUNNING))
			return false;
		this.onEnable();
		return true;
	}
	public boolean doDisable() {
		if (!this.state.compareAndSet(PLUGIN_STATE.RUNNING, PLUGIN_STATE.STOPPED))
			return false;
		this.onDisable();
		return true;
	}



	public PLUGIN_STATE getPluginState() {
		return this.state.get();
	}
	public boolean isPluginRunning() {
		return
			PLUGIN_STATE.RUNNING
				.equals(this.state);
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
