package com.poixson.commonapp.config;

import java.util.Map;


public class TestFileConfig extends xConfig {

	public final String  something;
	public final boolean bool;
	public final int     number;
	public final String  missing;



	public TestFileConfig(final Map<String, Object> datamap) {
		super(datamap);
		this.something = this.getStr("something", "somethingDefault");
		this.bool      = this.getBool("bool",     false);
		this.number    = this.getInt("number",    -1);
		this.missing   = this.getStr("missing",   "ok");
	}



	public String getSomething() {
		return this.something;
	}



}
