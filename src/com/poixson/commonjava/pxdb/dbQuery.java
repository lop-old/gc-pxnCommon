package com.poixson.commonjava.pxdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.poixson.commonjava.Utils.utilsSan;


public abstract class dbQuery {

	protected volatile PreparedStatement st = null;
	protected volatile ResultSet rs = null;
	protected volatile String sql = null;
	protected volatile boolean quiet = false;
	protected volatile int resultInt = -1;
	private final Object qLock = new Object();


	// worker methods
	protected abstract Connection getConnection();
	public abstract void close();
	public abstract boolean hasError();
	public abstract boolean inUse();
	public abstract boolean getLock();


	// get worker lock (shortcut)
	public static dbQuery get(String dbKey) {
		return dbManager.get(dbKey).getWorker();
	}


	// prepare query
	public dbQuery prepare(String sql) {
		if(sql == null || sql.isEmpty()) throw new IllegalArgumentException("sql cannot be empty!");
		synchronized(qLock) {
			clean();
			if(this.hasError())
				return null;
			try {
				st = getConnection().prepareStatement(sql);
			} catch (SQLException e) {
				e.printStackTrace();
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
		synchronized(qLock) {
			if(this.st == null) return false;
			if(this.sql == null) return false;
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
				if(queryType.equals("INSERT") || queryType.equals("UPDATE") || queryType.equals("DELETE"))
					resultInt = st.executeUpdate();
				else
					rs = st.executeQuery();
			} catch (SQLException e) {
				e.printStackTrace();
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


	// clean vars
	public void release() {
		clean();
	}
	public void clean() {
		synchronized(qLock) {
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
//			args = "";
			quiet = false;
			resultInt = -1;
		}
	}


	// san string for sql
	public static String san(String text) {
		return utilsSan.AlphaNum(text);
	}


	// has next row
	public boolean hasNext() {
		synchronized(qLock) {
			if(rs == null) return false;
			try {
				return rs.next();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}


	// result int
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
		synchronized(qLock) {
			if(st == null) return null;
			try {
				st.setString(index, value);
//				args += " String: "+value;
			} catch (SQLException e) {
				e.printStackTrace();
				clean();
				return null;
			}
		}
		return this;
	}
	// set int
	public dbQuery setInt(int index, int value) {
		synchronized(qLock) {
			if(st == null) return null;
			try {
				st.setInt(index, value);
//				args += " Int: "+Integer.toString(value);
			} catch (SQLException e) {
				e.printStackTrace();
				clean();
				return null;
			}
		}
		return this;
	}
	// set double
	public dbQuery setFloat(int index, double value) {
		synchronized(qLock) {
			if(st == null) return null;
			try {
				st.setDouble(index, value);
//				args += " Double: "+Double.toString(value);
			} catch (SQLException e) {
				e.printStackTrace();
				clean();
				return null;
			}
		}
		return this;
	}
	// set long
	public dbQuery setLong(int index, long value) {
		synchronized(qLock) {
			if(st == null) return null;
			try {
				st.setLong(index, value);
//				args += " Long: "+Long.toString(value);
			} catch (SQLException e) {
				e.printStackTrace();
				clean();
				return null;
			}
		}
		return this;
	}
	// set boolean
	public dbQuery setBool(int index, boolean value) {
		synchronized(qLock) {
			if(st == null) return null;
			try {
				st.setBoolean(index, value);
//				args += " Bool: "+Boolean.toString(value);
			} catch (SQLException e) {
				e.printStackTrace();
				clean();
				return null;
			}
		}
		return this;
	}


	// get query results
	public String getString(String label) {
		synchronized(qLock) {
			try {
				if(rs != null)
					return rs.getString(label);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	// get int
	public Integer getInt(String label) {
		synchronized(qLock) {
			try {
				if(rs != null)
					return rs.getInt(label);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	// get double
	public Double getFloat(String label) {
		synchronized(qLock) {
			try {
				if(rs != null)
					return rs.getDouble(label);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	// get long
	public Long getLong(String label) {
		synchronized(qLock) {
			try {
				if(rs != null)
					return rs.getLong(label);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	// get boolean
	public Boolean getBool(String label) {
		synchronized(qLock) {
			try {
				if(rs != null)
					return rs.getBoolean(label);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}


}
