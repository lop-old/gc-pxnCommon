package com.poixson.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.fusesource.jansi.Ansi;

import com.poixson.app.xApp;
import com.poixson.app.xVars;
import com.poixson.logger.xLog;


public final class xAppUtils {
	private xAppUtils() {}



	public static Map<String, String> getStartupVars(final xApp app) {
		final Map<String, String> result =
				new LinkedHashMap<String, String>();
		result.put( "Pid",         Integer.toString(ProcUtils.getPid()) );
		result.put( "Version",     app.getVersion()                     );
		result.put( "Commit",      app.getCommitHashShort()             );
		result.put( "Running as",  System.getProperty("user.name")      );
		result.put( "Current dir", System.getProperty("user.dir")       );
		result.put( "java home",   System.getProperty("java.home")      );
		result.put( "Terminal",    System.getProperty("jline.terminal") );
		if (xVars.isDebug())
			result.put("Debug", "true");
		return result;
//TODO:
//		if (Utils.notEmpty(args)) {
//			out.println();
//			out.println(utilsString.addStrings(" ", args));
//		}
	}
	public static void DisplayStartupVars(final xApp app, final xLog log) {
		final Map<String, String> varsMap =
			getStartupVars(app);
		final Iterator<Entry<String, String>> it =
			varsMap.entrySet().iterator();
		final int maxLineSize =
			StringUtils.FindLongestLine(
				varsMap.keySet().toArray(new String[0])
			);
		final StringBuilder str = new StringBuilder();
		while (it.hasNext()) {
			final Entry<String, String> entry = it.next();
			final String key = entry.getKey();
			final String val = entry.getValue();
			str.setLength(0);
			str.append(key)
				.append(':')
				.append( StringUtils.Repeat(maxLineSize - key.length(), ' ') )
				.append(val);
			log.publish( str.toString() );
		}
	}



	public static void DisplayTestColors(final xLog log) {
		final Ansi.Color[] colors = Ansi.Color.values();
		final int colorCount = colors.length;
		final StringBuilder str = new StringBuilder();
		COLOR_LOOP:
		for (int index=0; index<colorCount; index++) {
			final String colorName =
				colors[index].name()
					.toLowerCase();
			String invertColor;
			switch (colorName) {
			case "black":
				invertColor = "white";
				break;
			case "red":
			case "green":
			case "yellow":
			case "blue":
			case "magenta":
				invertColor = "black";
				break;
			default:
				continue COLOR_LOOP;
			}
			final Map<String, Object> map = new HashMap<String, Object>();
			map.put("color",  colorName  );
			map.put("invert", invertColor);
			map.put(
				"colorpad",
				StringUtils.PadCenter(10, colorName, ' ')
			);
			str.append(
				StringUtils.ReplaceTagKeys(
					"   @|bg_{color},{invert} {colorpad}|@ @|bg_{invert},{color} {colorpad}|@   ",
					map
				)
			);
			if ( ((index+1) % 3) == 0 ) {
				if (index != 2)
					log.publish();
				log.publish( str.toString() );
				str.setLength(0);
			}
		}
	}



}
