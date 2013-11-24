package com.poixson.commonjava.pxdb.TableManager;

import com.poixson.commonjava.Utils.utilsSan;

public class FieldDAO {

	public final String type;
	public final String fieldName;
	public final String size;
	public final String def;
	public final boolean nullable;


	// field dao
	public FieldDAO(String fieldType, String fieldName, String size, String def, boolean nullable) {
		this.type = fieldType;
		this.fieldName = utilsSan.AlphaNumSafe(fieldName);
		this.size = size;
		this.def  = def;
		this.nullable = nullable;
	}


	}


}
