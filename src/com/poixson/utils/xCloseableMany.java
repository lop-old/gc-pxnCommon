package com.poixson.utils;

import java.io.IOException;


public interface xCloseableMany extends xCloseable {


	@Override
	public void close() throws IOException;
	public void CloseAll();

	@Override
	public boolean isClosed();


}
