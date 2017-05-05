package com.poixson.utils.pxdb.tables;

import java.util.ArrayList;
import java.util.List;

import com.poixson.utils.SanUtils;
import com.poixson.utils.pxdb.dbQuery;


public class TableDAO {

	public final String tableName;

	public volatile String idField = "";

	public final List<FieldDAO> fields = new ArrayList<FieldDAO>();
	public final List<String>   unique = new ArrayList<String>();



	// table dao
	public TableDAO(final String tableName) {
		this.tableName = SanUtils.AlphaNumUnderscore(tableName);
	}



	// set id field
	public TableDAO idField(final String fieldName) {
		this.idField = fieldName;
		return this;
	}
	public String getIdField() {
		if (this.idField == null)   return null;
		if (this.idField.isEmpty()) return "id";
		return this.idField;
	}



	// add field to table
	public TableDAO addField(final String type, final String name,
			final String size, final String def, final boolean nullable) {
		this.fields.add(
			new FieldDAO(
				type,
				name,
				size,
				def,
				nullable
			)
		);
		return this;
	}



	public TableDAO unique(final String fieldName) {
		this.unique.add(fieldName);
		return this;
	}



	// CREATE TABLE name ( fields ) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1
	public String sqlCreateTable() {
		return
			(new StringBuilder())
				.append("CREATE TABLE `")
				.append(dbQuery.san(this.tableName))
				.append("` ( ")
				.append(sqlFields())
				.append(" ) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1")
				.toString();
	}
	// ALTER TABLE name ADD field
	public String sqlAddField(final FieldDAO field) {
		return "ALTER TABLE `"+field.sqlField();
	}
	// sql for fields
	public String sqlFields() {
		final StringBuilder sql = new StringBuilder();
		// id field
		if (getIdField() != null) {
			sql.append(
				FieldDAO.sqlIdField(
					getIdField()
				)
			);
		}
		// all other fields
		for (final FieldDAO field : this.fields) {
			if (sql.length() > 0) {
				sql.append(", ");
			}
			sql.append(field.sqlField());
		}
		// unique fields
		for (final String u : this.unique) {
			if (sql.length() > 0) {
				sql.append(", ");
			}
			sql
				.append("UNIQUE KEY `")
				.append(u)
				.append("` (`")
				.append(u)
				.append("`)");
		}
		return sql.toString();
	}



}
