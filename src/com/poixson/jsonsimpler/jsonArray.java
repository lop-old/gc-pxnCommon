package com.poixson.jsonsimpler;
// Based on https://github.com/fangyidong/json-simple/

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.poixson.commonjava.Utils.byRef.IntRef;


/**
 * JSON Array. Values are ordered. Supports java.utils.List interface
 */
public class jsonArray extends ArrayList<Object> implements JSON {
	private static final long serialVersionUID = 1L;

	protected boolean first = true;
	protected IntRef indent = null;



	public jsonArray() {
		super();
	}
	public jsonArray(final List<Object> list) {
		super(list);
	}
	@Override
	public void load(final String data) {
		
		
		
		
	}



//final OutputStream out = null;
//final Writer writer = new PrintWriter(out);
	@Override
	public String toString() {
		return this.toString(false);
	}
	public String toString(final boolean pretty) {
		this.indent = (pretty ? new IntRef(0) : null);
		final StringWriter out = new StringWriter();
		try {
			this.writeString(out, false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return out.toString();
	}



	void writeString(final Writer out, final boolean writeIndent) throws IOException {
		if(out == null) throw new NullPointerException("out argument is required!");
		// empty list
		if(this.isEmpty()) {
			if(this.indent != null)
				JSON.WriteIndent(out, this.indent);
			out.write(JSON.NULL_VALUE);
			if(this.indent != null)
				out.write(JSON.NEWLINE);
			return;
		}
		this.first = true;
		if(this.indent != null) {
			if(writeIndent)
				JSON.WriteIndent(out, this.indent);
			this.indent.value++;
		}
		out.write('[');
		final Iterator<Object> it = this.iterator();
		while(it.hasNext()) {
			final Object value = it.next();
			if(this.first)
				this.first = false;
			else
				out.write(',');
			if(this.indent != null)
				out.write(JSON.NEWLINE);
			JSON.WriteString(
					out,
					value,
					this.indent,
					true
			);
		}
		if(this.indent != null) {
			out.write(JSON.NEWLINE);
			this.indent.value--;
			JSON.WriteIndent(out, this.indent);
		}
		out.write(']');
	}



}
