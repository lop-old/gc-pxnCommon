package com.poixson.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.poixson.utils.xLogger.xLog;


public class xClock {

	public static final String DEFAULT_TIMESERVER = "pool.ntp.org";
	public static final boolean DEFAULT_BLOCKING = true;

	private static final AtomicReference<xClock> instance =
			new AtomicReference<xClock>(null);

	private volatile String timeserver = null;
	private volatile boolean enableNTP = false;

	private volatile double localOffset = 0.0;
	private volatile double lastChecked = 0.0;

	// update thread
	private final AtomicReference<Thread> thread = new AtomicReference<Thread>(null);
	private final AtomicBoolean running = new AtomicBoolean(false);

	private final AtomicReference<Condition> blockingLock = new AtomicReference<Condition>(null);



//TODO: add cooldown to update again
	public static xClock get(final boolean blocking) {
		if (instance.get() != null)
			return instance.get();
		// new instance
		final xClock clock = new xClock();
		if (!instance.compareAndSet(null, clock))
			return instance.get();
		clock.update(blocking);
		// just to prevent gc
		Keeper.add(clock);
		return clock;
	}
	public static xClock get() {
		return get(DEFAULT_BLOCKING);
	}



	// new instance
	private xClock() {
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new ClassCastException();
	}



	public void update(final boolean blocking) {
		if (!this.enableNTP) return;
		// wait for update
		if (blocking) {
			this.doUpdate(blocking);
			return;
		}
		// update in a new thread
		if (this.thread.get() == null) {
			final Thread thread = new Thread() {
				private volatile xClock  clock    = null;
				private volatile boolean blocking = false;
				public Thread init(final xClock clock, final boolean blocking) {
					this.clock    = clock;
					this.blocking = blocking;
					return this;
				}
				@Override
				public void run() {
					this.clock
						.doUpdate(this.blocking);
				}
			}.init(this, blocking);
			if (this.thread.compareAndSet(null, thread)) {
				thread.setDaemon(true);
				thread.setName("xClock Update");
				thread.start();
			}
		}
	}



	protected void doUpdate(final boolean blocking) {
		if (!this.enableNTP) return;
		// already running
		if (!this.running.compareAndSet(false, true)) {
			// wait until finished
			if (blocking) {
				Condition lock = this.blockingLock.get();
				if (lock == null) {
					lock = (new ReentrantLock()).newCondition();
					if (!this.blockingLock.compareAndSet(null, lock)) {
						lock = this.blockingLock.get();
					}
					try {
						while (true) {
							if (!this.running.get())
								break;
							if (this.blockingLock.get() == null)
								break;
							lock.await(100L, xTimeU.MS);
						}
					} catch (InterruptedException ignore) {}
				}
			}
			return;
		}
		long time = getSystemTime();
		// checked in last 60 seconds
		if ( this.lastChecked != 0.0) {
			if ((time - this.lastChecked) < 60.0) {
				return;
			}
		}
		final xLog log = this.log();
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(500);
			final InetAddress address = InetAddress.getByName(this.timeserver);
			byte[] buf = new ntpMessage().toByteArray();
			final DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);
			ntpMessage.encodeTimestamp(packet.getData(), 40, fromUnixTimestamp());
			socket.send(packet);
			socket.receive(packet);
			final ntpMessage msg = new ntpMessage(packet.getData());
			// calculate local offset for correction
			time = getSystemTime();
			this.localOffset =
				(
					(msg.receiveTimestamp - msg.originateTimestamp)
					+ (msg.transmitTimestamp - fromUnixTimestamp(time))
				) / 2.0;
			// less than 100ms
			if (this.localOffset < 0.1 && this.localOffset > -0.1) {
				log.info(
					"System time only off by {}, ignoring adjustment.",
					NumberUtils.FormatDecimal(
						"0.000",
						this.localOffset
					)
				);
				this.localOffset = 0.0;
			} else {
				log.info(
					"Internal time adjusted by {}{} seconds",
					(
						this.localOffset > 0
						? "+"
						: "-"
					),
					NumberUtils.FormatDecimal(
						"0.000",
						this.localOffset
					)
				);
				log.info(
					"System time:   {}",
					timestampToString(
						time / 1000.0
					)
				);
				log.info(
					"Internal time: {}",
					this.getString()
				);
			}
		} catch (SocketException e) {
			log.trace(e);
		} catch (UnknownHostException e) {
			log.trace(e);
		} catch (IOException e) {
			log.trace(e);
		} catch (Exception e) {
			log.trace(e);
		} finally {
			Utils.safeClose(socket);
			this.lastChecked = time;
			this.running.set(false);
			this.thread.set(null);
			// signal waiting threads
			final Condition lock = this.blockingLock.get();
			if (lock != null) {
				lock.signalAll();
				this.blockingLock.set(null);
			}
		}
	}



	public boolean isRunning() {
		return this.running.get();
	}



	public String getTimeServer() {
		if (Utils.isEmpty(this.timeserver)) {
			return DEFAULT_TIMESERVER;
		}
		return this.timeserver;
	}
	public void setTimeServer(final String host) {
		this.timeserver = host;
	}
	public void setEnabled(final boolean enabled) {
		this.enableNTP = enabled;
		if (!enabled) {
			this.localOffset = 0.0;
			this.lastChecked = 0.0;
		}
	}



	/**
	 * Get current time from system.
	 * @return
	 */
	public static long getSystemTime() {
		return System.currentTimeMillis();
	}
	/**
	 * Get current time adjusted by NTP.
	 * @return
	 */
	public long getCurrentTime() {
		return getSystemTime() - ((long) this.localOffset);
	}



	public long millis() {
		final long time = this.getCurrentTime();
		return time;
	}
	public double seconds() {
		return millis() / 1000.0;
	}



	protected static double fromUnixTimestamp(final double timestamp) {
		return (timestamp / 1000.0) + 2208988800.0;
	}
	protected static double fromUnixTimestamp() {
		return
			fromUnixTimestamp(
				getSystemTime()
			);
	}



	public static String timestampToString(final double timestamp) {
		if (timestamp <= 0.0)
			return "0";
		final StringBuilder buf = new StringBuilder();
		buf.append(
			(new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"))
				.format(
					new Date(
						(long) (timestamp * 1000.0)
					)
				)
		);
		buf.append(
			(new DecimalFormat("0.000"))
				.format(
					timestamp - timestamp
				)
		);
		return buf.toString();
	}
	public String getString() {
		return timestampToString(seconds());
	}



	// logger
	public xLog log() {
		return Utils.log();
	}



}
