package com.poixson.commonjava.pxdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xTime;


public class dbConfig {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private final String host;
	private final int    port;
	private final String db;
	private final String user;
	private final String pass;

	private static final Map<String, dbConfig> configs = new HashMap<String, dbConfig>();
	private final String key;

	private volatile Connection conn = null;
	private volatile boolean failed = false;


	// get config object
	public static dbConfig get(String dbKey) {
		synchronized(configs) {
			if(configs.containsKey(dbKey))
				return configs.get(dbKey);
		}
		return null;
	}
	public static dbConfig get(String host, int port, String db, String user, String pass) {
		if(host == null || host.isEmpty() || host.equalsIgnoreCase("localhost"))
			host = "127.0.0.1";
		if(port < 1 || port > 65536)
			port = 3306;
		if(db   == null || db.isEmpty()  ) throw new IllegalArgumentException("Database name not set!");
		if(user == null || user.isEmpty()) throw new IllegalArgumentException("Database username not set!");
		if(pass == null || pass.isEmpty()) throw new IllegalArgumentException("Database password not set!");
		String key = buildKey(host, port, db, user);
		// find existing config
		synchronized(configs) {
			if(configs.containsKey(key))
				return configs.get(key);
			// new config
			dbConfig config = new dbConfig(host, port, db, user, pass);
			configs.put(key, config);
			return config;
		}
	}
	// new config object
	private dbConfig(String host, int port, String db, String user, String pass) {
		this.host = host;
		this.port = port;
		this.db   = db;
		this.user = user;
		this.pass = pass;
		this.key = buildKey(host, port, db, user);
		// hook back to db manager (register config)
		dbManager.get().newConfig(this);
	}


	// connect to db
	public synchronized Connection getConnection() {
		if(failed) return null;
		Connection conn = null;
		// try connecting 5 times max
		for(int i=0; i<5; i++) {
			conn = doConnect();
			if(conn != null) break;
			utilsThread.Sleep(xTime.get("1s"));
		}
		// failed to connect
		if(conn == null) {
			failed = true;
			return null;
		}
		// successful connection
		return conn;
	}
	private Connection doConnect() {
		if(conn != null) {
			try {
				if(!conn.isClosed())
					return conn;
			} catch (SQLException ignore) {}
			conn = null;
		}
//		parent.log.info("db", "Making new db connection.. [ "+Integer.toString(getId())+" ]");
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(
					"jdbc:" + buildKey(host, port, db, null),
					user, pass
				);
			if(conn.isClosed())
				return null;
		} catch (SQLException e) {
//			parent.plugin.errorMsg("SQL Error!");
			e.printStackTrace();
			conn = null;
		} catch (InstantiationException e) {
//			parent.plugin.errorMsg("Unable to create database driver!");
			e.printStackTrace();
			conn = null;
		} catch (IllegalAccessException e) {
//			parent.plugin.errorMsg("Unable to create database driver!");
			e.printStackTrace();
			conn = null;
		} catch (ClassNotFoundException e) {
//			parent.plugin.errorMsg("Unable to load database driver!");
			e.printStackTrace();
			conn = null;
		}
		if(conn == null) {
//			parent.plugin.errorMsg("There was a problem getting the MySQL connection!!!");
			return null;
		}
		// connection ok
		return conn;
	}


	// user@host:port/db
	private static String buildKey(String host, int port, String db, String user) {
		if(user != null && !user.isEmpty())
			user += "@";
		return utilsString.add(null, "mysql://", user, host, ":", Integer.toString(port), "/", db);
	}


	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof dbConfig))
			return false;
		if(this.key == null || this.key.isEmpty())
			return false;
		return this.key.equals(
			((dbConfig) obj).getKey()
		);
	}
	@Override
	public String toString() {
		return getKey();
	}
	public String getKey() {
		return key;
	}


}
