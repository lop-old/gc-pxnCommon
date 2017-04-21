package com.poixson.utils.pxdb.tables;

import java.sql.SQLException;

import com.poixson.utils.pxdb.dbManager;
import com.poixson.utils.pxdb.dbQuery;
import com.poixson.utils.xLogger.xLog;


public abstract class dbTableManager {



	public abstract void InitTables() throws SQLException;
	protected abstract dbQuery getDB();
	protected abstract String getTablePrefix();



	// define new table
	protected TableDAO defineTable(final String tableName) {
		return new TableDAO(tableName);
	}



	// check table exists
	public boolean tableExists(final String tableName) throws SQLException {
		final dbQuery db = getDB();
		boolean result = false;
		try {
			db.Prepare("SHOW TABLES LIKE ?");
			db.setString(1, tableName);
			db.Execute();
			result = db.hasNext();
		} finally {
			db.free();
		}
		return result;
	}
	// create if needed
	public void createIfMissing(final TableDAO table) throws SQLException {
		if (!tableExists(table.tableName)) {
			log().info("Creating db table: "+table.tableName);
			// create table
			final dbQuery db = getDB();
			try {
//TODO:
//				final StringBuilder sql = new StringBuilder();
//				//TODO: sql.append();
//				db.Prepare("CREATE TABLE ? ( "+sql.toString()+" )");
//				db.setString(1, table.tableName);
//				db.Execute();
//				return;
			} finally {
				db.free();
			}
		}
//TODO:
//		// check fields
//		for (FieldDAO field : table.fields){
//
//		}
	}



	// logger
	public static xLog log() {
		return dbManager.log();
	}



}
