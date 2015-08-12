package com.poixson.commonapp.net.firewall;


public enum RuleType {

	ALLOW_LOCAL (true,  false),
	ALLOW_REMOTE(true,  true ),
	DENY_LOCAL  (false, false),
	DENY_REMOTE (false, true );



	public final boolean allow;
	public final boolean remote;



	RuleType(final boolean allow, final boolean remote) {
		this.allow  = allow;
		this.remote = remote;
	}



	public boolean isAllow() {
		return this.allow;
	}
	public boolean isDeny() {
		return !this.allow;
	}
	public boolean isLocal() {
		return !this.remote;
	}
	public boolean isRemote() {
		return this.remote;
	}



	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder();
		// allow
		if(this.isAllow()) {
			str.append("++");
		} else
		// deny
		if(this.isDeny()) {
			str.append("--");
		} else {
			throw new RuntimeException();
		}
		// local
		if(this.isLocal()) {
			str.append("L");
		} else
		// remote
		if(this.isRemote()) {
			str.append("R");
		} else {
			throw new RuntimeException();
		}
		return str.toString();
	}



}
