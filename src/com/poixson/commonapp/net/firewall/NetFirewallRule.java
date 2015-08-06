package com.poixson.commonapp.net;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsNumbers;
import com.poixson.commonjava.Utils.byRef.StringRef;


public abstract class NetFirewallRule {

	protected final RuleType type;



	public NetFirewallRule(final RuleType type) {
		if(type == null) throw new NullPointerException("type argument is required!");
		this.type = type;
	}



	public abstract Boolean check(
			final String localHost,  final int localPort,
			final String remoteHost, final int remotePort);



	protected void SplitPattern(final String pattern, final StringRef hostPattern, final StringRef portPattern) {
		if(utils.isEmpty(pattern)) throw new NullPointerException("pattern argument is required!");
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
		if(utils.isEmpty(portPattern)) return null;
		if("*".equals(portPattern))    return Boolean.TRUE;
		final int port;
		if(this.type.isLocal()) {
			port = localPort;
		} else
		if(this.type.isRemote()) {
			port = remotePort;
		} else {
			throw new RuntimeException();
		}
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



	public abstract String toString();



}
