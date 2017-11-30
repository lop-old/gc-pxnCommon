package com.poixson.utils;

import java.io.Closeable;
import java.io.IOException;


public interface xCloseable extends Closeable {


	@Override
	public void close() throws IOException;

	public boolean isClosed();


}
