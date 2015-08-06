package com.poixson.commonapp.net.rules;

import com.poixson.commonapp.net.NetFirewallRule;
import com.poixson.commonapp.net.RuleType;


public class ruleIPList extends NetFirewallRule {



	public ruleIPList(final RuleType type) {
		super(type);
	}



	@Override
	public Boolean check(
			final String localHost,  final int localPort,
			final String remoteHost, final int remotePort) {

		return Boolean.FALSE;
	}



	@Override
	public String toString() {
		return null;
	}



}
