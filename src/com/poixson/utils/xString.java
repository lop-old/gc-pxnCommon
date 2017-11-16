package com.poixson.utils;


public class xString {

	protected String data  = null;
	protected String next  = null;
	protected String delim = null;



	public static xString get() {
		return new xString("");
	}
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
		this.data = StringUtils.toString(obj);
	}



	@Override
	public String toString() {
		return this.data;
	}
	public xString set(final String data) {
		this.data = data;
		return this;
	}



	public boolean isEmpty() {
		return Utils.isEmpty(this.data);
	}
	public boolean notEmpty() {
		return Utils.notEmpty(this.data);
	}



	public xString append(final String append) {
		this.data =
				this.data == null
				? append
				: this.data + append;
		return this;
	}



	public xString remove(final String...strip) {
		if (this.data != null) {
			this.data = StringUtils.RemoveFromStr(
					this.data,
					strip
			);
		}
		return this;
	}



	public xString upper() {
		if (this.data != null) {
			this.data = this.data.toUpperCase();
		}
		return this;
	}
	public xString lower() {
		if (this.data != null) {
			this.data = this.data.toLowerCase();
		}
		return this;
	}



	public xString trim() {
		if (this.data != null) {
			this.data = this.data.trim();
		}
		return this;
	}
	public xString trims(final String...strip) {
		if (this.data != null) {
			this.data = StringUtils.Trim(this.data, strip);
		}
		return this;
	}



	public boolean startsWith(final String prefix) {
		if (this.data == null) {
			return false;
		}
		return this.data.startsWith(prefix);
	}
	public boolean endsWith(final String suffix) {
		if (this.data == null) {
			return false;
		}
		return this.data.endsWith(suffix);
	}



	public xString ensureStarts(final String start) {
		if (this.data != null) {
			this.data = StringUtils.ForceStarts(start, this.data);
		}
		return this;
	}
	public xString ensureEnds(final String end) {
		if (this.data != null) {
			this.data = StringUtils.ForceEnds(end, this.data);
		}
		return this;
	}



	public xString replaceWith(final String replaceWhat, final String[] withWhat) {
		if (this.data != null) {
			this.data = StringUtils.ReplaceWith(replaceWhat, withWhat, this.data);
		}
		return this;
	}



	public xString pad(final int width, final char padding) {
		if (this.data != null) {
			this.data = StringUtils.Pad(width, this.data, padding);
		}
		return this;
	}
	public xString padFront(final int width, final char padding) {
		if (this.data != null) {
			this.data = StringUtils.PadFront(width, this.data, padding);
		}
		return this;
	}
	public xString padCenter(final int width, final char padding) {
		if (this.data != null) {
			this.data = StringUtils.PadCenter(width, this.data, padding);
		}
		return this;
	}



	// ------------------------------------------------------------------------------- //
	// parser



	public xString delim(final String delimStr) {
		this.delim = (
			Utils.isEmpty(delimStr)
			? null
			: delimStr
		);
		return this;
	}
	public String delim() {
		return this.delim;
	}



	public boolean hasNext() {
		if (this.isEmpty())
			return false;
		final String dlm = this.delim;
		if (Utils.isEmpty(dlm))
			return false;
		// trim
		this.trim();
		while (this.data.startsWith(dlm)) {
			this.data = this.data.substring(dlm.length());
			this.trim();
		}
		if (this.data.isEmpty())
			return false;
		// find next delim
		final int pos = this.data.indexOf(dlm);
		if (pos == -1) {
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
		return (
			this.hasNext()
			? this.part()
			: null
		);
	}



}
