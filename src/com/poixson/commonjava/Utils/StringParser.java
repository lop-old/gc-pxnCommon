package com.poixson.commonjava.Utils;

import java.util.ArrayList;
import java.util.List;


// parse string parts by delimiter
public class StringParser {

	private final String original;
	private volatile String buffer;
	private volatile String delim;
	private volatile String first = null;
	private volatile String part = null;


	public StringParser(String data) {
		this(" ", data);
	}
	public StringParser(String delim, String data) {
		if(delim == null || delim.isEmpty()) throw new NullPointerException("delim is required");
		if(data  == null || data.isEmpty()) data = "";
		this.delim = delim;
		this.buffer = data;
		this.original = data;
	}
	public String[] SplitByLine(String data) {
		if(data == null || data.isEmpty()) return null;
		return data.replaceAll("\r", "").split("\n");
	}
	@Override
	public StringParser clone() {
		return new StringParser(delim, original);
	}


	// get next part
	public boolean next() {
		if(buffer == null) return false;
		buffer = utilsString.trim(delim, buffer);
		synchronized(buffer) {
			if(!hasNext()) return false;
			int pos = buffer.indexOf(delim);
			// last part
			if(pos == -1) {
				part = buffer;
				buffer = "";
			// get part from buffer
			} else {
				part = buffer.substring(0, pos);
				buffer = buffer.substring(pos + delim.length());
			}
		}
		// set first part
		if(first == null)
			first = part;
		return true;
	}
	public String getNext() {
		if(!next()) return null;
		return get();
	}
	public boolean hasNext() {
		return (buffer != null && !buffer.isEmpty());
	}
	// get current part
	public String get() {
		return part;
	}
	// get first part
	public String getFirst() {
		return first;
	}
	// get rest of buffer
	public String getBuffer() {
		return buffer;
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
		synchronized(buffer) {
			buffer = original;
			first = null;
			part = null;
		}
	}


	// first part equals
	public boolean firstEquals(String str) {
		return utilsString.strEquals(str, this.first);
	}
	public boolean firstEqualsIgnoreCase(String str) {
		return utilsString.strEqualsIgnoreCase(str, this.first);
	}
	// current part equals
	public boolean partEquals(String str) {
		return utilsString.strEquals(str, this.part);
	}
	public boolean partEqualsIgnoreCase(String str) {
		return utilsString.strEqualsIgnoreCase(str, this.part);
	}


}
