package com.poixson.utils;

import java.util.concurrent.atomic.AtomicReference;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import com.poixson.app.commands.xCommandHandler;


public final class ShellUtils_Extended extends ShellUtils {

	protected final AtomicReference<xCommandHandler> handler =
			new AtomicReference<xCommandHandler>(null);



	protected ShellUtils_Extended() {
		super();
		AnsiConsole.systemInstall();
	}



	// ------------------------------------------------------------------------------- //
	// commands



	@Override
	public xCommandHandler getCommandHandler() {
		return this.handler.get();
	}
	@Override
	public void setCommandHandler(final xCommandHandler handler) {
		if (handler == null) {
			this.handler.set(null);
		} else {
			if ( ! this.handler.compareAndSet(null, handler) )
				throw new IllegalStateException("Command handler already set!");
		}
	}
	@Override
	public void registerCommands(final Object...objs) {
		final xCommandHandler handler = this.handler.get();
		if (handler == null) throw new UnsupportedOperationException("Command handler not set");
		handler.register(objs);
	}
	@Override
	public boolean process(final String line) {
		final xCommandHandler handler = this.handler.get();
		if (handler == null) throw new UnsupportedOperationException("Command handler not set");
		return handler.process(line);
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
