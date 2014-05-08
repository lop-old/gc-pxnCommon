package com.poixson.commonjava.Utils;

import java.util.ArrayList;
import java.util.List;


// parse string parts by delimiter
public class StringParser {

	private final String original;
	private volatile String buffer = null;
	private final String delim;
	private volatile String first = null;
	private volatile String part = null;


	public StringParser(final String data) {
		this(" ", data);
	}
	public StringParser(final String delim, final String data) {
		if(utils.isEmpty(delim)) throw new NullPointerException("delim is required");
		if(utils.notEmpty(data))
			this.buffer = data;
		this.delim = delim;
		this.original = data;
	}
	public String[] SplitByLine(String data) {
		if(utils.isEmpty(data)) return null;
		return data.replaceAll("\r", "").split("\n");
	}
	@Override
	public StringParser clone() {
		return new StringParser(this.delim, this.original);
	}


	// get next part
	public boolean next() {
		if(this.buffer == null) return false;
		this.buffer = utilsString.trim(this.delim, this.buffer);
		synchronized(this.buffer) {
			if(!hasNext()) return false;
			int pos = this.buffer.indexOf(this.delim);
			// last part
			if(pos == -1) {
				this.part = this.buffer;
				this.buffer = "";
			// get part from buffer
			} else {
				this.part = this.buffer.substring(0, pos);
				this.buffer = this.buffer.substring(pos + this.delim.length());
			}
		}
		// set first part
		if(this.first == null)
			this.first = this.part;
		return true;
	}
	public String getNext() {
		if(!next()) return null;
		return get();
	}
	public boolean hasNext() {
		return utils.notEmpty(this.buffer);
	}
	// get current part
	public String get() {
		return this.part;
	}
	// get first part
	public String getFirst() {
		return this.first;
	}
	// get rest of buffer
	public String getBuffer() {
		return this.buffer;
	}


	// parse into list
	public List<String> getList() {
		if(!hasNext()) return null;
		List<String> parts = new ArrayList<String>();
		while(next())
			parts.add(get());
		return parts;
	}


	// reset parser
	public void reset() {
		synchronized(this.buffer) {
			this.buffer = this.original;
			this.first = null;
			this.part = null;
		}
	}


	// first part equals
	public boolean firstEquals(String str) {
		return utilsString.strEquals(str, this.first);
	}
	public boolean firstEqualsIgnoreCase(String str) {
		return utilsString.strEqualsIgnoreCase(str, this.first);
	}
	public boolean isFirst(String str) {
		return firstEqualsIgnoreCase(str);
	}
	// current part equals
	public boolean partEquals(String str) {
		return utilsString.strEquals(str, this.part);
	}
	public boolean partEqualsIgnoreCase(String str) {
		return utilsString.strEqualsIgnoreCase(str, this.part);
	}
	public boolean isPart(String str) {
		return partEqualsIgnoreCase(str);
	}


}
