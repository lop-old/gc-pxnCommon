package com.poixson.commonapp.net;

import java.util.concurrent.CopyOnWriteArraySet;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.xLogger.xLog;


public class NetFirewall {

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
	public Boolean check(
			final String localHost,  final int localPort,
			final String remoteHost, final int remotePort) {
		if(utils.isEmpty(this.rules))
			return Boolean.TRUE;
		for(final NetFirewallRule rule : this.rules) {
			final Boolean result = rule.check(
					localHost,
					localPort,
					remoteHost,
					remotePort
			);
			if(result != null) {
xLog.getRoot("NET").finer("Found matching firewall rule:  "+rule.toString()+" - "+
"Local: "+localHost+":"+Integer.toString(localPort)+" - "+
"Remote: "+remoteHost+":"+Integer.toString(remotePort));
				return result;
			}
		}
xLog.getRoot("NET").finer("No matching firewall rule!  "+
"Local: "+localHost+":"+Integer.toString(localPort)+"  "+
"Remote: "+remoteHost+":"+Integer.toString(remotePort));
		return null;
	}



}
