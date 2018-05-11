package com.poixson.app.commands;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poixson.logger.xLogRoot;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class xCommandHandlerImpl implements xCommandHandler {

	protected final CopyOnWriteArrayList<xCommandDAO> commands =
			new CopyOnWriteArrayList<xCommandDAO>();



	public xCommandHandlerImpl() {
	}



	@Override
	public void register(final Object...objs) {
		if (Utils.isEmpty(objs)) return;
		final List<xCommandDAO> found = new ArrayList<xCommandDAO>();
		OBJECTS_LOOP:
		for (final Object o : objs) {
			if (o == null) continue OBJECTS_LOOP;
			final Method[] methods = o.getClass().getMethods();
			if (methods == null) continue OBJECTS_LOOP;
			METHODS_LOOP:
			for (final Method m : methods) {
				final xCommandSpec anno = m.getAnnotation(xCommandSpec.class);
				if (anno == null) continue METHODS_LOOP;
				// found command annotation
				final xCommandDAO dao =
					new xCommandDAO(
						anno.Name(),
						StringUtils.SplitByDelims(anno.Aliases(), ','),
						o, m
					);
				found.add(dao);
			} // end METHODS_LOOP
		} // end OBJECTS_LOOP
		xLogRoot.get()
			.fine("Found {} commands", found.size());
		this.commands.addAll(found);
	}



	@Override
	public boolean process(final String line) {
		if (Utils.isEmpty(line)) return false;
		final xCommandDAO dao =
			this.findCommand(
				StringUtils.PeekFirstPart(line, ' ')
			);
		if (dao == null)
			return false;
		return dao.invoke(line);
	}



	protected xCommandDAO findCommand(final String cmd) {
		if (Utils.isEmpty(cmd)) return null;
		// find matching command
		{
			final Iterator<xCommandDAO> it = this.commands.iterator();
			while (it.hasNext()) {
				final xCommandDAO dao = it.next();
				if (dao.isCommand(cmd))
					return dao;
			}
		}
		// find matching alias
		{
			final Iterator<xCommandDAO> it = this.commands.iterator();
			while (it.hasNext()) {
				final xCommandDAO dao = it.next();
				if (dao.isAlias(cmd))
					return dao;
			}
		}
		// no match
		return null;
	}



}
