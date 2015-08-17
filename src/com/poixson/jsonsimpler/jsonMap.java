/*
package com.poixson.jsonsimpler;
//Based on https://github.com/fangyidong/json-simple/

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.poixson.commonjava.Utils.byRef.IntRef;


/ **
 * JSON Object. Key/value pairs are unordered. Supports java.util.Map interface.
 * /
public class jsonMap extends HashMap<String, Object> implements JSON {
	private static final long serialVersionUID = 1L;

	protected boolean first = true;
	protected IntRef indent = null;



	public jsonMap() {
		super();
	}
	public jsonMap(final Map<String, Object> map) {
		super(map);
	}
	@Override
	public void load(final String data) {
		
		
		
		
	}



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
		// empty map
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
			this.indent.increment();
		}
		out.write('{');
		final Iterator<Entry<String, Object>> it = this.entrySet().iterator();
		while(it.hasNext()) {
			final Entry<String, Object> entry = it.next();
			if(this.first)
				this.first = false;
			else
				out.write(',');
			if(this.indent != null)
				out.write(JSON.NEWLINE);
			JSON.WriteIndent(out, this.indent);
			out.write(entry.getKey());
			out.write(':');
			if(this.indent != null)
				out.write(' ');
			JSON.WriteString(
					out,
					entry.getValue(),
					this.indent,
					false
			);
		}
		if(this.indent != null) {
			out.write(JSON.NEWLINE);
			this.indent.decrement();
			JSON.WriteIndent(out, this.indent);
		}
		out.write('}');
	}



}
*/