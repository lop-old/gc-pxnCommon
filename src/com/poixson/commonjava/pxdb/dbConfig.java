package com.poixson.commonjava.pxdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.poixson.commonjava.xVars;
import com.poixson.commonjava.Utils.CoolDown;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsCrypt;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.Utils.xTimeU;
import com.poixson.commonjava.xLogger.xLog;


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
				HASH_KEY = new Boolean(!xVars.get().debug());
			return HASH_KEY.booleanValue();
		}
	}

	private final String host;
	private final int    port;
	private final String db;
	private final String user;
	private final String pass;

	private static final Map<String, dbConfig> configs = new HashMap<String, dbConfig>();
	private final String key;

	private volatile Connection connection = null;
	private volatile boolean failed = false;


	// get config object
	public static dbConfig get(final String dbKey) {
		if(dbKey == null || dbKey.isEmpty())
			return null;
		synchronized(configs) {
			if(configs.containsKey(dbKey))
				return configs.get(dbKey);
		}
		return null;
	}
	public static dbConfig load(final String host, final int port, final String db, final String user, final String pass) {
		final String hostStr = utils.isEmpty(host) ? "127.0.0.1" : host;
		final int portInt = (port < 1 || port > 65536) ? 3306 : port;
		if(db   == null || db.isEmpty()  ) throw new IllegalArgumentException("Database name not set!");
		if(user == null || user.isEmpty()) throw new IllegalArgumentException("Database username not set!");
		if(pass == null || pass.isEmpty()) throw new IllegalArgumentException("Database password not set!");
		String key = buildKey(hostStr, portInt, db, user);
		// find existing config
		synchronized(configs) {
			if(configs.containsKey(key))
				return configs.get(key);
			// new config
			dbConfig config = new dbConfig(hostStr, portInt, db, user, pass);
			configs.put(key, config);
			// hook back to db manager (register config)
			dbManager.register(config);
			return config;
		}
	}
	// new config object
	private dbConfig(final String host, final int port, final String db, final String user, final String pass) {
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
		if(this.failed) {
			if(this.coolFail.runAgain())
				log().severe("Database connection previously failed. We're not gonna hammer the server, but rather give up.");
			return null;
		}
		Connection conn = null;
		// try connecting 5 times max
		for(long i=0; i<5L; i++) {
			conn = doConnect();
			if(conn != null) break;
			xTime sleepTime = xTime.get( new Long( (i*2L)+1L ), xTimeU.S);
			log().warning("Failed to connect to database, waiting "+sleepTime.toFullString()+" to try again..");
			utilsThread.Sleep(sleepTime);
		}
		// failed to connect
		if(conn == null) {
			this.failed = true;
			return null;
		}
		// successful connection
		return conn;
	}
	private Connection doConnect() {
		if(this.connection != null) {
			try {
				if(!this.connection.isClosed())
					return this.connection;
			} catch (SQLException ignore) {}
			this.connection = null;
		}
//		parent.log.info("db", "Making new db connection.. [ "+Integer.toString(getId())+" ]");
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			this.connection = DriverManager.getConnection(
					"jdbc:" + buildKey(this.host, this.port, this.db, null),
					this.user, this.pass
				);
			if(this.connection.isClosed())
				return null;
		} catch (SQLException e) {
//			parent.plugin.errorMsg("SQL Error!");
			log().trace(e);
			this.connection = null;
		} catch (InstantiationException e) {
//			parent.plugin.errorMsg("Unable to create database driver!");
			log().trace(e);
			this.connection = null;
		} catch (IllegalAccessException e) {
//			parent.plugin.errorMsg("Unable to create database driver!");
			log().trace(e);
			this.connection = null;
		} catch (ClassNotFoundException e) {
//			parent.plugin.errorMsg("Unable to load database driver!");
			log().trace(e);
			this.connection = null;
		}
		if(this.connection == null) {
//			parent.plugin.errorMsg("There was a problem getting the MySQL connection!!!");
			return null;
		}
		// connection ok
		log().info("Connected to db: "+this.key);
		return this.connection;
	}


	// user@host:port/db
	private static String buildKey(final String host, final int port, final String db, final String user) {
		final String userStr = utils.isEmpty(user) ? null : user+"@";
		final String key = utilsString.add(null, "mysql://", userStr, host, ":", Integer.toString(port), "/", db);
		if(get_HASH_KEY() && user != null)
			return utilsCrypt.MD5(key);
		return key;
	}


	@Override
	public boolean equals(final Object obj) {
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
		return this.key;
	}


	// logger
	public static xLog log() {
		return dbManager.log();
	}


}
