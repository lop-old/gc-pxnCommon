package com.poixson.logger.printers;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import com.poixson.app.xVars;
import com.poixson.logger.xLevel;
import com.poixson.logger.xLogRecord;
import com.poixson.logger.xLogRoot;
import com.poixson.logger.formatters.xLogFormatter;
import com.poixson.tools.xTimeU;
import com.poixson.utils.Utils;


public abstract class xLogPrinterBasic implements xLogPrinter {

	private volatile xLevel level = null;
	private volatile xLogFormatter formatter = null;

	protected final ReentrantLock publishLock = new ReentrantLock(true);



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
		this.getPublishLock();
		if (Utils.isEmpty(lines)) {
			this.publish( (String) null );
		} else {
			for (final String line : lines) {
				this.publish(line);
			}
		}
		this.releasePublishLock();
	}
	@Override
	public abstract void publish(final String line);



	// ------------------------------------------------------------------------------- //
	// publish lock



	@Override
	public void getPublishLock() throws IOException {
		this.getPublishLock(this.publishLock);
	}
	@Override
	public void releasePublishLock() {
		this.releasePublishLock(
			this.publishLock
		);
	}



	protected void getPublishLock(final ReentrantLock lock) throws IOException {
		TIMEOUT_LOOP:
		for (int i=0; i<5; i++) {
			try {
				if (lock.tryLock(100L, xTimeU.MS)) {
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
	// config



	// log level
	@Override
	public xLevel getLevel() {
		return this.level;
	}
	@Override
	public void setLevel(final xLevel lvl) {
		this.level = lvl;
	}
	// is level loggable
	@Override
	public boolean isLoggable(final xLevel lvl) {
		if (lvl == null || this.level == null)
			return true;
		return this.level.isLoggable(lvl);
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
		return xLogRoot.get()
				.getDefaultFormatter();
	}
	@Override
	public void setFormatter(final xLogFormatter formatter) {
		this.formatter = formatter;
	}



}
