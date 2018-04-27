package com.poixson.logger.printers;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import com.poixson.app.xVars;
import com.poixson.logger.xLevel;
import com.poixson.logger.xLogRecord;
import com.poixson.logger.formatters.xLogFormatter;
import com.poixson.logger.formatters.xLogFormatter_Detailed;
import com.poixson.tools.xTimeU;


public abstract class xLogPrinterBasic implements xLogPrinter {

	private volatile xLevel level = null;
	private volatile xLogFormatter formatter = null;

	protected final ReentrantLock publishLock = new ReentrantLock(true);

	protected final AtomicReference<xLogFormatter> defaultFormatter =
			new AtomicReference<xLogFormatter>(null);



	public xLogPrinterBasic() {
	}



	// ------------------------------------------------------------------------------- //
	// publish



	@Override
	public void publish(final xLogRecord record) {
		try {
			if (record.isEmpty()) {
				this.publish( (String[]) null );
				return;
			}
			// handle the log entry
			final xLogFormatter formatter = this.getFormatter();
			this.publish(
				formatter.formatMessage(
					record
				)
			);
		} catch (IOException e) {
			e.printStackTrace(
				xVars.getOriginalErr()
			);
		}
	}
	@Override
	public void publish(final String[] lines) throws IOException {
		this.doPublish(lines);
	}
	@Override
	public void publish(final String line) throws IOException {
		this.doPublish(line);
	}
	@Override
	public void publish() throws IOException {
		this.doPublish( (String)null );
	}

	protected abstract void doPublish(final String[] lines) throws IOException;
	protected abstract void doPublish(final String   line ) throws IOException;



	// ------------------------------------------------------------------------------- //
	// publish lock



	@Override
	public void getPublishLock() throws IOException {
		this.getPublishLock(
			this.publishLock
		);
	}
	@Override
	public void releasePublishLock() {
		this.releasePublishLock(
			this.publishLock
		);
	}



	protected void getPublishLock(final ReentrantLock lock) throws IOException {
		TIMEOUT_LOOP:
		for (int i=0; i<50; i++) {
			try {
				if (lock.tryLock(10L, xTimeU.MS)) {
					return;
				}
			} catch (InterruptedException e) {
				throw new IOException("Failed to publish!", e);
			}
			if (Thread.interrupted())
				break TIMEOUT_LOOP;
		} // end TIMEOUT_LOOP
		throw new IOException("Failed to publish!");
	}
	protected void releasePublishLock(final ReentrantLock lock) {
		lock.unlock();
	}



	// ------------------------------------------------------------------------------- //
	// default formatter



	public xLogFormatter getDefaultFormatter() {
		// existing instance
		{
			final xLogFormatter formatter = this.defaultFormatter.get();
			if (formatter != null)
				return formatter;
		}
		// new default formatter
		{
			final xLogFormatter formatter =
				new xLogFormatter_Detailed();
			if ( ! this.defaultFormatter.compareAndSet(null, formatter) )
				return this.defaultFormatter.get();
			return formatter;
		}
	}



	// ------------------------------------------------------------------------------- //
	// config



	// log level
	@Override
	public xLevel getLevel() {
		return this.level;
	}
	@Override
	public void setLevel(final xLevel level) {
		this.level = level;
	}
	// is level loggable
	@Override
	public boolean isLoggable(final xLevel level) {
		if (level == null || this.level == null)
			return true;
		return this.level.isLoggable(level);
	}
	public boolean notLoggable(final xLevel level) {
		if (level == null || this.level == null)
			return false;
		return this.level.notLoggable(level);
	}



	// ------------------------------------------------------------------------------- //
	// printer formatter



	@Override
	public xLogFormatter getFormatter() {
		// existing instance
		final xLogFormatter formatter = this.formatter;
		if (formatter != null)
			return formatter;
		// default formatter
		return this.getDefaultFormatter();
	}
	@Override
	public void setFormatter(final xLogFormatter formatter) {
		this.formatter = formatter;
	}



}
