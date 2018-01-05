package com.poixson.tools;


public class xString {

	protected String data  = null;
	protected String next  = null;
	protected String delim = null;



	public static xString getNew() {
		return new xString("");
	}
	public static xString getNew(final String data) {
		return new xString(data);
	}
	public static xString getNew(final Object obj) {
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
		this.data = (
			this.data == null
			? append
			: this.data + append
		);
		return this;
	}



	public xString remove(final String...strip) {
		final String data = this.data;
		if (data != null) {
			this.data = StringUtils.RemoveFromStr(
				data,
				strip
			);
		}
		return this;
	}



	public xString upper() {
		final String data = this.data;
		if (data != null) {
			this.data = data.toUpperCase();
		}
		return this;
	}
	public xString lower() {
		final String data = this.data;
		if (data != null) {
			this.data = data.toLowerCase();
		}
		return this;
	}



	public xString trim() {
		final String data = this.data;
		if (data != null) {
			this.data = data.trim();
		}
		return this;
	}
	public xString trims(final String...strip) {
		final String data = this.data;
		if (data != null) {
			this.data = StringUtils.Trim(data, strip);
		}
		return this;
	}



	public boolean startsWith(final String prefix) {
		final String data = this.data;
		if (data == null) {
			return false;
		}
		return data.startsWith(prefix);
	}
	public boolean endsWith(final String suffix) {
		final String data = this.data;
		if (data == null) {
			return false;
		}
		return data.endsWith(suffix);
	}



	public boolean contains(final CharSequence str) {
		final String data = this.data;
		if (data == null)
			return false;
		return data.contains(str);
	}



	public int indexOf(final String...delims) {
		final String data = this.data;
		if (data == null)
			return -1;
		return StringUtils.IndexOf(data, delims);
	}
	public int indexOf(final int fromIndex, final String...delims) {
		final String data = this.data;
		if (data == null)
			return -1;
		return StringUtils.IndexOf(data, fromIndex, delims);
	}
	public int lastIndexOf(final String...delims) {
		final String data = this.data;
		if (data == null)
			return -1;
		return StringUtils.LastIndexOf(data, delims);
	}
	public int lastIndexOf(final int fromIndex, final String...delims) {
		final String data = this.data;
		if (data == null)
			return -1;
		return StringUtils.LastIndexOf(data, fromIndex, delims);
	}



	public xString ensureStarts(final String start) {
		final String data = this.data;
		if (data != null) {
			this.data = StringUtils.ForceStarts(start, data);
		}
		return this;
	}
	public xString ensureEnds(final String end) {
		final String data = this.data;
		if (data != null) {
			this.data = StringUtils.ForceEnds(end, data);
		}
		return this;
	}



	public xString replaceWith(final String replaceWhat, final String[] withWhat) {
		final String data = this.data;
		if (data != null) {
			this.data = StringUtils.ReplaceWith(replaceWhat, withWhat, data);
		}
		return this;
	}



	public xString pad(final int width, final char padding) {
		final String data = this.data;
		if (data != null) {
			this.data = StringUtils.Pad(width, data, padding);
		}
		return this;
	}
	public xString padFront(final int width, final char padding) {
		final String data = this.data;
		if (data != null) {
			this.data = StringUtils.PadFront(width, data, padding);
		}
		return this;
	}
	public xString padCenter(final int width, final char padding) {
		final String data = this.data;
		if (data != null) {
			this.data = StringUtils.PadCenter(width, data, padding);
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
		String data = this.data.trim();
		while (data.startsWith(dlm)) {
			data = data.substring(dlm.length()).trim();
		}
		if (data.isEmpty()) {
			this.data = "";
			return false;
		}
		// find next delim
		final int pos = data.indexOf(dlm);
		if (pos == -1) {
			this.next = data;
			this.data = "";
		} else {
			this.next = data.substring(0, pos);
			this.data = data.substring(pos + dlm.length());
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
