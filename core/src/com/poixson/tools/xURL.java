package com.poixson.tools;

import com.poixson.utils.NumberUtils;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;

// protocol:[//[user[:password]@]host[:port]][/path]
public class xURL {

	public String protocol = null;
	public String user     = null;
	public String pass     = null;
	public String host     = null;
	public int    port     = -1;
	public String path     = null;



	public static xURL getNew() {
		return new xURL();
	}
	public static xURL getNew(final String uri) {
		return new xURL(uri);
	}
	public static xURL getNew(final String protocol, final String host) {
		return new xURL(protocol, host);
	}
	public static xURL getNew(final String host, final int port) {
		return new xURL(host, port);
	}
	public static xURL getNew(final String protocol, final String host, final int port) {
		return new xURL(protocol, host, port);
	}
	public static xURL getNew(final String protocol, final String host, final int port, final String path) {
		return new xURL(protocol, host, port, path);
	}
	public static xURL getNew(final String user, final String pass,
			final String protocol, final String host, final int port, final String path) {
		return new xURL(protocol, user, pass, host, port, path);
	}



	public xURL() {
	}
	public xURL(final String uri) {
		this();
		this.setURI(uri);
	}
	public xURL(final String protocol, final String host) {
		this();
		this.protocol = protocol;
		this.host     = host;
	}
	public xURL(final String host, final int port) {
		this();
		this.host     = host;
		this.port     = port;
	}
	public xURL(final String protocol, final String host, final int port) {
		this();
		this.protocol = protocol;
		this.host     = host;
		this.port     = port;
	}
	public xURL(final String protocol, final String host, final int port, final String path) {
		this();
		this.protocol = protocol;
		this.host     = host;
		this.port     = port;
		this.path     = path;
	}
	public xURL(final String user, final String pass,
			final String protocol, final String host, final int port, final String path) {
		this();
		this.protocol = protocol;
		this.user     = user;
		this.pass     = pass;
		this.host     = host;
		this.port     = port;
		this.path     = path;
	}



	public xURL reset() {
		this.protocol = null;
		this.user     = null;
		this.pass     = null;
		this.host     = null;
		this.port     = -1;
		this.path     = null;
		return this;
	}



	public xURL setURI(final String uri) {
		String buf = uri;
		int pos;
		this.reset();
		// protocol://
		{
			pos = buf.indexOf("//");
			if (pos != -1) {
				this.protocol =
					StringUtils.TrimEnd(
						buf.substring(0, pos),
						":"
					);
				buf = buf.substring(pos + 2);
			}
		}
		// user:pass
		{
			pos = buf.indexOf('@');
			if (pos != -1) {
				final String str = buf.substring(0, pos);
				buf = buf.substring(pos + 1);
				pos = str.indexOf(':');
				if (pos == -1) {
					this.user = str;
				} else {
					this.user = str.substring(0, pos);
					this.pass = str.substring(pos + 1);
				}
			}
		}
		// host:port
		{
			pos = buf.indexOf('/');
			if (pos != -1) {
				final String str = buf.substring(0, pos);
				buf = buf.substring(pos);
				pos = str.indexOf(':');
				if (pos != -1) {
					this.host = str.substring(0, pos);
					final Integer i =
						NumberUtils.toInteger(
							str.substring(pos + 1)
						);
					this.port = (
						i == null
						? -1
						: i.intValue()
					);
				}
			}
		}
		// path
		{
			if (Utils.notEmpty(buf)) {
				this.path = buf;
			}
		}
		return this;
	}



	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder();
		final String protocol = this.protocol;
		final String user     = this.user;
		final String pass     = this.pass;
		final String host     = this.host;
		final int    port     = this.port;
		final String path     = this.path;
		if (Utils.notEmpty(protocol)) {
			str.append(protocol).append("://");
		}
		if (Utils.notEmpty(user) || Utils.notEmpty(pass)) {
			if (Utils.notEmpty(user)) {
				str.append(user);
			}
			if (Utils.notEmpty(pass)) {
				str.append(':').append(pass);
			}
			str.append('@');
		}
		str.append(host);
		if (port > 0) {
			str.append(':').append(port);
		}
		if (Utils.notEmpty(path)) {
			str.append(path);
		}
		return str.toString();
	}



	// protocol
	public xURL setProtocol(final String protocol) {
		this.protocol = protocol;
		return this;
	}
	public String getProtocol() {
		return this.protocol;
	}
	public boolean hasProtocol() {
		return Utils.notEmpty(this.protocol);
	}



	// user
	public xURL setUser(final String user) {
		this.user = user;
		return this;
	}
	public String getUser() {
		return this.user;
	}
	public boolean hasUser() {
		return Utils.notEmpty(this.user);
	}



	// pass
	public xURL setPass(final String pass) {
		this.pass = pass;
		return this;
	}
	public String getPass() {
		return this.pass;
	}
	public boolean hasPass() {
		return Utils.notEmpty(this.pass);
	}



	// host
	public xURL setHost(final String host) {
		this.host = host;
		return this;
	}
	public String getHost() {
		return this.host;
	}
	public boolean hasHost() {
		return Utils.notEmpty(this.host);
	}



	// port
	public xURL setPort(final int port) {
		this.port = (
			port > 0
			? port
			: -1
		);
		return this;
	}
	public int getPort() {
		final int port = this.port;
		return (
			port > 0
			? port
			: -1
		);
	}
	public boolean hasPort() {
		return (this.port > 0);
	}



	// path
	public xURL setPath(final String path) {
		this.path = path;
		return this;
	}
	public String getPath() {
		return this.path;
	}
	public boolean hasPath() {
		return Utils.notEmpty(this.path);
	}



}
