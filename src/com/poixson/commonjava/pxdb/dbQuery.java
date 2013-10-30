package com.poixson.commonjava.pxdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public abstract class dbQuery {

	protected volatile PreparedStatement st = null;
	protected volatile ResultSet rs = null;
	protected volatile String sql = null;
	protected volatile boolean quiet = false;
	protected volatile int resultInt = -1;

	protected final Object lock = new Object();


	// worker methods
	protected abstract Connection getConnection();
	public abstract void close();
	public abstract boolean hasError();
	public abstract boolean inUse();
	public abstract boolean getLock();


	// prepare query
	public dbQuery prepare(String sql) {
		if(sql == null || sql.isEmpty()) throw new IllegalArgumentException("sql cannot be empty!");
		synchronized(lock) {
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
		synchronized(lock) {
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
		synchronized(lock) {
			st = null;
			rs = null;
			sql = null;
//			args = "";
			quiet = false;
			resultInt = -1;
		}
	}


	// has next row
	public boolean hasNext() {
		synchronized(lock) {
			if(rs == null)
				return false;
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


}
