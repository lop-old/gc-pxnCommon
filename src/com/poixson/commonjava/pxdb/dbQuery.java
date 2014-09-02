package com.poixson.commonjava.pxdb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;

import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsMath;
import com.poixson.commonjava.Utils.utilsSan;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.xLogger.xLevel;
import com.poixson.commonjava.xLogger.xLog;


public class dbQuery {

	protected final dbWorker worker;
	protected final String tablePrefix;
	protected volatile PreparedStatement st = null;
	protected volatile ResultSet rs = null;
	protected volatile String sql = null;
	protected volatile boolean quiet = false;
	protected volatile int resultInt = -1;
	private final Object lock = new Object();

	// args
	protected volatile int paramCount = 0;
	protected volatile String[] args = null;
	private static final String ARG_PRE   = "[";
	private static final String ARG_DELIM = "|";
	private static final String ARG_POST  = "]";


	// new query
	public static dbQuery get(final String dbKey) {
		final dbWorker worker = dbManager.getWorkerLock(dbKey);
		if(worker == null)
			return null;
		return new dbQuery(worker);
	}
	// new query (must already have lock)
	public dbQuery(final dbWorker worker) {
		if(worker == null) throw new NullPointerException("worker cannot be null");
		this.worker = worker;
		this.tablePrefix = worker.getTablePrefix();
	}


	// prepared query
	public dbQuery Prepare(final String sql) throws SQLException {
		if(utils.isEmpty(sql)) throw new IllegalArgumentException("sql cannot be empty");
		synchronized(this.lock) {
			if(!this.worker.inUse()) {
				log().trace(new IllegalAccessException("dbWorker not locked!"));
				return null;
			}
			this.clean();
			this.sql = sql.replace(
				"_table_",
				(this.tablePrefix == null ? "" : this.tablePrefix)
			);
			try {
				// prepared statement
				this.st = this.worker.getConnection().prepareStatement(this.sql);
				// parameters for debugging
				this.paramCount = this.st.getParameterMetaData().getParameterCount();
				this.args = new String[this.paramCount];
			} catch (SQLNonTransientConnectionException | NullPointerException ignore) {
				log().severe("db connection closed!");
				this.close();
				return null;
			}
		}
		return this;
	}
	public boolean Prep(final String sql) {
		if(utils.isEmpty(sql)) throw new IllegalArgumentException("sql cannot be empty");
		try {
			if(this.Prepare(sql) != null)
				return true;
		} catch (SQLException e) {
			log().trace(e);
		}
		this.clean();
		return false;
	}



	// execute query
	public boolean Execute() throws SQLException {
		synchronized(this.lock) {
			if(!this.worker.inUse()) {
				log().trace(new IllegalAccessException("dbWorker not locked!"));
				return false;
			}
			if(this.st == null) return false;
			if(utils.isEmpty(this.sql)) return false;
			String str = this.sql;
			while(str.startsWith(" "))
				str = str.substring(1);
			if(str.isEmpty()) return false;
			final String queryType;
			{
				final int pos = str.indexOf(" ");
				queryType = (pos == -1) ?
						str.toUpperCase() :
						str.substring(0, pos).toUpperCase();
			}
//			if(!quiet)
//				getLog().debug("query", this.sql+(args.isEmpty() ? "" : "  ["+args+" ]") );
			try {
				// log query
				this.worker.logDesc();
				if(log().isLoggable(xLevel.FINEST)) {
					// replace ? with values
					log().finest(
						"("+Integer.toString(this.worker.getId())+") QUERY: "+
						utilsString.replaceWith("?", this.args, str)
					);
				}
				// execute query
				if(queryType.equals("INSERT") || queryType.equals("UPDATE") || queryType.equals("CREATE") || queryType.equals("DELETE"))
					this.resultInt = this.st.executeUpdate();
				else
					this.rs = this.st.executeQuery();
			} catch (SQLNonTransientConnectionException e) {
				log().severe("db connection closed!");
				this.close();
				return false;
			} catch (SQLException e) {
				this.clean();
				throw e;
			}
		}
		return true;
	}
	public boolean Execute(final String sql) throws SQLException {
		if(utils.isEmpty(sql))
			return false;
		if(Prepare(sql) == null)
			return false;
		return Execute();
	}



	public boolean Exec() {
		try {
			return this.Execute();
		} catch (SQLException e) {
			log().trace(e);
			this.clean();
		}
		return false;
	}
	public boolean Exec(final String sql) {
		try {
			return this.Execute(sql);
		} catch (SQLException e) {
			log().trace(e);
			this.clean();
		}
		return false;
	}



	public void desc(final String desc) {
		this.worker.desc(desc);
	}



	// set quiet mode
	public boolean quiet() {
		return this.quiet(true);
	}
	public boolean quiet(final boolean setQuiet) {
		this.quiet = setQuiet;
		return this.quiet;
	}


	// get db key
	public String dbKey() {
		if(this.worker == null)
			return null;
		return this.worker.dbKey();
	}
//	// table prefix
//	public String getTablePrefix() {
//		return this.tablePrefix;
//	}



	// clean vars
	public void clean() {
		synchronized(this.lock) {
			if(this.rs != null) {
				try {
					this.rs.close();
				} catch (SQLException ignore) {}
				this.rs = null;
			}
			if(this.st != null) {
				try {
					this.st.close();
				} catch (SQLException ignore) {}
				this.st = null;
			}
			this.sql = null;
			this.paramCount = 0;
			this.args = null;
			this.quiet = false;
			this.resultInt = -1;
		}
	}
	public void free() {
		this.clean();
		this.worker.free();
	}
	public void close() {
		this.clean();
		this.worker.close();
	}


	// san string for sql
	public static String san(final String text) {
		return utilsSan.AlphaNumSafe(text);
	}


	// has next row
	public boolean hasNext() {
		synchronized(this.lock) {
			if(this.rs == null) return false;
			try {
				return this.rs.next();
			} catch (SQLException e) {
				log().trace(e);
			}
		}
		return false;
	}


	public ResultSet getResultSet() {
		return this.rs;
	}



	// result count
	public int getResultInt() {
		return this.resultInt;
	}
	public int getAffectedRows() {
		return getResultInt();
	}
	public int getInsertId() {
		return getResultInt();
	}


	// query parameters
	public dbQuery setString(final int index, final String value) {
		synchronized(this.lock) {
			if(this.st == null) return null;
			try {
				this.st.setString(index, value);
				if(this.paramCount > 0)
					this.args[index-1] = ARG_PRE+"str"+ARG_DELIM+value+ARG_POST;
			} catch (SQLException e) {
				log().trace(e);
				this.clean();
				return null;
			}
		}
		return this;
	}
	// set int
	public dbQuery setInt(final int index, final int value) {
		synchronized(this.lock) {
			if(this.st == null) return null;
			try {
				this.st.setInt(index, value);
				if(this.paramCount > 0)
					this.args[index-1] = ARG_PRE+"int"+ARG_DELIM+Integer.toString(value)+ARG_POST;
			} catch (SQLException e) {
				log().trace(e);
				this.clean();
				return null;
			}
		}
		return this;
	}
	// set long
	public dbQuery setLong(final int index, final long value) {
		synchronized(this.lock) {
			if(this.st == null) return null;
			try {
				this.st.setLong(index, value);
				if(this.paramCount > 0)
					this.args[index-1] = ARG_PRE+"lng"+ARG_DELIM+Long.toString(value)+ARG_POST;
			} catch (SQLException e) {
				log().trace(e);
				this.clean();
				return null;
			}
		}
		return this;
	}
	// set decimal
	public dbQuery setDecimal(final int index, final double value) {
		if(this.setDouble(index, value) == null)
			return null;
		if(this.paramCount > 0)
			this.args[index-1] = ARG_PRE+"dec"+ARG_DELIM+Double.toString(value)+ARG_POST;
		return this;
	}
	// set double
	public dbQuery setDouble(final int index, final double value) {
		synchronized(this.lock) {
			if(this.st == null) return null;
			try {
				this.st.setDouble(index, value);
				if(this.paramCount > 0)
					this.args[index-1] = ARG_PRE+"dbl"+ARG_DELIM+Double.toString(value)+ARG_POST;
			} catch (SQLException e) {
				log().trace(e);
				this.clean();
				return null;
			}
		}
		return this;
	}
	// set float
	public dbQuery setFloat(final int index, final float value) {
		synchronized(this.lock) {
			if(this.st == null) return null;
			try {
				this.st.setFloat(index, value);
				if(this.paramCount > 0)
					this.args[index-1] = ARG_PRE+"flt"+ARG_DELIM+Float.toString(value)+ARG_POST;
			} catch (SQLException e) {
				log().trace(e);
				this.clean();
				return null;
			}
		}
		return this;
	}
	// set boolean
	public dbQuery setBool(final int index, final boolean value) {
		synchronized(this.lock) {
			if(this.st == null) return null;
			try {
				this.st.setBoolean(index, value);
				if(this.paramCount > 0)
					this.args[index-1] = ARG_PRE+"bool"+ARG_DELIM+(value ? "True" : "False")+ARG_POST;
			} catch (SQLException e) {
				log().trace(e);
				this.clean();
				return null;
			}
		}
		return this;
	}


	// get string
	public String getString(final String label) {
		try {
			return getStr(label);
		} catch (SQLException e) {
			log().trace(e);
		}
		return null;
	}
	public String getStr(final String label) throws SQLException {
		synchronized(this.lock) {
			return this.rs.getString(label);
		}
	}
	// get integer
	public Integer getInteger(final String label) {
		try {
			final String value = getStr(label);
			if(value == null)
				return null;
			return utilsMath.toInt(value);
		} catch (SQLException e) {
			log().trace(e);
		}
		return null;
	}
	public int getInt(final String label) throws SQLException {
		synchronized(this.lock) {
			return this.rs.getInt(label);
		}
	}
	// get long
	public Long getLong(final String label) {
		try {
			final String value = getStr(label);
			if(value == null)
				return null;
			return utilsMath.toLong(value);
		} catch (SQLException e) {
			log().trace(e);
		}
		return null;
	}
	public long getLng(final String label) throws SQLException {
		synchronized(this.lock) {
			return this.rs.getLong(label);
		}
	}
	// get decimal
	public Double getDecimal(final String label) {
		return getDouble(label);
	}
	public double getDec(final String label) throws SQLException {
		return getDbl(label);
	}
	// get double
	public Double getDouble(final String label) {
		try {
			final String value = getStr(label);
			if(value == null)
				return null;
			return utilsMath.toDouble(value);
		} catch (SQLException e) {
			log().trace(e);
		}
		return null;
	}
	public double getDbl(final String label) throws SQLException {
		synchronized(this.lock) {
			return this.rs.getDouble(label);
		}
	}
	// get float
	public Float getFloat(final String label) {
		try {
			final String value = getStr(label);
			if(value == null)
				return null;
			return utilsMath.toFloat(value);
		} catch (SQLException e) {
			log().trace(e);
		}
		return null;
	}
	public float getFlt(final String label) throws SQLException {
		synchronized(this.lock) {
			return this.rs.getFloat(label);
		}
	}
	// get boolean
	public Boolean getBoolean(final String label) {
		try {
			final String value = getStr(label);
			if(value == null)
				return null;
			return utilsMath.toBoolean(value);
		} catch (SQLException e) {
			log().trace(e);
		}
		return null;
	}

	public boolean getBool(final String label) throws SQLException {
		synchronized(this.lock) {
			return this.rs.getBoolean(label);
		}
	}


	// lock table (readable/unreadable)
	public boolean lockTable(final String tableName, final boolean readable) {
		if(utils.isEmpty(tableName)) throw new NullPointerException("tableName cannot be null");
		synchronized(this.lock) {
			final StringBuilder str = (new StringBuilder())
				.append("LOCK TABLES `").append(tableName).append("` ")
				.append(readable ? "READ" : "WRITE")
				.append(" /* lock table */");
			if(!Prep(str.toString()) || !Exec()) {
				log().severe("Failed to lock table "+tableName);
				return false;
			}
		}
		return true;
	}
	// lock table (unreadable)
	public boolean lockTable(final String tableName) {
		return this.lockTable(tableName, false);
	}
	// unlock table
	public void unlockTables() {
		synchronized(this.lock) {
			final String sql = "UNLOCK TABLES /* unlock table */";
			if(!Prep(sql) || !Exec())
				log().severe("Failed to unlock tables");
		}
	}


	// logger
	public static xLog log() {
		return dbManager.log();
	}


}
