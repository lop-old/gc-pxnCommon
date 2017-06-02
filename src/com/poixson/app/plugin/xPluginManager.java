package com.poixson.app.plugin;


public class xPluginManager {
//	private static final String LOG_NAME = "PluginManager";

	private final Map<String, PluginDAO> plugins = new HashMap<String, PluginDAO>();



	public xPluginManager() {}



	public void enableAll() {
		synchronized(this.plugins) {
			for(final PluginDAO dao : this.plugins.values()) {
				try {
					dao.log.finest(this.classFieldName+": "+dao.plugin.getClass().getName());
					dao.plugin.doEnable();
				} catch (Exception e) {
					dao.log.trace(e);
				}
			}
		}
	}
	public void disableAll() {
		synchronized(this.plugins){
			for(final PluginDAO dao : this.plugins.values()) {
				try {
					dao.plugin.doDisable();
				} catch (Exception e) {
					dao.log.trace(e);
				}
			}
		}
	}



	public void addPlugin(final PluginDAO plugin) {
		if(plugin == null) throw new RequiredArgumentException("plugin");
		synchronized(this.plugins) {
			this.plugins.put(plugin.name, plugin);
		}
	}
	public void removePlugin(final String name) {
		if(utils.isEmpty(name)) throw new RequiredArgumentException("name");
		synchronized(this.plugins) {
			final PluginDAO dao = this.plugins.get(name);
			if(dao.plugin != null) {
				if(dao.plugin.isEnabled()) {
					try {
						dao.plugin.doDisable();
					} catch (Exception e) {
						dao.log.trace(e);
					}
				}
				try {
					dao.plugin.doUnload();
				} catch (Exception e) {
					dao.log.trace(e);
				}
			}
			this.plugins.remove(name);
		}
	}



	// logger
	private static volatile xLog _log = null;
	public static xLog log() {
		if(_log == null)
			_log = xLog.getRoot();
		return _log;
	}



}
