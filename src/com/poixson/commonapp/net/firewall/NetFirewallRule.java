/*
package com.poixson.commonapp.net.firewall;

import java.net.InetSocketAddress;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsNumbers;
import com.poixson.commonjava.Utils.byRef.StringRef;
import com.poixson.commonjava.Utils.exceptions.RequiredArgumentException;


public abstract class NetFirewallRule {

	protected final RuleType ruleType;



	public NetFirewallRule(final RuleType ruleType) {
		if(ruleType == null) throw new RequiredArgumentException("ruleType");
		this.ruleType = ruleType;
	}



	public abstract Boolean check(final InetSocketAddress local, final InetSocketAddress remote);



	protected void SplitPattern(final String pattern,
			final StringRef hostPattern, final StringRef portPattern) {
		if(utils.isEmpty(pattern)) throw new RequiredArgumentException("pattern");
		final String[] parts = pattern.split(":", 2);
		if(parts.length > 1) {
			hostPattern.value(parts[0]);
			portPattern.value(parts[1]);
		} else {
			hostPattern.value(pattern);
			portPattern.value(null);
		}
	}



	public Boolean checkPort(final int localPort, final int remotePort, final String portPattern) {
		if(!this.ruleType.isLocal() && !this.ruleType.isRemote())
			throw new RuntimeException("Not local or remote type!");
		if(utils.isEmpty(portPattern)) return null;
		if("*".equals(portPattern))    return Boolean.TRUE;
		final int port = (this.ruleType.isLocal() ? localPort : remotePort);
		final String[] parts = portPattern.split("-", 2);
		// port range
		if(parts.length > 1) {
			final int min = ( "*".equals(parts[0]) ? 0                     : Integer.parseInt(parts[0]) );
			final int max = ( "*".equals(parts[1]) ? utilsNumbers.MAX_PORT : Integer.parseInt(parts[1]) );
			return utilsNumbers.isMinMax(port, min, max);
		}
		// single port
		final int pat = Integer.parseInt(portPattern);
		return pat == port;
	}



	@Override
	public abstract String toString();



}
*/
