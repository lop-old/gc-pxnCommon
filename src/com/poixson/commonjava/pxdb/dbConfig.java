package com.poixson.commonjava.pxdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.poixson.commonjava.Failure;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.Utils.xTimeU;
import com.poixson.commonjava.Utils.exceptions.RequiredArgumentException;
import com.poixson.commonjava.xLogger.xLog;


public class dbConfig {

	private final String key;

	private final String host;
	private final int    port;
	private final String user;
	private final String pass;
	private final String db;
	private final String prefix;

	private volatile int poolSizeWarn = 5;
	private volatile int poolSizeHard = 10;

	private volatile Connection connection = null;



	public static dbConfig load(final String host, final int port,
			final String db, final String user, final String pass, final String prefix) {
		if(utils.isEmpty(user)) throw new RequiredArgumentException("database username");
		if(utils.isEmpty(pass)) throw new RequiredArgumentException("database password");
		if(utils.isEmpty(db)  ) throw new RequiredArgumentException("database name");
		final String hostStr = utils.isEmpty(host) ? "127.0.0.1" : host;
		final int portInt = ((port < 1 || port > 65536) ? 3306 : port);
		final String key = BuildKey(hostStr, portInt, db, user, prefix);
		// find existing config
		{
			final dbConfig config = dbManager.getConfig(key);
			if(config != null)
				return config;
		}
		// new config
		{
			final dbConfig config = new dbConfig(key, hostStr, portInt, user, pass, db, prefix);
			// hook back to db manager (register config)
			if(dbManager.register(config))
				return config;
		}
		return null;
	}
	public static dbConfig load(final String host, final int port,
			final String user, final String pass, final String db, final String prefix,
			final int poolSizeWarn, final int poolSizeHard) {
		final dbConfig dbconfig = load(host, port, db, user, pass, prefix);
		if(dbconfig == null)
			return null;
		dbconfig.poolSizeWarn = poolSizeWarn;
		dbconfig.poolSizeHard = poolSizeHard;
		return dbconfig;
	}
	// new config object
	private dbConfig(final String key, final String host, final int port,
			final String user, final String pass, final String db, final String prefix) {
		this.key  = key;
		this.host = host;
		this.port = port;
		this.db   = db;
		this.user = user;
		this.pass = pass;
		this.prefix = prefix;
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}



	// connect to db
//	private final CoolDown coolFail = CoolDown.get("2s");
	public Connection getConnection() {
		if(Failure.hasFailed())
			return null;
//		if(this.failed) {
//			if(this.coolFail.runAgain())
//				log().severe("Database connection previously failed. We're not gonna hammer the server, but rather give up.");
//			return null;
//		}
		// try connecting (5 times max)
		for(long i = 0; i < 5L; i++) {
			try {
				// make new connection
				final Connection conn = doConnect();
				// successful connection
				if(conn != null)
					return conn;
				final xTime sleepTime = xTime.get(Long.valueOf( (i * 2L) + 1L ), xTimeU.S);
				log().warning("Failed to connect to database, waiting "+
						sleepTime.toFullString()+" to try again.. "+this.key);
				utilsThread.Sleep(sleepTime);
			} catch (SQLException e) {
				log().severe("Failed to connect to db server: "+this.key);
				final String msg = e.getMessage();
				if(msg.startsWith("Communications link failure"))
					Failure.fail("Failed to connect to database server");
				else
				if(msg.startsWith("Access denied for user"))
					Failure.fail("Failed to authenticate with database server");
				else
					Failure.fail("Problem connecting to database server");
				log().trace(e);
				return null;
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				Failure.fail("MySQL lib failure");
				log().trace(e);
				return null;
			}
		}
		// failed to connect
		Failure.fail("Failed to connect to database server, max attempts");
		return null;
	}
	// make new connection
	private Connection doConnect() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		if(this.connection != null) {
			try {
				if(!this.connection.isClosed())
					return this.connection;
			} catch (SQLException ignore) {}
			this.connection = null;
		}
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		final Connection conn = DriverManager.getConnection(
			BuildKey(this.host, this.port, this.db),
			this.user, this.pass
		);
		if(conn == null)    return null;
		if(conn.isClosed()) return null;
		// connection ok
		this.connection = conn;
		log().info("Connected to db: "+this.key);
		return this.connection;
	}



	// user@host:port/db/prefix
	private static String BuildKey(final String host, final int port,
			final String db, final String user, final String prefix) {
		final StringBuilder key = new StringBuilder();
		key.append("jdbc:mysql://");
		// user
		if(utils.notEmpty(user))
			key.append(user).append("@");
		// host:port
		key.append(host).append(":").append(port);
		// /db
		key.append("/").append(db);
		// /prefix
		if(utils.notEmpty(prefix))
			key.append("/").append(prefix);
		return key.toString();
	}
	// host:port/db
	private static String BuildKey(final String host, final int port, final String db) {
		return BuildKey(host, port, db, null, null);
	}



	@Override
	public boolean equals(final Object obj) {
		if(obj == null)
			return false;
		if(!(obj instanceof dbConfig))
			return false;
		if(utils.isEmpty(this.key))
			return false;
		return this.key.equals(
			((dbConfig) obj).dbKey()
		);
	}



	public String dbKey() {
		return this.key;
	}
	@Override
	public String toString() {
		return dbKey();
	}



	// get table prefix
	public String getTablePrefix() {
		if(utils.isEmpty(this.prefix))
			return "";
		return this.prefix;
	}



	public int getPoolSizeWarn() {
		return this.poolSizeWarn;
	}
	public int getPoolSizeHard() {
		return this.poolSizeHard;
	}



	// logger
	public static xLog log() {
		return dbManager.log();
	}



}
