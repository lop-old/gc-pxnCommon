package com.poixson.app.commands;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poixson.exceptions.RequiredArgumentException;
import com.poixson.logger.xLogRoot;
import com.poixson.tools.events.xHandler;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class xCommandHandler extends xHandler<xCommand> {

	protected final CopyOnWriteArrayList<xCommandDAO> commands =
			new CopyOnWriteArrayList<xCommandDAO>();



	public xCommandHandler() {
		super(xCommand.class);
	}



	@Override
	public int register(final Object...objects) {
		final int count =
			super.register(objects);
		if (count > 0) {
			xLogRoot.get()
				.fine("Found {} commands", count);
		}
		return count;
	}



	@Override
	protected boolean registerMethod(
			final Object object, final Method method, final xCommand anno) {
		if (object == null) throw new RequiredArgumentException("object");
		if (method == null) throw new RequiredArgumentException("method");
		if (anno == null)   throw new RequiredArgumentException("anno");
		if ( ! (anno instanceof xCommand) )
			throw new IllegalArgumentException( "Invalid annotation type: " + anno.getClass().getName() );
		final xCommand cmd = (xCommand) anno;
		final xCommandDAO dao =
			new xCommandDAO(
				cmd.Name(),
				StringUtils.SplitByDelims(anno.Aliases(), ','),
				object, method
			);
		this.commands.add(dao);
		return true;
	}



	@Override
	public void unregisterObject(final Object object) {
		if (object == null) return;
		final Set<xCommandDAO> remove = new HashSet<xCommandDAO>();
		final Iterator<xCommandDAO> it = this.commands.iterator();
		while (it.hasNext()) {
			final xCommandDAO dao = it.next();
			if (dao.isObject(object)) {
				remove.add(dao);
			}
		}
		if ( ! remove.isEmpty() ) {
			for (final xCommandDAO dao : remove) {
				this.commands.remove(dao);
			}
		}
	}
	@Override
	public void unregisterMethod(final Object object, final String methodName) {
		if (object == null || Utils.isEmpty(methodName)) return;
		final Set<xCommandDAO> remove = new HashSet<xCommandDAO>();
		final Iterator<xCommandDAO> it = this.commands.iterator();
		while (it.hasNext()) {
			final xCommandDAO dao = it.next();
			if (dao.isMethod(object, methodName)) {
				remove.add(dao);
			}
		}
		if ( ! remove.isEmpty() ) {
			for (final xCommandDAO dao : remove) {
				this.commands.remove(dao);
			}
		}
	}
	@Override
	public void unregisterAll() {
		this.commands.clear();
	}



	public boolean process(final String line) {
		if (Utils.isEmpty(line)) return false;
		final xCommandDAO dao =
			this.findCommand(
				StringUtils.PeekFirstPart(line, ' ')
			);
		if (dao == null)
			return false;
		dao.invoke(line);
		return true;
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
