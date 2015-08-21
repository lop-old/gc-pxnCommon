package com.poixson.commonapp.net.firewall;

import java.net.InetSocketAddress;
import java.util.concurrent.CopyOnWriteArraySet;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.xLogger.xLog;


public class NetFirewall {
	private static final String LOG_NAME = "FW";

	protected final CopyOnWriteArraySet<NetFirewallRule> rules =
			new CopyOnWriteArraySet<NetFirewallRule>();



	public void addRule(final NetFirewallRule rule) {
		if(rule == null) throw new NullPointerException("rule argument is required!");
		this.rules.add(rule);
	}



	/**
	 * Check an accepted socket connection.
	 * @param channel
	 * @return true if ok to accept, false to deny, null if no match
	 */
	public Boolean check(final InetSocketAddress local, final InetSocketAddress remote) {
		if(utils.isEmpty(this.rules))
			return Boolean.TRUE;
		for(final NetFirewallRule rule : this.rules) {
			final Boolean result = rule.check(local, remote);
			if(result != null) {
				log().finest("Found matching firewall rule:   "+rule.toString()+"   "+
						"Local: "+local.getHostString()+":"+Integer.toString(local.getPort())+"   "+
						"Remote: "+remote.getHostString()+":"+Integer.toString(remote.getPort()));
				return result;
			}
		}
		log().finest("No matching firewall rule!   "+
				"Local: "+local.getHostString()+":"+Integer.toString(local.getPort())+"   "+
				"Remote: "+remote.getHostString()+":"+Integer.toString(remote.getPort()));
		return null;
	}



	// logger
	public static xLog log() {
		return xLog.getRoot(LOG_NAME);
	}



}
