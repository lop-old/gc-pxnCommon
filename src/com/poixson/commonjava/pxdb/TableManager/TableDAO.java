package com.poixson.commonjava.pxdb.TableManager;

import java.util.ArrayList;
import java.util.List;

import com.poixson.commonjava.Utils.utilsSan;

public class TableDAO {

	public final String tableName;

	public final List<FieldDAO> fields = new ArrayList<FieldDAO>();
	public final List<String>   unique = new ArrayList<String>();


	// table dao
	public TableDAO(String tableName) {
		this.tableName = utilsSan.AlphaNumSafe(tableName);
	}


	// set id field
	public TableDAO idField(String fieldName) {
		idField = fieldName;
		return this;
	}
	public String getIdField() {
		if(idField == null)   return null;
		if(idField.isEmpty()) return "id";
		return idField;
	}


	// add field to table
	public TableDAO addField(String type, String name, String size, String def, boolean nullable) {
		this.fields.add(
			new FieldDAO(type, name, size, def, nullable)
		);
		return this;
	}


	public TableDAO unique(String fieldName) {
		this.unique.add(fieldName);
		return this;
	}


}
