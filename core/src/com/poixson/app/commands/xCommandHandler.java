package com.poixson.app.commands;


public interface xCommandHandler {


	public void register(final Object...objs);

	public boolean process(final String line);


}
