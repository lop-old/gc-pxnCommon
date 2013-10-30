package com.poixson.commonjava.pxdb.TableManager;

import com.poixson.commonjava.pxdb.dbQuery;


public class exampleDefines extends dbTableManager {

	private static final String TABLE_PREFIX = "pxn_";


	@Override
	public void InitTables() {
		// Example table
		TableDAO tableExample =
			defineTable("Example")
				.addField("id", "id")
				.addField("str", "name", "16")
				.addField("int", "value", "11")
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
