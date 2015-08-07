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
	static final int    LOCAL_PORT  = 1111;
	static final int    REMOTE_PORT = 2222;

	static final InetSocketAddress LOCAL  = new InetSocketAddress(LOCAL_HOST,  LOCAL_PORT);
	static final InetSocketAddress REMOTE = new InetSocketAddress(REMOTE_HOST, REMOTE_PORT);



	// hostname rules
	@Test
	public void ruleHostnameTest() {
		xLogTest.testStart(TEST_NAME_HOSTNAME);
		// expect success
		this.checkHost(true, "*");
		this.checkHost(true, "*hostname");
		this.checkHost(true, "*.hostname");
		this.checkHost(true, "local.host*");
		this.checkHost(true, "local.*");
		this.checkHost(true, "*:1111");
		this.checkHost(true, "*:1110-1112");
		this.checkHost(true, "*:*-1112");
		this.checkHost(true, "*:1110-*");
		// expect fail
		this.checkHost(false, "invalid*");
		this.checkHost(false, "*invalid");
		this.checkHost(false, null);
		this.checkHost(false, "*:2222");
		this.checkHost(false, "*:0-1110");
		this.checkHost(false, "*:*-1110");
		this.checkHost(false, "*:1112-"+Integer.toString(utilsNumbers.MAX_PORT));
		this.checkHost(false, "*:1112-*");
		// expect exception
		try {
			this.checkHost(true, null);
			Assert.assertTrue("Failed to throw expected exception!", false);
		} catch (Exception ignore) {}
		xLogTest.testPassed(TEST_NAME_HOSTNAME);
	}
	void checkHost(final boolean expected, final String pattern) {
		final NetFirewall firewall = new NetFirewall();
		firewall.addRule(new ruleHostname(RuleType.ALLOW_LOCAL, pattern));
		final Boolean result = firewall.check(
				LOCAL_HOST,
				LOCAL_PORT,
				REMOTE_HOST,
				REMOTE_PORT
		);
		if(expected)
			Assert.assertEquals("Pattern didn't return expected result: "+pattern, expected, result.booleanValue());
		else
			Assert.assertTrue("Pattern shouldn't have a match: "+pattern, result == null);
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
