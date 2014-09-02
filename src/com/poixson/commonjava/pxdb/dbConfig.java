package com.poixson.commonjava.pxdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.poixson.commonjava.Utils.CoolDown;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsThread;
import com.poixson.commonjava.Utils.xTime;
import com.poixson.commonjava.Utils.xTimeU;
import com.poixson.commonjava.xLogger.xLog;


public class dbConfig {

	private final String key;

	private final String host;
	private final int    port;
	private final String db;
	private final String user;
	private final String pass;
	private final String prefix;

	private volatile Connection connection = null;
	private volatile boolean failed = false;



	public static String load(final String host, final int port, final String db, final String user, final String pass, final String prefix) {
		if(utils.isEmpty(db)  ) throw new IllegalArgumentException("Database name not set");
		if(utils.isEmpty(user)) throw new IllegalArgumentException("Database username not set");
		if(utils.isEmpty(pass)) throw new IllegalArgumentException("Database password not set");
		final String hostStr = utils.isEmpty(host) ? "127.0.0.1" : host;
		final int portInt = ((port < 1 || port > 65536) ? 3306 : port);
		final String key = BuildKey(hostStr, portInt, db, user, prefix);
		// find existing config
		{
			final dbConfig config = dbManager.getConfig(key);
			if(config != null)
				return config.dbKey();
		}
		// new config
		final dbConfig config = new dbConfig(key, hostStr, portInt, db, user, pass, prefix);
		// hook back to db manager (register config)
		return dbManager.register(config);
	}
	// new config object
	private dbConfig(final String key, final String host, final int port,
			final String db, final String user, final String pass, final String prefix) {
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
	private final CoolDown coolFail = CoolDown.get("2s");
	public synchronized Connection getConnection() {
		if(this.failed) {
			if(this.coolFail.runAgain())
				log().severe("Database connection previously failed. We're not gonna hammer the server, but rather give up.");
			return null;
		}
		// try connecting 5 times max
		for(long i = 0; i < 5L; i++) {
			try {
				// make new connection
				final Connection conn = doConnect();
				// successful connection
				if(conn != null)
					return conn;
				final xTime sleepTime = xTime.get( new Long( (i * 2L) + 1L ), xTimeU.S);
				log().warning("Failed to connect to database, waiting "+
						sleepTime.toFullString()+" to try again.. "+this.key);
				utilsThread.Sleep(sleepTime);
			} catch (SQLException e) {
				if(e.getMessage().startsWith("Communications link failure"))
					log().severe("Failed to connect to database server! "+this.key);
				log().trace(e);
				break;
			} catch (InstantiationException e) {
				log().trace(e);
				break;
			} catch (IllegalAccessException e) {
				log().trace(e);
				break;
			} catch (ClassNotFoundException e) {
				log().trace(e);
				break;
			}
		}
		// failed to connect
		this.failed = true;
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


	// logger
	public static xLog log() {
		return dbManager.log();
	}


}
