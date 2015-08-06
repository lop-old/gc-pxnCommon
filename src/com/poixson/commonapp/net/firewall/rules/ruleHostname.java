package com.poixson.commonapp.net.firewall.rules;

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
	public Boolean check(
			final String localHost,  final int localPort,
			final String remoteHost, final int remotePort) {
		if(utils.isEmpty(this.pattern)) return null;
		if("*".equals(this.pattern))    return Boolean.TRUE;
		// split pattern by :
		final StringRef hostPattern = new StringRef();
		final StringRef portPattern = new StringRef();
		this.SplitPattern(this.pattern, hostPattern, portPattern);
		final String host;
		if(this.type.isLocal()) {
			host = localHost;
		} else
		if(this.type.isRemote()) {
			host = remoteHost;
		} else {
			throw new RuntimeException();
		}
		// check port
		boolean portResult = false;
		if(utils.notEmpty(portPattern.value)) {
			final Boolean match = this.checkPort(localPort, remotePort, portPattern.value);
			if(match != null) {
				if(match.booleanValue())
					portResult = true;
				else
					return null;
			}
		}
		// check hostname
		if(utils.notEmpty(hostPattern.value)){
			if(host.matches(utilsString.wildcardToRegex(hostPattern.value)))
				return Boolean.valueOf(this.type.isAllow());
		}
		if(portResult)
			return Boolean.TRUE;
		return null;
	}



	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder();
		str.append("<").append(this.type.toString()).append(">");
		str.append(this.pattern);
		return str.toString();
	}



}
