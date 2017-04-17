package com.poixson.utils;
/*
 * cron4j - A pure Java cron-like scheduler
 *
 * Copyright (C) 2007-2010 Carlo Pelliccia (www.sauronsoftware.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version
 * 2.1, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License 2.1 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License version 2.1 along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;


/**
 * A GUID generator.
 * @author Carlo Pelliccia
 */
public final class SystemHash {
	private SystemHash() {}

	private static final String DESCRIPTOR = getMachineDescriptor();



	/**
	 * Generates a GUID (48 chars).
	 * @return The generated GUID.
	 */
	public static String generate() {
		final int rnd = NumberUtils.getRandom(0, Integer.MAX_VALUE);
		final StringBuffer str = new StringBuffer();
		encode(str, DESCRIPTOR);
		encode(str, Runtime.getRuntime());
		encode(str, Thread.currentThread());
		encode(str, System.currentTimeMillis());
		encode(str, rnd);
		return str.toString();
	}



	// ==================================================



	/**
	 * Calculates a machine id, as an integer value.
	 * @return The calculated machine id.
	 */
	private static String getMachineDescriptor() {
		final StringBuffer desc = new StringBuffer();
		desc.append(System.getProperty("os.name"));
		desc.append("::");
		desc.append(System.getProperty("os.arch"));
		desc.append("::");
		desc.append(System.getProperty("os.version"));
		desc.append("::");
		desc.append(System.getProperty("user.name"));
		desc.append("::");
		final StringBuffer b = buildNetworkInterfaceDescriptor();
		if (b != null) {
			desc.append(b);
		} else {
			InetAddress addr = null;
			try {
				addr = InetAddress.getLocalHost();
				desc.append(addr.getHostAddress());
			} catch (UnknownHostException ignore) {}
		}
		return desc.toString();
	}
	/**
	 * Builds a descriptor fragment using the {@link NetworkInterface} class,
	 * available since Java 1.4.
	 * @return A descriptor fragment, or null if the method fails.
	 */
	private static StringBuffer buildNetworkInterfaceDescriptor() {
		final Enumeration<NetworkInterface> e;
		try {
			e = NetworkInterface.getNetworkInterfaces();
		} catch (Throwable ignore) {
			return null;
		}
		final StringBuffer str = new StringBuffer();
		while (e.hasMoreElements()) {
			NetworkInterface net = e.nextElement();
			final StringBuffer str1 = getMACAddressDescriptor(net);
			final StringBuffer str2 = getInetAddressDescriptor(net);
			final StringBuffer str3 = new StringBuffer();
			if (str1 != null) {
				str3.append(str1);
			}
			if (str2 != null) {
				if (str3.length() > 0)
					str3.append('=');
				str3.append(str2);
			}
			if (str3.length() > 0) {
				if (str.length() > 0)
					str.append(';');
				str.append(str3);
			}
		}
		return str;
	}
	/**
	 * Builds a descriptor fragment using the machine MAC address.
	 * @return A descriptor fragment, or null if the method fails.
	 */
	private static StringBuffer getMACAddressDescriptor(final NetworkInterface net) {
		byte[] haddr = null;
		try {
			haddr = net.getHardwareAddress();
		} catch (Throwable ignore) {}
		final StringBuffer str = new StringBuffer();
		if (haddr != null) {
			for (int i = 0; i < haddr.length; i++) {
				if (str.length() > 0) {
					str.append("-");
				}
				final String hex = Integer.toHexString(0xff & haddr[i]);
				if (hex.length() == 1) {
					str.append('0');
				}
				str.append(hex);
			}
		}
		return str;
	}
	/**
	 * Builds a descriptor fragment using the machine inet address.
	 * @return A descriptor fragment, or null if the method fails.
	 */
	private static StringBuffer getInetAddressDescriptor(NetworkInterface net) {
		final StringBuffer str = new StringBuffer();
		final Enumeration<InetAddress> e = net.getInetAddresses();
		while (e.hasMoreElements()) {
			final InetAddress addr = e.nextElement();
			if (str.length() > 0) {
				str.append(',');
			}
			str.append(addr.getHostAddress());
		}
		return str;
	}



	// ==================================================



	/**
	 * Encodes an object and appends it to the buffer.
	 * @param str The buffer.
	 * @param obj The object.
	 */
	private static void encode(final StringBuffer str, final Object obj) {
		encode(str, obj.hashCode());
	}
	/**
	 * Encodes an integer value and appends it to the buffer.
	 * @param str The buffer.
	 * @param value The value.
	 */
	private static void encode(final StringBuffer str, final int value) {
		final String hex = Integer.toHexString(value);
		final int hexSize = hex.length();
		for (int i = 8; i > hexSize; i--) {
			str.append('0');
		}
		str.append(hex);
	}
	/**
	 * Encodes a long value and appends it to the buffer.
	 * @param str The buffer.
	 * @param value The value.
	 */
	private static void encode(final StringBuffer str, final long value) {
		final String hex = Long.toHexString(value);
		final int hexSize = hex.length();
		for (int i = 16; i > hexSize; i--) {
			str.append('0');
		}
		str.append(hex);
	}



}
