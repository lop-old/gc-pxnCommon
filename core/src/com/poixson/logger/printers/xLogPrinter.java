package com.poixson.logger.printers;

import java.io.IOException;

import com.poixson.logger.xLevel;
import com.poixson.logger.formatters.xLogFormatter;
import com.poixson.logger.records.xLogRecord;


public interface xLogPrinter {


	public void publish(final xLogRecord record) throws IOException;
	public void publish(final String[] lines) throws IOException;
	public void publish(final String   line ) throws IOException;
	public void publish() throws IOException;
	public void flush() throws IOException;


	public void getPublishLock() throws IOException;
	public void releasePublishLock();


	public xLevel getLevel();
	public void setLevel(final xLevel level);
	public boolean isLoggable(final xLevel level);
	public boolean notLoggable(final xLevel level);


	public xLogFormatter getFormatter();
	public void setFormatter(final xLogFormatter formatter);


}
