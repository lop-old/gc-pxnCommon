package com.poixson.commonjava.pxdb;

import java.util.ArrayList;
import java.util.List;


public class dbQueryThenDelete extends dbQuery {

	private final List<Integer> deleteList = new ArrayList<Integer>();
	private volatile String idField = null;


	// new query
	public static dbQueryThenDelete get(String dbKey) {
		dbWorker worker = dbManager.getWorker(dbKey);
		if(worker == null)
			return null;
		return new dbQueryThenDelete(worker);
	}
	// new query (must have lock already)
	public dbQueryThenDelete(dbWorker worker) {
		super(worker);
	}


	public void setIdField(String idField) {
		this.idField = idField;
	}


	public boolean doDelete() {
		dbQuery query = dbQuery.get(this.worker.getKey());
//		query.prepare("DELETE FROM `` WHERE ");
		query.release();
		return false;
	}


	// capture result row
	@Override
	public boolean hasNext() {
		String idField = this.idField;
		if(idField == null || idField.isEmpty())
			idField = "id";
		boolean has = super.hasNext();
		// add row to delete list
		if(has)
			deleteList.add(this.getInt(idField));
		return has;
	}


}
