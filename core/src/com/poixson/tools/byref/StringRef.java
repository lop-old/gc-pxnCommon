package com.poixson.tools.byref;

import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class StringRef implements StringRefInterface {

	public volatile String value = null;



	public static StringRef getNew(final String val) {
		return new StringRef(val);
	}
	public StringRef(final String val) {
		this.value = val;
	}
	public StringRef() {
	}



	@Override
	public void value(final String val) {
		this.value = val;
	}
	@Override
	public String value() {
		return this.value;
	}



	@Override
	public boolean isEmpty() {
		return Utils.isEmpty(this.value);
	}
	@Override
	public boolean notEmpty() {
		return Utils.notEmpty(this.value);
	}



	@Override
	public int length() {
		final String val = this.value;
		return (
			val == null
			? 0
			: val.length()
		);
	}



	// ------------------------------------------------------------------------------- //
	// index of



	// index of (single delim)
	@Override
	public int indexOf(final char delim) {
		final String val = this.value;
		return (
			val == null
			? -1
			: val.indexOf(delim)
		);
	}
	@Override
	public int indexOf(final String delim) {
		final String val = this.value;
		return (
			val == null
			? -1
			: val.indexOf(delim)
		);
	}

	// index of (many delims)
	@Override
	public int indexOf(final char...delims) {
		final String val = this.value;
		return (
			val == null
			? -1
			: StringUtils.IndexOf(val, delims)
		);
	}
	@Override
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
	@Override
	public int indexOfLast(final char delim) {
		final String val = this.value;
		return (
			val == null
			? -1
			: val.lastIndexOf(delim)
		);
	}
	@Override
	public int indexOfLast(final String delim) {
		final String val = this.value;
		return (
			val == null
			? -1
			: val.lastIndexOf(delim)
		);
	}

	// last index of (many delims)
	@Override
	public int indexOfLast(final char...delims) {
		final String val = this.value;
		return (
			val == null
			? -1
			: StringUtils.IndexOfLast(val, delims)
		);
	}
	@Override
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
	@Override
	public String peekFirstPart(final char delim) {
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
	@Override
	public String peekFirstPart(final String delim) {
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
	@Override
	public String cutFirstPart(final char delim) {
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
	@Override
	public String cutFirstPart(final String delim) {
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
	@Override
	public String peekFirstPart(final char...delims) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		return
			StringUtils.PeekFirstPart(
				val,
				delims
			);
	}
	@Override
	public String peekFirstPart(final String...delims) {
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
	@Override
	public String cutFirstPart(final char...delims) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		int pos = Integer.MAX_VALUE;
		// find earliest delim
		for (final char delim : delims) {
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
	@Override
	public String cutFirstPart(final String...delims) {
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
	@Override
	public String peekLastPart(final char delim) {
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
	@Override
	public String peekLastPart(final String delim) {
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
	@Override
	public String cutLastPart(final char delim) {
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
	@Override
	public String cutLastPart(final String delim) {
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
	@Override
	public String peekLastPart(final char...delims) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		return
			StringUtils.PeekLastPart(
				val,
				delims
			);
	}
	@Override
	public String peekLastPart(final String...delims) {
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
	@Override
	public String cutLastPart(final char...delims) {
		final String val = this.value;
		if (Utils.isEmpty(val))
			return val;
		int pos = Integer.MIN_VALUE;
		// find latest delim
		for (final char delim : delims) {
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
	@Override
	public String cutLastPart(final String...delims) {
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
