package com.poixson.commonjava.Utils;


public class xString {

	protected volatile String data = null;
	protected volatile String next = null;
	protected volatile String delim = null;



	public static xString get(final String data) {
		return new xString(data);
	}
	public static xString get(final Object obj) {
		return new xString(obj);
	}
	public xString(final String data) {
		this.data = data;
	}
	public xString(final Object obj) {
		this.data = utilsString.toString(obj);
	}



	public String toString() {
		return this.data;
	}
	public xString set(final String data) {
		this.data = data;
		return this;
	}



	public boolean isEmpty() {
		return utils.isEmpty(this.data);
	}
	public boolean notEmpty() {
		return utils.notEmpty(this.data);
	}



	public xString append(final String append) {
		if(this.data == null)
			this.data = append;
		else
			this.data = this.data + append;
		return this;
	}



	public xString remove(final String...strip) {
		if(this.data != null)
			this.data = utilsString.remove(this.data, strip);
		return this;
	}



	public xString upper() {
		if(this.data != null)
			this.data = this.data.toUpperCase();
		return this;
	}
	public xString lower() {
		if(this.data != null)
			this.data = this.data.toLowerCase();
		return this;
	}



	public xString trim() {
		if(this.data != null)
			this.data = this.data.trim();
		return this;
	}
	public xString trims(final String...strip) {
		if(this.data != null)
			this.data = utilsString.trims(this.data, strip);
		return this;
	}



	public boolean startsWith(final String prefix) {
		if(this.data == null)
			return false;
		return this.data.startsWith(prefix);
	}
	public boolean endsWith(final String suffix) {
		if(this.data == null)
			return false;
		return this.data.endsWith(suffix);
	}



	public xString ensureStarts(final String start) {
		if(this.data != null)
			this.data = utilsString.ensureStarts(start, this.data);
		return this;
	}
	public xString ensureEnds(final String end) {
		if(this.data != null)
			this.data = utilsString.ensureEnds(end, this.data);
		return this;
	}



	public xString replaceWith(final String replaceWhat, final String[] withWhat) {
		if(this.data != null)
			this.data = utilsString.replaceWith(replaceWhat, withWhat, this.data);
		return this;
	}



	public xString pad(final int width, final char padding) {
		if(this.data != null)
			this.data = utilsString.pad(width, this.data, padding);
		return this;
	}
	public xString padFront(final int width, final char padding) {
		if(this.data != null)
			this.data = utilsString.padFront(width, this.data, padding);
		return this;
	}
	public xString padCenter(final int width, final char padding) {
		if(this.data != null)
			this.data = utilsString.padCenter(width, this.data, padding);
		return this;
	}



	// *** parser *** //



	public xString delim(final String delim) {
		this.delim = (utils.isEmpty(delim) ? null : delim);
		return this;
	}
	public String delim() {
		return this.delim;
	}



	public boolean hasNext() {
		if(this.isEmpty()) return false;
		final String dlm = this.delim;
		if(utils.isEmpty(dlm)) return false;
		// trim
		this.trim();
		while(this.data.startsWith(dlm)) {
			this.data = this.data.substring(dlm.length());
			this.trim();
		}
		if(this.data.isEmpty()) return false;
		// find next delim
		final int pos = this.data.indexOf(dlm);
		if(pos == -1) {
			this.next = this.data;
			this.data = "";
		} else {
			this.next = this.data.substring(0, pos);
			this.data = this.data.substring(pos + dlm.length());
		}
		return true;
	}
	public String part() {
		return this.next;
	}
	public String getNext() {
		if(!this.hasNext())
			return null;
		return this.part();
	}



}
