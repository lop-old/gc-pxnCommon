package com.poixson.utils;


public interface xHashable {


	@Override
	public String toString();
	public String getKey();
	public boolean matches(final xHashable hashable);


}
