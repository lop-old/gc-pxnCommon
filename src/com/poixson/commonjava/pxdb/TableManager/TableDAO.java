package com.poixson.commonjava.pxdb.TableManager;

import java.util.ArrayList;
import java.util.List;


public class TableDAO {

	public final String name;

	public final List<FieldDAO> fields = new ArrayList<FieldDAO>();
	public final List<String>   unique = new ArrayList<String>();


	// table dao
	public TableDAO(String name) {
		this.name = name;
	}


	// add field to table
	public TableDAO addField(String type, String name) {
		return addField(type, name, null);
	}
	public TableDAO addField(String type, String name, String size) {
		this.fields.add(
			new FieldDAO(type, name, size)
		);
		return this;
	}


	public TableDAO unique(String fieldName) {
		this.unique.add(fieldName);
		return this;
	}


}
