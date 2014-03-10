package com.growcontrol.gcCommon.pxnClock;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.growcontrol.gcCommon.pxnLogger.pxnLog;
import com.growcontrol.gcCommon.pxnLogger.pxnLogger;
import com.growcontrol.gcCommon.pxnUtils.pxnUtilsMath;


public class pxnClock {
	// instance
	private static volatile pxnClock clock = null;
	private static final Object lock = new Object();

	protected volatile boolean enableNTP  = true;
	protected volatile boolean active = false;
	protected volatile String timeServer = "pool.ntp.org";

	protected volatile double localOffset = 0.0;
	protected volatile double lastChecked = 0.0; // system time

	// instance lock
	protected final Object threadLock = new Object();
	protected Thread updateThread = null;


	// get clock
	public static pxnClock get(boolean blocking) {
		if(clock == null) {
			synchronized(lock) {
				if(clock == null) {
					clock = new pxnClock();
					clock.Update(blocking);
				}
			}
		}
		return clock;
	}
	public static pxnClock getBlocking() {
		return get(true);
	}
	public static pxnClock get() {
		return get(true);
	}
	// new instance
	protected pxnClock() {}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}


//	// static update
//	public static double getLocalOffsetNow() {
//		return getLocalOffsetNow(null);
//	}
//	public static double getLocalOffsetNow(String timeServer) {
//		pxnClock clock = getBlocking();
//		if(timeServer != null && !timeServer.isEmpty())
//			clock.setTimeServer(timeServer);
//		clock.Update(false);
//		return clock.getLocalOffset();
//	}


	// update from time server
	public void Update(boolean enabled, boolean threaded) {
		setNTP(enabled);
		Update(threaded);
	}
	public void Update(String timeServer, boolean threaded) {
		setTimeServer(timeServer);
		Update(threaded);
	}
	public synchronized void Update(boolean blocking) {
		if(!enableNTP) return;
		if(blocking) {
			// run blocking
			doUpdate();
		} else {
			// run threaded
			if(updateThread == null) {
				updateThread = new Thread() {
					@Override
					public void run() {
						doUpdate();
					}
				};
			} else {
				// start thread
				if(!updateThread.isAlive())
					updateThread.start();
			}
		}
	}


	// run time query
	protected void doUpdate() {
		if(!enableNTP) return;
		synchronized(lock) {
			if(active) return;
			active = true;
		}
		double time = System.currentTimeMillis();
		if(lastChecked != 0.0 && ((time-lastChecked)/1000.0) < 60.0) return;
		lastChecked = time;
		DatagramSocket socket = null;;
		try {
			socket = new DatagramSocket();
socket.setSoTimeout(500);
			InetAddress address = InetAddress.getByName(timeServer);
			byte[] buf = new ntpMessage().toByteArray();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);
			ntpMessage.encodeTimestamp(packet.getData(), 40, fromUnixTimestamp());
			socket.send(packet);
			socket.receive(packet);
			ntpMessage msg = new ntpMessage(packet.getData());
			// calculate local offset
			time = System.currentTimeMillis();
			localOffset = ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - fromUnixTimestamp(time))) / 2.0;
			// less than 100ms
			if(localOffset < 0.1 && localOffset > -0.1) {
				pxnLog.get().debug("System time only off by "+pxnUtilsMath.FormatDecimal("0.000", localOffset)+", not adjusting.");
				localOffset = 0.0;
			} else {
				pxnLogger log = pxnLog.get();
				log.info("Internal time adjusted by "+(localOffset>0 ? "+" : "-")+pxnUtilsMath.FormatDecimal("0.000", localOffset)+" seconds");
				log.debug("System time:   "+timestampToString(time/1000.0));
				log.debug("Adjusted time: "+getString());
			}
			// clean up
			msg = null;
			packet = null;
			address = null;
		} catch (UnknownHostException | SocketTimeoutException e) {
			localOffset = 0.0;
			pxnLog.get().exception(e);
		} catch (IOException e) {
			localOffset = 0.0;
			pxnLog.get().exception(e);
		} catch (Exception e) {
			localOffset = 0.0;
			pxnLog.get().exception(e);
		} finally {
			// clean up
			if(socket != null)
				socket.close();
			socket = null;
		}
		active = false;
//double destinationTimestamp = fromUnixTimestamp();
//double roundTripDelay = (destinationTimestamp-msg.originateTimestamp) - (msg.transmitTimestamp-msg.receiveTimestamp);
//double localClockOffset = ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - destinationTimestamp)) / 2;
//GrowControl.log.severe(msg.toString());
//System.out.println("Dest. timestamp:     " + NtpMessage.timestampToString(destinationTimestamp));
//System.out.println("Round-trip delay: " + new DecimalFormat("0.00").format(roundTripDelay*1000) + " ms");
//System.out.println("Local clock offset: " + new DecimalFormat("0.00").format(localClockOffset*1000) + " ms");
	}
	// has update run?
	public boolean hasUpdated() {
		if(!enableNTP) return true;
		return (localOffset != 0.0) && (lastChecked != 0.0);
	}
	public boolean isUpdating() {
		return active;
	}


	// convert from 1970ms to 1900s
	protected static double fromUnixTimestamp(double timestamp) {
		return (timestamp/1000.0) + 2208988800.0;
	}
	protected static double fromUnixTimestamp() {
		return fromUnixTimestamp(System.currentTimeMillis());
	}


	// display time formatted: 29-Jun-2012 03:07:04.794
	public static String timestampToString(double timestamp) {
		if(timestamp <= 0.0) return "0";
		return new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date( (long)(timestamp*1000.0) )) +
			new DecimalFormat("0.000").format( timestamp - ((long)timestamp) );
	}


	// get corrected time
	public double getLocalOffset() {
		return localOffset;
	}
	public double Seconds() {
		return Millis() / 1000.0;
	}
	public long Millis() {
		return System.currentTimeMillis() + ((long)(localOffset*1000.0));
	}
	public String getString() {
		return timestampToString(Seconds());
	}


	// enable/disable NTP
	public boolean isEnabled() {
		return enableNTP;
	}
	public void setNTP(boolean enabled) {
		this.enableNTP = enabled;
		if(!enabled) {
			localOffset = 0.0;
			lastChecked = 0.0;
		}
	}


	// set time server host
	public void setTimeServer(String timeServer) {
		if(timeServer == null || timeServer.isEmpty()) throw new NullPointerException("timeServer host cannot be null!");
		this.timeServer = timeServer;
	}


}
