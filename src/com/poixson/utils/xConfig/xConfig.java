package com.poixson.utils.xConfig;

import java.util.Map;

import com.poixson.utils.Utils;


public class xConfig extends xConfigValues {

	private volatile boolean fromResource = false;
	private volatile String fileName = null;



	public xConfig(final Map<String, Object> datamap) {
		super(datamap);
	}
	@Override
	public xConfig clone() {
		return new xConfig(this.datamap);
	}



	// ------------------------------------------------------------------------------- //



	public boolean isFromResource() {
		return this.fromResource;
	}
	void setFromResource() {
		this.setFromResource(true);
	}
	void setFromResource(final boolean fromResource) {
		this.fromResource = fromResource;
	}



	public String getFileName() {
		return this.fileName;
	}
	public xConfig setFileName(final String fileName) {
		this.fileName =
			Utils.isEmpty(fileName)
			? null
			: fileName;
		return this;
	}



}
