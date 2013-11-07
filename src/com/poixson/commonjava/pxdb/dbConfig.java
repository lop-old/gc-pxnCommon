package com.poixson.commonjava.pxdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.poixson.commonjava.Utils.CoolDown;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.Utils.xTimeU;
import com.poixson.webxbukkit.WebAPI;


public class dbConfig {
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	// never change this while running
	private static volatile Boolean HASH_KEY = null;
	private static final Object lock_HASH_KEY = new Object();
	private static boolean get_HASH_KEY() {
		synchronized(lock_HASH_KEY) {
			if(HASH_KEY == null)
				HASH_KEY = !WebAPI.get().isDebug();
			return HASH_KEY;
		}
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
		if(dbKey == null || dbKey.isEmpty())
			return null;
		synchronized(configs) {
			if(configs.containsKey(dbKey))
				return configs.get(dbKey);
		}
		return null;
	}
	public static dbConfig load(String host, int port, String db, String user, String pass) {
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
			// hook back to db manager (register config)
			dbManager.register(config);
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
	}


	// connect to db
	private final CoolDown coolFail = CoolDown.get("2s");
	public synchronized Connection getConnection() {
		if(failed) {
			if(coolFail.runAgain())
				System.out.println("Database connection previously failed. We're not gonna hammer the server, but rather give up.");
			return null;
		}
		Connection conn = null;
		// try connecting 5 times max
		for(long i=0; i<5L; i++) {
			conn = doConnect();
			if(conn != null) break;
			xTime sleepTime = xTime.get( (i * 2L) + 1, xTimeU.S);
			System.out.println("Failed to connect to database, waiting "+sleepTime.toLongString()+" to try again..");
			utilsThread.Sleep(sleepTime);
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
		if(user == null || user.isEmpty())
			user = null;
		else
			user += "@";
		String key = utilsString.add(null, "mysql://", user, host, ":", Integer.toString(port), "/", db);
		if(get_HASH_KEY() && user != null)
			return utilsString.MD5(key);
		return key;
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
