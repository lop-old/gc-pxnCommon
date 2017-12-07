package com.poixson.utils.pxdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.poixson.utils.Failure;
import com.poixson.utils.ThreadUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.xTime;
import com.poixson.utils.xTimeU;
import com.poixson.utils.exceptions.RequiredArgumentException;
import com.poixson.utils.xLogger.xLog;


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
		if (Utils.isEmpty(user)) throw RequiredArgumentException.getNew("database username");
		if (Utils.isEmpty(pass)) throw RequiredArgumentException.getNew("database password");
		if (Utils.isEmpty(db)  ) throw RequiredArgumentException.getNew("database name");
		final String hostStr = (
				Utils.isEmpty(host)
				? "127.0.0.1"
				: host
		);
		final int portInt = ((port < 1 || port > 65536) ? 3306 : port);
		final String key = BuildKey(hostStr, portInt, db, user, prefix);
		// find existing config
		{
			final dbPool pool = dbManager.getPool(key);
			if (pool != null) {
				final dbConfig config = pool.getConfig();
				if (config != null)
					return config;
			}
		}
		// new config
		{
			final dbConfig config =
				new dbConfig(
					key,
					hostStr,
					portInt,
					user,
					pass,
					db,
					prefix
				);
			// hook back to db manager (register config)
			final dbPool existingPool =
				dbManager.register(config);
			if (existingPool != null) {
				final dbConfig existingConfig =
					existingPool.getConfig();
				if (existingConfig != null)
					return existingConfig;
			}
			return config;
		}
	}
	public static dbConfig load(final String host, final int port,
			final String user, final String pass, final String db, final String prefix,
			final int poolSizeWarn, final int poolSizeHard) {
		final dbConfig dbconfig =
			load(
				host,
				port,
				db,
				user,
				pass,
				prefix
			);
		if (dbconfig == null)
			return null;
		dbconfig.poolSizeWarn = poolSizeWarn;
		dbconfig.poolSizeHard = poolSizeHard;
		return dbconfig;
	}
	// new config object
	private dbConfig(final String key, final String host, final int port,
			final String user, final String pass, final String db, final String prefix) {
		this.key    = key;
		this.host   = host;
		this.port   = port;
		this.db     = db;
		this.user   = user;
		this.pass   = pass;
		this.prefix = prefix;
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}



	// connect to db
//	private final CoolDown coolFail = CoolDown.getNew("2s");
	public Connection getConnection() {
		if (Failure.hasFailed())
			return null;
//TODO:
//		if (this.failed) {
//			if (this.coolFail.runAgain()) {
//				log().severe("Database connection previously failed. We're not gonna hammer the server, but rather give up.");
//			}
//			return null;
//		}
		// try connecting (5 times max)
		for (long i = 0; i < 5L; i++) {
			try {
				// make new connection
				final Connection conn = doConnect();
				// successful connection
				if (conn != null) {
					return conn;
				}
				final xTime sleepTime = xTime.getNew(
					(i * 2L) + 1L,
					xTimeU.S
				);
				log().warning(
					"Failed to connect to database, waiting {} to try again.. {}",
					sleepTime.toFullString(),
					this.key
				);
				ThreadUtils.Sleep(sleepTime);
			} catch (SQLException e) {
				log().severe("Failed to connect to db server: {}", this.key);
				final String msg = e.getMessage();
				if (msg.startsWith("Communications link failure")) {
					Failure.fail("Failed to connect to database server");
				} else
				if (msg.startsWith("Access denied for user")) {
					Failure.fail("Failed to authenticate with database server");
				} else {
					Failure.fail("Problem connecting to database server");
				}
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
		if (this.connection != null) {
			try {
				if (!this.connection.isClosed()) {
					return this.connection;
				}
			} catch (SQLException ignore) {}
			this.connection = null;
		}
//TODO: should this be here?
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		final Connection conn = DriverManager.getConnection(
			BuildKey(this.host, this.port, this.db),
			this.user, this.pass
		);
		if (conn == null)    return null;
		if (conn.isClosed()) return null;
		// connection ok
		this.connection = conn;
		log().info("Connected to db: {}", this.key);
		return this.connection;
	}



	// user@host:port/db/prefix
	private static String BuildKey(final String host, final int port,
			final String db, final String user, final String prefix) {
		final StringBuilder key = new StringBuilder();
		key.append("jdbc:mysql://");
		// user
		if (Utils.notEmpty(user)) {
			key.append(user).append("@");
		}
		// host:port
		key.append(host).append(":").append(port);
		// /db
		key.append("/").append(db);
		// /prefix
		if (Utils.notEmpty(prefix)) {
			key.append("/").append(prefix);
		}
		return key.toString();
	}
	// host:port/db
	private static String BuildKey(final String host, final int port, final String db) {
		return BuildKey(
				host,
				port,
				db,
				null,
				null
		);
	}



	@Override
	public boolean equals(final Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof dbConfig))
			return false;
		if (Utils.isEmpty(this.key))
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
		return (
			Utils.isEmpty(this.prefix)
			? ""
			: this.prefix
		);
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
