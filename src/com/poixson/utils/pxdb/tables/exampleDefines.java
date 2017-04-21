package com.poixson.utils.pxdb.tables;

import java.sql.SQLException;

import com.poixson.utils.pxdb.dbQuery;


public class exampleDefines extends dbTableManager {

//TODO: this should be custom
	private static final String TABLE_PREFIX = "pxn_";



	@Override
	public void InitTables() throws SQLException {
		// Example table
		TableDAO tableExample =
			defineTable("Example")
				.idField("id")
				// note, if default=null and nullable=false then
				// no default is set and a value is required
				//			type	name			size	default	nullable
				.addField("str",	"name",			"16",	null,	false)
				.addField("int",	"value",		"11",	"0",	false)
				.unique("name");
		createIfMissing(tableExample);
	}



	@Override
	protected dbQuery getDB() {
		return null;
	}



	@Override
	protected String getTablePrefix() {
		return TABLE_PREFIX;
	}



}
