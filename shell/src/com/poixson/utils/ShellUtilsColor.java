package com.poixson.utils;

import org.fusesource.jansi.Ansi;


public final class ShellUtilsColor extends ShellUtils {



	protected ShellUtilsColor() {
		super();
	}



	@Override
	protected String renderAnsi(final String line) {
		return
			Ansi.ansi()
				.render(line)
				.toString();
	}
	@Override
	protected String[] renderAnsi(final String[] lines) {
		String[] result = new String[ lines.length ];
		for (int index=0; index<lines.length; index++) {
			result[index] = (
				Utils.isEmpty(lines[index])
				? ""
				: Ansi.ansi()
					.render(
						lines[index]
					).toString()
			);
		}
		return result;
	}



}
