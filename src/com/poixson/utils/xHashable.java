package com.poixson.commonjava.Utils;

public interface xHashable {


	@Override
	public String toString();
	public String getKey();
	public boolean matches(final xHashable hashable);


}
