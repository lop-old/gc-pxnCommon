package com.poixson.utils.byref;

import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class StringRef {

	public volatile String value = null;



	public StringRef(final String value) {
		this.value = value;
	}
	public StringRef() {}



	public void value(final String val) {
		this.value = val;
	}
	public String value() {
		return this.value;
	}



	public boolean isEmpty() {
		return Utils.isEmpty(this.value);
	}
	public boolean notEmpty() {
		return Utils.notEmpty(this.value);
	}



	// ------------------------------------------------------------------------------- //
	// index of



	// index of (single delim)
	public int indexOf(final char delim) {
		final String val = this.value;
		return (
			val == null
			? -1
			: val.indexOf(delim)
		);
	}
	public int indexOf(final String delim) {
		final String val = this.value;
		return (
			val == null
			? -1
			: val.indexOf(delim)
		);
	}

	// index of (many delims)
	public int indexOf(final char...delims) {
		final String val = this.value;
		return (
			val == null
			? -1
			: StringUtils.IndexOf(val, delims)
		);
	}
	public int indexOf(final String...delims) {
		final String val = this.value;
		return (
			val == null
			? -1
			: StringUtils.IndexOf(val, delims)
		);
	}



	// ------------------------------------------------------------------------------- //
	// last index of



	// last index of (single delim)
	public int indexOfLast(final char delim) {
		final String val = this.value;
		return (
			val == null
			? -1
			: val.lastIndexOf(delim)
		);
	}
	public int indexOfLast(final String delim) {
		final String val = this.value;
		return (
			val == null
			? -1
			: val.lastIndexOf(delim)
		);
	}

	// last index of (many delims)
	public int indexOfLast(final char...delims) {
		final String val = this.value;
		return (
			val == null
			? -1
			: StringUtils.IndexOfLast(val, delims)
		);
	}
	public int indexOfLast(final String...delims) {
		final String val = this.value;
		return (
			val == null
			? -1
			: StringUtils.IndexOfLast(val, delims)
		);
	}



	// ------------------------------------------------------------------------------- //
	// get/cut first part



	// get first part (single delim)
	public String PeekFirstPart(final char delim) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		final int pos = val.indexOf(delim);
		return (
			pos == -1
			? val
			: val.substring(0, pos)
		);
	}
	public String PeekFirstPart(final String delim) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		final int pos = val.indexOf(delim);
		return (
			pos == -1
			? val
			: val.substring(0, pos)
		);
	}

	// cut first part (single delim)
	public String CutFirstPart(final char delim) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		final int pos = val.indexOf(delim);
		if (pos == -1) {
			this.value = "";
			return val;
		}
		final String result = val.substring(0, pos);
		this.value = val.substring(pos + 1);
		return result;
	}
	public String CutFirstPart(final String delim) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		final int pos = val.indexOf(delim);
		if (pos == -1) {
			this.value = "";
			return val;
		}
		final String result = val.substring(0, pos);
		this.value = val.substring(pos + delim.length());
		return result;
	}



	// get first part (many delims)
	public String PeekFirstPart(final char...delims) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		return
			StringUtils.PeekFirstPart(
				val,
				delims
			);
	}
	public String PeekFirstPart(final String...delims) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		return
			StringUtils.PeekFirstPart(
				val,
				delims
			);
	}

	// cut first part (many delims)
	public String CutFirstPart(final char...delims) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		int pos = Integer.MAX_VALUE;
		// find earliest delim
		for (final char delim : delims) {
			if (Utils.isEmpty(delim)) continue;
			final int p = val.indexOf(delim);
			// delim not found
			if (p == -1) continue;
			// earlier delim
			if (p < pos) {
				pos = p;
				if (p == 0) break;
			}
		}
		// delims not found
		if (pos == Integer.MAX_VALUE) {
			this.value = "";
			return val;
		}
		// cut part
		final String result = val.substring(0, pos);
		this.value = val.substring(pos + 1);
		return result;
	}
	public String CutFirstPart(final String...delims) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		int pos = Integer.MAX_VALUE;
		int delimSize = 0;
		// find earliest/longest delim
		for (final String delim : delims) {
			if (Utils.isEmpty(delim)) continue;
			final int p = val.indexOf(delim);
			// delim not found
			if (p == -1) continue;
			// longer delim
			if (p == pos) {
				if (delim.length() > delimSize) {
					delimSize = delim.length();
				}
				continue;
			}
			// earlier delim
			if (p < pos) {
				pos = p;
				delimSize = delim.length();
			}
		}
		// delims not found
		if (pos == Integer.MAX_VALUE) {
			this.value = "";
			return val;
		}
		// cut part
		final String result = val.substring(0, pos);
		this.value = val.substring(pos + delimSize);
		return result;
	}



	// ------------------------------------------------------------------------------- //
	// get/cut last part



	// get last part (single delim)
	public String PeekLastPart(final char delim) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		final int pos = val.lastIndexOf(delim);
		return (
			pos == -1
			? val
			: val.substring(pos + 1)
		);
	}
	public String PeekLastPart(final String delim) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		final int pos = val.lastIndexOf(delim);
		return (
			pos == -1
			? val
			: val.substring(pos + delim.length())
		);
	}

	// cut last part (single delim)
	public String CutLastPart(final char delim) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		final int pos = val.lastIndexOf(delim);
		if (pos == -1) {
			this.value = "";
			return val;
		}
		final String result = val.substring(pos + 1);
		this.value = val.substring(0, pos);
		return result;
	}
	public String CutLastPart(final String delim) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		final int pos = val.lastIndexOf(delim);
		if (pos == -1) {
			this.value = "";
			return val;
		}
		final String result = val.substring(pos + delim.length());
		this.value = val.substring(0, pos);
		return result;
	}



	// get last part (many delims)
	public String PeekLastPart(final char...delims) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		return
			StringUtils.PeekLastPart(
				val,
				delims
			);
	}
	public String PeekLastPart(final String...delims) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		return
			StringUtils.PeekLastPart(
				val,
				delims
			);
	}

	// cut last part (many delims)
	public String CutLastPart(final char...delims) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		int pos = Integer.MIN_VALUE;
		// find latest delim
		for (final char delim : delims) {
			if (Utils.isEmpty(delim)) continue;
			final int p = val.lastIndexOf(delim);
			// delim not found
			if (p == -1) continue;
			// later delim
			if (p > pos) {
				pos = p;
				if (p == 0) break;
			}
		}
		// delims not found
		if (pos == Integer.MIN_VALUE) {
			this.value = "";
			return val;
		}
		// cut part
		final String result = val.substring(pos + 1);
		this.value = val.substring(0, pos);
		return result;
	}
	public String CutLastPart(final String...delims) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		int pos = Integer.MIN_VALUE;
		int delimSize = 0;
		// find latest/longest delim
		for (final String delim : delims) {
			if (Utils.isEmpty(delim)) continue;
			final int p = val.lastIndexOf(delim);
			// delim not found
			if (p == -1) continue;
			// longer delim
			if (p == pos) {
				if (delim.length() > delimSize) {
					delimSize = delim.length();
				}
				continue;
			}
			// later delim
			if (p > pos) {
				pos = p;
				delimSize = delim.length();
			}
		}
		// delims not found
		if (pos == Integer.MIN_VALUE) {
			this.value = "";
			return val;
		}
		// cut part
		final String result = val.substring(pos + delimSize);
		this.value = val.substring(0, pos);
		return result;
	}



}
