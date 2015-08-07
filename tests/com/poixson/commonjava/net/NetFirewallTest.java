package com.poixson.commonjava.net;

import java.net.InetSocketAddress;

import org.junit.Assert;
import org.junit.Test;

import com.poixson.commonapp.net.firewall.NetFirewall;
import com.poixson.commonapp.net.firewall.RuleType;
import com.poixson.commonapp.net.firewall.rules.ruleHostname;
import com.poixson.commonjava.xLogTest;
import com.poixson.commonjava.Utils.utilsNumbers;


public class NetFirewallTest {
	static final String TEST_NAME_HOSTNAME = "NetFirewall Hostname";
	static final String TEST_NAME_IPLIST   = "NetFirewall IP List";
	static final String TEST_NAME_IPRANGE  = "NetFirewall IP Range";

	static final String LOCAL_HOST  = "local.hostname";
	static final String REMOTE_HOST = "remote.hostname";
	static final String REMOTE_HOST_BAD = "bad.host";
	static final int    LOCAL_PORT  = 1111;
	static final int    REMOTE_PORT = 2222;

	static final InetSocketAddress LOCAL  = new InetSocketAddress(LOCAL_HOST,  LOCAL_PORT);
	static final InetSocketAddress REMOTE = new InetSocketAddress(REMOTE_HOST, REMOTE_PORT);
	static final InetSocketAddress REMOTE_BAD = new InetSocketAddress(REMOTE_HOST_BAD, REMOTE_PORT);



	// hostname rules
	@Test
	public void ruleHostnameTest() {
		xLogTest.testStart(TEST_NAME_HOSTNAME);
		// expect success
		this.checkHost(Boolean.TRUE, RuleType.ALLOW_LOCAL, "*",           LOCAL, REMOTE);
		this.checkHost(Boolean.TRUE, RuleType.ALLOW_LOCAL, "*hostname",   LOCAL, REMOTE);
		this.checkHost(Boolean.TRUE, RuleType.ALLOW_LOCAL, "*.hostname",  LOCAL, REMOTE);
		this.checkHost(Boolean.TRUE, RuleType.ALLOW_LOCAL, "local.host*", LOCAL, REMOTE);
		this.checkHost(Boolean.TRUE, RuleType.ALLOW_LOCAL, "local.*",     LOCAL, REMOTE);
		this.checkHost(Boolean.TRUE, RuleType.ALLOW_LOCAL, "*:1111",      LOCAL, REMOTE);
		this.checkHost(Boolean.TRUE, RuleType.ALLOW_LOCAL, "*:1110-1112", LOCAL, REMOTE);
		this.checkHost(Boolean.TRUE, RuleType.ALLOW_LOCAL, "*:*-1112",    LOCAL, REMOTE);
		this.checkHost(Boolean.TRUE, RuleType.ALLOW_LOCAL, "*:1110-*",    LOCAL, REMOTE);
		// expect fail
		this.checkHost(null, RuleType.ALLOW_LOCAL, null,       LOCAL, REMOTE);
		this.checkHost(null, RuleType.ALLOW_LOCAL, "invalid*", LOCAL, REMOTE);
		this.checkHost(null, RuleType.ALLOW_LOCAL, "*invalid", LOCAL, REMOTE);
		this.checkHost(null, RuleType.ALLOW_LOCAL, "*:2222",   LOCAL, REMOTE);
		this.checkHost(null, RuleType.ALLOW_LOCAL, "*:0-1110", LOCAL, REMOTE);
		this.checkHost(null, RuleType.ALLOW_LOCAL, "*:*-1110", LOCAL, REMOTE);
		this.checkHost(null, RuleType.ALLOW_LOCAL, "*:1112-"+Integer.toString(utilsNumbers.MAX_PORT), LOCAL, REMOTE);
		this.checkHost(null, RuleType.ALLOW_LOCAL, "*:1112-*", LOCAL, REMOTE);
		// deny host
		this.checkHost(Boolean.FALSE, RuleType.DENY_REMOTE, "bad.*", LOCAL, REMOTE_BAD);
		xLogTest.testPassed(TEST_NAME_HOSTNAME);
	}
	void checkHost(final Boolean expected, final RuleType type, final String pattern,
			final InetSocketAddress local, final InetSocketAddress remote) {
		final NetFirewall firewall = new NetFirewall();
		firewall.addRule(new ruleHostname(type, pattern));
		final Boolean result = firewall.check(local, remote);
		Assert.assertEquals("Pattern didn't return expected result: "+pattern, expected, result);
	}



	// ip list
	@Test
	public void ruleIPListTest() {
		xLogTest.testStart(TEST_NAME_IPLIST);

		xLogTest.testPassed(TEST_NAME_IPLIST);
	}



	// ip range
	@Test
	public void ruleIPRangeTest() {
		xLogTest.testStart(TEST_NAME_IPRANGE);

		xLogTest.testPassed(TEST_NAME_IPRANGE);
	}



}
