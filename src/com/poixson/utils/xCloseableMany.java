package com.poixson.utils;

import java.io.IOException;


public interface xCloseableMany extends xCloseable {


	@Override
	public void close() throws IOException;
	public void closeAll();

	@Override
	public boolean isClosed();


}
