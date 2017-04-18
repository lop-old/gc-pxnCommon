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


public class xClock {

	public static final String DEFAULT_TIMESERVER = "pool.ntp.org";

	private volatile String timeserver = null;
	private volatile boolean enableNTP = false;

	private volatile double localOffset = 0.0;
	private volatile double lastChecked = 0.0;

	// update thread
	private volatile Thread thread = null;
	private volatile boolean blocking = true;
	private volatile boolean running = false;
	private final Object runLock = new Object();

	// clock instance
	private static volatile xClock instance = null;
	private static final Object lock = new Object();



	public static xClock get(final boolean blocking) {
		if (instance == null) {
			synchronized(lock) {
				if (instance == null) {
					instance = new xClock(blocking);
					// just to prevent gc
					Keeper.add(instance);
				}
			}
		}
		instance.update();
		return instance;
	}
	public static xClock get() {
		return get(true);
	}



	// new instance
	private xClock(final boolean blocking) {
		this.blocking = blocking;
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new ClassCastException();
	}



	public void update() {
		if (!this.enableNTP || this.running) return;
		synchronized(this.runLock) {
			if (this.running) return;
			this.running = true;
			// wait for update
			if (this.blocking) {
				doUpdate();
			// update threaded
			} else {
				if (this.thread == null) {
					this.thread = new Thread() {
						@Override
						public void run() {
							doUpdate();
						}
					};
					this.thread.start();
				}
			}
		}
	}



	protected void doUpdate() {
		if (!this.enableNTP || !this.running) return;
		long time = getSystemTime();
		// checked in last 60 seconds
		if ( this.lastChecked != 0.0 &&
			((time - this.lastChecked) < 60.0) )
				return;
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
			this.localOffset = ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - fromUnixTimestamp(time))) / 2.0;
			// less than 100ms
			if (this.localOffset < 0.1 && this.localOffset > -0.1) {
//TODO:
//				log().info("System time only off by "+
//						NumberUtils.FormatDecimal("0.000", this.localOffset)+
//						", ignoring adjustment.");
				this.localOffset = 0.0;
			} else {
//TODO:
//				log().info("Internal time adjusted by "+(this.localOffset>0 ? "+" : "-")+
//						NumberUtils.FormatDecimal("0.000", this.localOffset)+" seconds");
//				log().info("System time:   "+timestampToString(time / 1000.0));
//				log().info("Internal time: "+getString());
			}
		} catch (SocketException e) {
//TODO:
//			log().trace(e);
		} catch (UnknownHostException e) {
//			log().trace(e);
		} catch (IOException e) {
//			log().trace(e);
		} catch (Exception e) {
//			log().trace(e);
		} finally {
			Utils.safeClose(socket);
			this.thread = null;
			this.lastChecked = time;
			this.running = false;
		}
	}



	public boolean isRunning() {
		return this.running;
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
		synchronized(this.runLock) {
			this.enableNTP = enabled;
			if (!enabled) {
				this.localOffset = 0.0;
				this.lastChecked = 0.0;
			}
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
		return (long) getCurrentTime();
	}
	public double seconds() {
		return millis() / 1000.0;
	}



	protected static double fromUnixTimestamp(final double timestamp) {
		return (timestamp / 1000.0) + 2208988800.0;
	}
	protected static double fromUnixTimestamp() {
		return fromUnixTimestamp( (double) getSystemTime() );
	}



	public static String timestampToString(final double timestamp) {
		if (timestamp <= 0.0)
			return "0";
		return (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"))
				.format(new Date( (long)(timestamp * 1000.0) ))+
				(new DecimalFormat("0.000")
				.format( timestamp - ((long) timestamp) ));
	}
	public String getString() {
		return timestampToString(seconds());
	}



//TODO:
//	// logger
//	public static xLog log() {
//		return utils.log();
//	}



}
