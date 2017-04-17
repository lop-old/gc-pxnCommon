/*
package com.poixson.commonapp.net.firewall.rules;

import java.net.InetSocketAddress;

import com.poixson.commonapp.net.firewall.NetFirewallRule;
import com.poixson.commonapp.net.firewall.RuleType;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.byRef.StringRef;


public class ruleHostname extends NetFirewallRule {

	protected final String pattern;



	public ruleHostname(final RuleType type, final String pattern) {
		super(type);
		this.pattern = pattern;
	}



	@Override
	public Boolean check(final InetSocketAddress local, final InetSocketAddress remote) {
		if(!this.ruleType.isLocal() && !this.ruleType.isRemote())
			throw new RuntimeException("Not local or remote type!");
		if(utils.isEmpty(this.pattern)) return null;
		if("*".equals(this.pattern))    return Boolean.valueOf(this.ruleType.isAllow());
		// split pattern by :
		final StringRef hostPattern = new StringRef();
		final StringRef portPattern = new StringRef();
		this.SplitPattern(this.pattern, hostPattern, portPattern);
		// check port
		if(utils.notEmpty(portPattern.value)) {
			final Boolean match = this.checkPort(
					local.getPort(),
					remote.getPort(),
					portPattern.value
			);
			if(match != null) {
				if(match.booleanValue()) {
					if(utils.isEmpty(hostPattern.value))
						return Boolean.valueOf(this.ruleType.isAllow());
				} else {
					return null;
				}
			}
		}
		// check hostname
		final String host = (this.ruleType.isLocal() ? local.getHostName() : remote.getHostName());
		if(host.matches(utilsString.wildcardToRegex(hostPattern.value)))
			return Boolean.valueOf(this.ruleType.isAllow());
		return null;
	}



	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder();
		str.append("<").append(this.ruleType.toString()).append(">");
		str.append(this.pattern);
		return str.toString();
	}



}
*/
