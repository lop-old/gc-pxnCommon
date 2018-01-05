package com.poixson.abstractions;

import java.io.IOException;


public interface xOpenable extends xCloseable {


	public void open() throws IOException;

	public boolean isOpen();


}
