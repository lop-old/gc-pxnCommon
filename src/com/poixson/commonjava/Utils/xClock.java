package com.poixson.commonjava.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.poixson.commonjava.xLogger.xLog;


public class xClock {

	public static final String DEFAULT_TIMESERVER = "pool.ntp.org";
	private volatile String timeserver = null;
	private volatile boolean enableNTP = true;

	private volatile double localOffset = 0.0;
	private volatile double lastChecked = 0.0;

	// update thread
	private volatile Thread thread = null;
	private volatile boolean blocking = true;
	private volatile Boolean running = false;

	// clock instance
	private static volatile xClock instance = null;
	private static final Object lock = new Object();


	public static xClock get(final boolean blocking) {
		if(instance == null) {
			synchronized(lock) {
				if(instance == null)
					instance = new xClock(blocking);
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
		if(!enableNTP) return;
		if(running) return;
		synchronized(running) {
			if(running) return;
			running = true;
			// wait for update
			if(blocking) {
				doUpdate();
			// update threaded
			} else {
				if(thread == null)
					thread = new Thread() {
						@Override
						public void run() {
							doUpdate();
						}
					};
				thread.start();
			}
		}
	}


	protected void doUpdate() {
		if(!enableNTP) return;
		if(!running) return;
		double time = getSystemTime();
		// checked in last 60 seconds
		if(lastChecked != 0.0 && ((time - lastChecked) < 60.0)) return;
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(500);
			final InetAddress address = InetAddress.getByName(timeserver);
			byte[] buf = new ntpMessage().toByteArray();
			final DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);
			ntpMessage.encodeTimestamp(packet.getData(), 40, fromUnixTimestamp());
			socket.send(packet);
			socket.receive(packet);
			final ntpMessage msg = new ntpMessage(packet.getData());
			// calculate local offset for correction
			time = getSystemTime();
			localOffset = ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - fromUnixTimestamp(time))) / 2.0;
			// less than 100ms
			if(localOffset < 0.1 && localOffset > -0.1) {
				log().info("System time only off by "+utilsMath.FormatDecimal("0.000", localOffset)+", ignoring adjustment.");
				localOffset = 0.0;
			} else {
				log().info("Internal time adjusted by "+(localOffset>0 ? "+" : "-")+utilsMath.FormatDecimal("0.000", localOffset)+" seconds");
				log().info("System time:   "+timestampToString(time / 1000.0));
				log().info("Internal time: "+getString());
			}
		} catch (SocketException e) {
			log().trace(e);
		} catch (UnknownHostException e) {
			log().trace(e);
		} catch (IOException e) {
			log().trace(e);
		} catch (Exception e) {
			log().trace(e);
		} finally {
			if(socket != null)
				socket.close();
			socket = null;
		}
		lastChecked = time;
		running = false;
	}


	public boolean isRunning() {
		return running;
	}


	public String getTimeServer() {
		if(utils.isEmpty(timeserver))
			return DEFAULT_TIMESERVER;
		return timeserver;
	}
	public void setTimeServer(final String host) {
		timeserver = host;
	}
	public void setEnabled(final boolean enabled) {
		synchronized(running) {
			this.enableNTP = enabled;
			if(!enabled) {
				localOffset = 0.0;
				lastChecked = 0.0;
			}
		}
	}


	/**
	 * Get current time from system.
	 * @return
	 */
	public static double getSystemTime() {
		return System.currentTimeMillis();
	}
	/**
	 * Get current time adjusted by NTP.
	 * @return
	 */
	public double getCurrentTime() {
		return getSystemTime() - localOffset;
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
		return fromUnixTimestamp(getSystemTime());
	}


	public static String timestampToString(final double timestamp) {
		if(timestamp <= 0.0) return "0";
		return new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date( (long)(timestamp * 1000.0) ))+
				(new DecimalFormat("0.000").format( timestamp - ((long) timestamp) ));
	}
	public String getString() {
		return timestampToString(seconds());
	}


	// logger
	public static xLog log() {
		return xLog.getRoot();
	}


}
