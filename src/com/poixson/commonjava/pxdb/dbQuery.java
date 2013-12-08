package com.poixson.commonjava.pxdb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;

import com.poixson.commonjava.Utils.utilsMath;
import com.poixson.commonjava.Utils.utilsSan;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.xLogger.xLevel;
import com.poixson.commonjava.xLogger.xLog;


public class dbQuery {

	protected final dbWorker worker;
	protected volatile PreparedStatement st = null;
	protected volatile ResultSet rs = null;
	protected volatile String sql = null;
	protected volatile boolean quiet = false;
	protected volatile int resultInt = -1;
	private final Object lock = new Object();

	// args
	protected volatile int paramCount = 0;
	protected volatile String[] args;
	private static final String ARG_PRE   = "[";
	private static final String ARG_DELIM = "|";
	private static final String ARG_POST  = "]";


	// new query
	public static dbQuery get(String dbKey) {
		dbWorker worker = dbManager.getWorkerLock(dbKey);
		if(worker == null)
			return null;
		return new dbQuery(worker);
	}
	// new query (must have lock already)
	public dbQuery(dbWorker worker) {
		if(worker == null) throw new NullPointerException("worker cannot be null");
		this.worker = worker;
	}


	// prepared query
	public dbQuery prepare(String sql) {
		if(sql == null || sql.isEmpty()) throw new IllegalArgumentException("sql cannot be empty!");
		synchronized(lock) {
			if(!worker.inUse()) {
				log().trace(
					new IllegalAccessException("dbWorker not locked!")
				);
				return null;
			}
			clean();
			this.sql = sql;
			try {
				st = worker.getConnection().prepareStatement(sql);
				paramCount = st.getParameterMetaData().getParameterCount();
				if(paramCount > 0)
					args = new String[paramCount];
			} catch (SQLNonTransientConnectionException | NullPointerException e) {
				log().severe("db connection closed!");
				close();
				return null;
			} catch (SQLException e) {
				log().trace(e);
				clean();
				return null;
			}
		}
		return this;
	}


	// execute query
	public boolean exec(String sql) {
		if(sql != null && !sql.isEmpty())
			prepare(sql);
		return exec();
	}
	public boolean exec() {
		synchronized(lock) {
			if(!worker.inUse()) {
				log().trace(
					new IllegalAccessException("dbWorker not locked!")
				);
				return false;
			}
			if(this.st == null) return false;
			if(this.sql == null || sql.isEmpty()) return false;
			String sql = this.sql;
			while(sql.startsWith(" "))
				sql = sql.substring(1);
			if(sql.isEmpty()) return false;
			int pos = sql.indexOf(" ");
			String queryType = (pos == -1) ?
				sql.toUpperCase() :
				sql.substring(0, pos).toUpperCase();
//			if(!quiet)
//				getLog().debug("query", this.sql+(args.isEmpty() ? "" : "  ["+args+" ]") );
			try {
				// log query
				if(log().isLoggable(xLevel.FINEST)) {
					// replace ? with values
					log().finest(
						"("+Integer.toString(worker.getId())+") QUERY: "+
						utilsString.replaceWith("?", args, sql)
					);
				}
				// execute query
				if(queryType.equals("INSERT") || queryType.equals("UPDATE") || queryType.equals("CREATE") || queryType.equals("DELETE"))
					resultInt = st.executeUpdate();
				else
					rs = st.executeQuery();
			} catch (SQLNonTransientConnectionException e) {
				log().severe("db connection closed!");
				close();
				return false;
			} catch (SQLException e) {
				log().trace(e);
				clean();
				return false;
			}
		}
		return true;
	}


	// set quiet mode
	public boolean quiet() {
		return quiet(true);
	}
	public boolean quiet(boolean quiet) {
		this.quiet = quiet;
		return this.quiet;
	}


	// get db key
	public String dbKey() {
		if(worker == null) return null;
		return worker.dbKey();
	}


	// clean vars
	public void clean() {
		synchronized(lock) {
			if(rs != null) {
				try {
					rs.close();
				} catch (SQLException ignore) {}
				rs = null;
			}
			if(st != null) {
				try {
					st.close();
				} catch (SQLException ignore) {}
				st = null;
			}
			sql = null;
			paramCount = 0;
			args = null;
			quiet = false;
			resultInt = -1;
		}
	}
	public void release() {
		clean();
		worker.release();
	}
	public void close() {
		clean();
		worker.close();
	}


	// san string for sql
	public static String san(String text) {
		return utilsSan.AlphaNumSafe(text);
	}


	// has next row
	public boolean next() {
		synchronized(lock) {
			if(rs == null) return false;
			try {
				return rs.next();
			} catch (SQLException e) {
				log().trace(e);
			}
		}
		return false;
	}


	// result count
	public int getAffectedRows() {
		return getResultInt();
	}
	public int getInsertId() {
		return getResultInt();
	}
	public int getResultInt() {
		return this.resultInt;
	}


	// query parameters
	public dbQuery setString(int index, String value) {
		synchronized(lock) {
			if(st == null) return null;
			try {
				st.setString(index, value);
				if(paramCount > 0)
					args[index-1] = ARG_PRE+"str"+ARG_DELIM+value+ARG_POST;
			} catch (SQLException e) {
				log().trace(e);
				clean();
				return null;
			}
		}
		return this;
	}
	// set int
	public dbQuery setInt(int index, int value) {
		synchronized(lock) {
			if(st == null) return null;
			try {
				st.setInt(index, value);
				if(paramCount > 0)
					args[index-1] = ARG_PRE+"int"+ARG_DELIM+Integer.toString(value)+ARG_POST;
			} catch (SQLException e) {
				log().trace(e);
				clean();
				return null;
			}
		}
		return this;
	}
	// set long
	public dbQuery setLong(int index, long value) {
		synchronized(lock) {
			if(st == null) return null;
			try {
				st.setLong(index, value);
				if(paramCount > 0)
					args[index-1] = ARG_PRE+"lng"+ARG_DELIM+Long.toString(value)+ARG_POST;
			} catch (SQLException e) {
				log().trace(e);
				clean();
				return null;
			}
		}
		return this;
	}
	// set decimal
	public dbQuery setDecimal(int index, double value) {
		if(setDouble(index, value) == null)
			return null;
		if(paramCount > 0)
			args[index-1] = ARG_PRE+"dec"+ARG_DELIM+Double.toString(value)+ARG_POST;
		return this;
	}
	// set double
	public dbQuery setDouble(int index, double value) {
		synchronized(lock) {
			if(st == null) return null;
			try {
				st.setDouble(index, value);
				if(paramCount > 0)
					args[index-1] = ARG_PRE+"dbl"+ARG_DELIM+Double.toString(value)+ARG_POST;
			} catch (SQLException e) {
				log().trace(e);
				clean();
				return null;
			}
		}
		return this;
	}
	// set float
	public dbQuery setFloat(int index, float value) {
		synchronized(lock) {
			if(st == null) return null;
			try {
				st.setFloat(index, value);
				if(paramCount > 0)
					args[index-1] = ARG_PRE+"flt"+ARG_DELIM+Float.toString(value)+ARG_POST;
			} catch (SQLException e) {
				log().trace(e);
				clean();
				return null;
			}
		}
		return this;
	}
	// set boolean
	public dbQuery setBool(int index, boolean value) {
		synchronized(lock) {
			if(st == null) return null;
			try {
				st.setBoolean(index, value);
				if(paramCount > 0)
					args[index-1] = ARG_PRE+"bool"+ARG_DELIM+(value ? "True" : "False")+ARG_POST;
			} catch (SQLException e) {
				log().trace(e);
				clean();
				return null;
			}
		}
		return this;
	}


	// get string
	public String getString(String label) {
		try {
			return getStr(label);
		} catch (SQLException e) {
			log().trace(e);
		}
		return null;
	}
	public String getStr(String label) throws SQLException {
		synchronized(lock) {
			return rs.getString(label);
		}
	}
	// get integer
	public Integer getInteger(String label) {
		try {
			String value = getStr(label);
			if(value == null)
				return null;
			return utilsMath.parseInteger(value);
		} catch (SQLException e) {
			log().trace(e);
		}
		return null;
	}
	public int getInt(String label) throws SQLException {
		synchronized(lock) {
			return rs.getInt(label);
		}
	}
	// get long
	public Long getLong(String label) {
		try {
			String value = getStr(label);
			if(value == null)
				return null;
			return utilsMath.parseLong(value);
		} catch (SQLException e) {
			log().trace(e);
		}
		return null;
	}
	public long getLng(String label) throws SQLException {
		synchronized(lock) {
			return rs.getLong(label);
		}
	}
	// get decimal
	public Double getDecimal(String label) {
		return getDouble(label);
	}
	public double getDec(String label) throws SQLException {
		return getDbl(label);
	}
	// get double
	public Double getDouble(String label) {
		try {
			String value = getStr(label);
			if(value == null)
				return null;
			return utilsMath.parseDouble(value);
		} catch (SQLException e) {
			log().trace(e);
		}
		return null;
	}
	public double getDbl(String label) throws SQLException {
		synchronized(lock) {
			return rs.getDouble(label);
		}
	}
	// get float
	public Float getFloat(String label) {
		try {
			String value = getStr(label);
			if(value == null)
				return null;
			return utilsMath.parseFloat(value);
		} catch (SQLException e) {
			log().trace(e);
		}
		return null;
	}
	public float getFlt(String label) throws SQLException {
		synchronized(lock) {
			return rs.getFloat(label);
		}
	}
	// get boolean
	public Boolean getBoolean(String label) {
		try {
			String value = getStr(label);
			if(value == null)
				return null;
			return utilsMath.parseBoolean(value);
		} catch (SQLException e) {
			log().trace(e);
		}
		return null;
	}
	public boolean getBool(String label) throws SQLException {
		synchronized(lock) {
			return rs.getBoolean(label);
		}
	}


	// lock table
	public boolean lockTable(String tableName) {
		return lockTable(tableName, false);
	}
	public boolean lockTable(String tableName, boolean readable) {
		if(tableName == null || tableName.isEmpty()) throw new NullPointerException("tableName cannot be null");
		synchronized(lock) {
			StringBuilder sql = (new StringBuilder())
				.append("LOCK TABLES `").append(tableName).append("` ")
				.append(readable ? "READ" : "WRITE")
				.append(" /* lock table */");
			prepare(sql.toString());
			return exec();
		}
	}
	// unlock table
	public void unlockTables() {
		synchronized(lock) {
			prepare("UNLOCK TABLES /* unlock table */");
			exec();
		}
	}


	// logger
	public static xLog log() {
		return dbManager.log();
	}


}
