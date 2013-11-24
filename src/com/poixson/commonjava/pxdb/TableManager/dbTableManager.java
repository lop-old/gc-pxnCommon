package com.poixson.commonjava.pxdb.TableManager;

import com.poixson.commonjava.pxdb.dbQuery;


public abstract class dbTableManager {


	public abstract void InitTables();
	protected abstract dbQuery getDB();
	protected abstract String getTablePrefix();


	// define new table
	protected TableDAO defineTable(String tableName) {
		return new TableDAO(tableName);
	}


	// check table exists
	public boolean tableExists(String tableName) {
		dbQuery db = getDB();
		try {
			db.prepare("SHOW TABLES LIKE ?");
			db.setString(1, tableName);
			db.exec();
			return db.next();
		} finally {
			db.release();
		}
	}
	// create if needed
	public void createIfMissing(TableDAO table) {
		if(!tableExists(table.tableName)) {
			System.out.println("Creating db table: "+table.tableName);
			// create table
			dbQuery db = getDB();
			try {
				String sql = "";
				db.prepare("CREATE TABLE ? ( "+sql+" )");
				db.setString(1, table.tableName);
				db.exec();
				return;
			} finally {
				db.release();
			}
		}
//		// check fields
//		for(FieldDAO field : table.fields){
//
//		}
	}


}
