package com.poixson.logger.records;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.xLevel;


public class xLogRecord_Special implements xLogRecord {

	public enum SpecialType {
		CLEAR_SCREEN,
		CLEAR_LINE,
		BEEP
	};
	protected SpecialType type;



	public xLogRecord_Special(final SpecialType type) {
		if (type == null) throw new RequiredArgumentException("type");
		this.type = type;
	}



	@Override
	public String[] getLines() {
		return null;
	}
	@Override
	public xLevel getLevel() {
		return null;
	}



	@Override
	public boolean isEmpty() {
		return true;
	}
	@Override
	public boolean notEmpty() {
		return false;
	}



}
