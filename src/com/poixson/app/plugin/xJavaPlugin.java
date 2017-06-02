package com.poixson.app.plugin;

import java.util.concurrent.atomic.AtomicReference;

import com.poixson.utils.exceptions.RequiredArgumentException;


public abstract class xJavaPlugin {

	protected final xPluginManager<?> manager;
	protected final xPluginYML yml;

	public static enum PLUGIN_STATE {
		INITED,
		RUNNING,
		STOPPED,
		UNLOADED,
		FAILED
	}
	protected final AtomicReference<PLUGIN_STATE> state =
		new AtomicReference<PLUGIN_STATE>(null);



	public xJavaPlugin(final xPluginManager<?> manager, final xPluginYML yml) {
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



//TODO:
/*
	private volatile xPluginManager manager = null;
	private volatile xPluginYML yml = null;

	private enum INIT_STATE {FRESH, INITED, UNLOADED}
	private volatile INIT_STATE inited = INIT_STATE.FRESH;
	private volatile boolean enabled = false;
	private volatile boolean failed = false;



	protected void doInit(final xPluginManager pluginManager, final xPluginYML yaml) {
		if(pluginManager == null) throw new RequiredArgumentException("pluginManager");
		if(yaml          == null) throw new RequiredArgumentException("yaml");
		if(this.inited.equals(INIT_STATE.INITED))   throw new IllegalStateException("Plugin already inited!");
		if(this.inited.equals(INIT_STATE.UNLOADED)) throw new IllegalStateException("Cannot init plugin, already unloaded!");
		synchronized(this.inited) {
			if(this.inited.equals(INIT_STATE.INITED))   throw new IllegalStateException("Plugin already inited!");
			if(this.inited.equals(INIT_STATE.UNLOADED)) throw new IllegalStateException("Cannot init plugin, already unloaded!");
			this.manager = pluginManager;
			this.yml = yaml;
			this.onInit();
			this.inited = INIT_STATE.INITED;
		}
	}
	protected void doUnload() {
		if(!this.inited.equals(INIT_STATE.INITED)) return;
		if(this.isEnabled())
			this.doDisable();
		synchronized(this.inited) {
			if(!this.inited.equals(INIT_STATE.INITED)) return;
			this.inited = INIT_STATE.UNLOADED;
		}
	}
	protected void doEnable() {
		if(this.inited.equals(INIT_STATE.UNLOADED)) throw new IllegalStateException("Cannot enable plugin, already unloaded!");
		if(!this.inited.equals(INIT_STATE.INITED))  throw new IllegalStateException("Cannot enable plugin, not inited!");
		if(this.enabled) return;
		synchronized(this.inited) {
			if(this.inited.equals(INIT_STATE.UNLOADED)) throw new IllegalStateException("Cannot enable plugin, already unloaded!");
			if(!this.inited.equals(INIT_STATE.INITED))  throw new IllegalStateException("Cannot enable plugin, not inited!");
			if(this.enabled) return;
			this.log().finer("Enabling..");
			this.onEnable();
			if(this.failed) {
				this.log().severe("Plugin failed to load");
				this.enabled = false;
				return;
			}
			this.log().finer("Plugin is Ready");
			this.enabled = true;
		}
	}
	protected void doDisable() {
		if(this.inited.equals(INIT_STATE.UNLOADED)) throw new IllegalStateException("Cannot enable plugin, already unloaded!");
		if(!this.inited.equals(INIT_STATE.INITED))  throw new IllegalStateException("Cannot enable plugin, not inited!");
		if(!this.enabled) return;
		synchronized(this.inited){
			if(this.inited.equals(INIT_STATE.UNLOADED)) throw new IllegalStateException("Cannot enable plugin, already unloaded!");
			if(!this.inited.equals(INIT_STATE.INITED))  throw new IllegalStateException("Cannot enable plugin, not inited!");
			if(!this.enabled) return;
			this.enabled = false;
			this.log().finer("Disabling..");
			this.onDisable();
			this.log().finer("Disabled");
		} 
	}
	public void fail(final String msg) {
		this.failed = true;
		if(utils.notEmpty(msg)) {
			this.log().fatal(msg);
			Failure.addMessageSilently(msg);
		}
	}



	public boolean isEnabled() {
		if(!this.inited.equals(INIT_STATE.INITED))
			return false;
		return this.enabled;
	}




	public abstract void unregister(final Class<? extends xEventListener> listenerClass);



	public xPluginManager getPluginManager() {
		return this.manager;
	}
	public xPluginYML getPluginYML() {
		return this.yml;
	}



	public String getPluginName() {
		return getPluginYML().getPluginName();
	}
	public String getPluginVersion() {
		return getPluginYML().getPluginVersion();
	}
	public String getPluginAuthor() {
		return getPluginYML().getPluginAuthor();
	}
	public String getPluginWebsite() {
		return getPluginYML().getPluginWebsite();
	}



	// logger
	private volatile xLog _log = null;
	public xLog log() {
		if(this._log == null)
			this._log = xLog.getRoot(getPluginName());
		return this._log;
	}
*/



}
