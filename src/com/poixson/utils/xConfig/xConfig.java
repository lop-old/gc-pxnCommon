package com.poixson.utils.xConfig;

import java.util.Map;

import com.poixson.utils.Utils;


public class xConfig extends xConfigValues {

	private volatile boolean fromResource = false;
	private volatile String fileName = null;



	public xConfig(final Map<String, Object> datamap) {
		super(datamap);
	}
	@Override
	public xConfig clone() {
		return new xConfig(this.datamap);
	}



	// ------------------------------------------------------------------------------- //



	public boolean isFromResource() {
		return this.fromResource;
	}
	void setFromResource() {
		this.setFromResource(true);
	}
	void setFromResource(final boolean fromResource) {
		this.fromResource = fromResource;
	}



	public String getFileName() {
		return this.fileName;
	}
	public xConfig setFileName(final String fileName) {
		this.fileName =
			Utils.isEmpty(fileName)
			? null
			: fileName;
		return this;
	}



//TODO:
/*
	public static void Dump(final Object obj) {
		Dump(obj, 0, false);
	}
	public void dump() {
		Dump(this.datamap, 0, false);
	}
	private static void Dump(final Object obj, final int indent, boolean noIndent) {
		// null
		if(obj == null) {
			System.out.println(DumpIndent(indent, noIndent)+"[NULL]");
			return;
		}
		// xConfig object
		if(obj instanceof xConfig) {
			((xConfig) obj).dump();
			return;
		}
		// set/list
		if(obj instanceof Collection) {
			@SuppressWarnings("rawtypes")
			final Collection c = (Collection) obj;
			System.out.println();
			System.out.println(DumpIndent(indent, false)+"[SET/LIST]");
			for(final Object o : c) {
				Dump(o, indent+1, false);
			}
			return;
		}
		// map
		if(obj instanceof Map) {
			final Map<String, Object> map = utilsObject.castMap(
					String.class,
					Object.class,
					obj
			);
//			System.out.println();
			System.out.println(DumpIndent(indent, noIndent)+"[MAP]");
			for(final Entry<String, Object> entry : map.entrySet()) {
				System.out.print(DumpIndent(indent+1, false)+entry.getKey()+" = ");
				Dump(entry.getValue(), indent+2, true);
			}
			return;
		}
		// other
		System.out.println(DumpIndent(indent, noIndent)+obj.toString());
	}
//	private void dump(final int indent, final boolean noIndent) {
//		if(indent == 0) {
//			System.out.println();
//			System.out.println("DUMP:");
//		}
//		for(final Entry<String, Object> entry : this.datamap.entrySet()) {
//			final String key = entry.getKey();
//			final Object val = entry.getValue();
//			System.out.print(DumpIndent(indent+1, false)+key+" = ");
//			Dump(val, indent+2, false);
//		}
//		if(indent == 0) {
//			System.out.println();
//		}
//	}
	private static String DumpIndent(final int indent, final boolean noIndent) {
		if(noIndent)
			return "";
		return utilsString.repeat(indent, "  ");
	}
*/



}
