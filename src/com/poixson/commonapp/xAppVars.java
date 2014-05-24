package com.poixson.commonapp;

import com.poixson.commonjava.xVars;


public class xAppVars extends xVars {


	// singleton instance
	private static volatile xAppVars instance = null;
	public static xAppVars get() {
		if(instance == null) {
			synchronized(instanceLock) {
				if(instance == null)
					instance = new xAppVars();
			}
		}
		return instance;
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}


	// new instance of holder
	protected xAppVars() {
		super();
		// clone vars
		if(instance != null) {

		// new instance
		} else {

		}
	}


}
