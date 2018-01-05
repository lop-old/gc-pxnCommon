package com.poixson.abstractions;


public interface xHashable {


	@Override
	public String toString();
	public String getKey();
	public boolean matches(final xHashable hashable);


}
