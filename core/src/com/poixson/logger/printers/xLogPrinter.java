package com.poixson.logger.printers;

import java.io.IOException;

import com.poixson.logger.xLevel;
import com.poixson.logger.xLogRecord;
import com.poixson.logger.formatters.xLogFormatter;


public interface xLogPrinter {


	public void publish(final xLogRecord record);
	public void publish(final String[] lines) throws IOException;
	public void publish(final String   line ) throws IOException;


	public void getPublishLock() throws IOException;
	public void releasePublishLock();


	public xLevel getLevel();
	public void setLevel(final xLevel level);
	public boolean isLoggable(final xLevel level);


	public xLogFormatter getFormatter();
	public void setFormatter(final xLogFormatter formatter);


}
