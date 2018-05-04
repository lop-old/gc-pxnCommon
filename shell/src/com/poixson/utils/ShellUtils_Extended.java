package com.poixson.utils;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;


public final class ShellUtils_Extended extends ShellUtils {



	protected ShellUtils_Extended() {
		super();
		AnsiConsole.systemInstall();
	}



	// ------------------------------------------------------------------------------- //
	// colors



	@Override
	protected String renderAnsi(final String line) {
		if (Utils.isEmpty(line))
			return line;
		return
			Ansi.ansi()
				.render(line)
				.toString();
	}
	@Override
	protected String[] renderAnsi(final String[] lines) {
		if (Utils.isEmpty(lines))
			return lines;
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
