package com.poixson.commonapp.net.firewall.rules;

import java.net.InetSocketAddress;

import com.poixson.commonapp.net.firewall.NetFirewallRule;
import com.poixson.commonapp.net.firewall.RuleType;


public class ruleIPList extends NetFirewallRule {



	public ruleIPList(final RuleType type) {
		super(type);
	}



	@Override
	public Boolean check(final InetSocketAddress local, final InetSocketAddress remote) {

		return Boolean.FALSE;
	}



	@Override
	public String toString() {
		return null;
	}



}
