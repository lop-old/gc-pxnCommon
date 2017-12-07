package com.poixson.app.gui;

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.LayoutManager;
import java.io.Closeable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

import com.poixson.app.gui.annotations.xWindowProperties;
import com.poixson.app.gui.remapped.RemappedWindowAdapter;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;
import com.poixson.utils.xLogger.AttachedLogger;
import com.poixson.utils.xLogger.xLog;


public abstract class xWindow extends JFrame implements Closeable, AttachedLogger {
	private static final long serialVersionUID = 1L;
	private static final String LOG_NAME = "GUI";

	private static final ConcurrentMap<String, xWindow> allWindows =
			new ConcurrentHashMap<String, xWindow>();

	private final String windowKey;

	private final AtomicBoolean closing     = new AtomicBoolean(false);



	// new window instance
	public xWindow() throws HeadlessException {
		// find a unique name
		{
			final String className =
				StringUtils.PeekLastPart(
					this.getClass().getName(),
					'.'
				);
			if (Utils.isEmpty(className)) throw new RuntimeException("Failed to detect window class name");
			final Set<String> winKeys = allWindows.keySet();
			String last = null;
			while (true) {
				final String name =
					StringUtils.ForceUnique(
						className,
						winKeys
					);
				if (StringUtils.StrEquals(name, last))
					throw new RuntimeException("Detected duplicate window key when attempting to generate a unique key!");
				if (allWindows.putIfAbsent(name, this) == null) {
					this.windowKey = name;
					this.log().fine("New window created: {}", name);
					break;
				}
				last = name;
			}
		}
		// annotations
		final xWindowProperties props =
			this.getClass().getAnnotation(xWindowProperties.class);
		if (props != null) {
			// window title
			final String title = props.title();
			if (Utils.notEmpty(title)) {
				this.setTitle(title);
			}
			// resizable
			this.setResizable(props.resizable());
		}
		// register close hook
		EventQueue.invokeLater(
			new Runnable() {
				private volatile xWindow window = null;
				public Runnable init(final xWindow window) {
					this.window = window;
					return this;
				}
				@Override
				public void run() {
					this.window.addWindowListener(
						RemappedWindowAdapter.getNew(
							this.window,
							"close"
						)
					);
				}
			}.init(this)
		);
	}
	public xWindow(final String title) throws HeadlessException {
		this();
		this.setTitle(title);
	}
	public xWindow(final String title, final LayoutManager layout) throws HeadlessException {
		this(title);
		this.setLayout(layout);
	}



	@Override
	public void close() {
		if (guiUtils.forceDispatchThread(this, "close")) return;
		// only close once
		if (!this.closing.compareAndSet(false, true))    return;
		// close window
		this.log().fine("Closing window: {}", this.getWindowKey());
		this.dispose();
	}



	public String getWindowKey() {
		return this.windowKey;
	}



	public void autoHeight(final int width) {
		if (guiUtils.forceDispatchThread(this, "autoHeight", Integer.valueOf(width)))
			return;
		this.pack();
		this.setSize(width, this.getHeight());
	}



	public void showFocused() {
		if (guiUtils.forceDispatchThread(this, "showFocused"))
			return;
		this.setVisible(true);
		if (!this.isFocused()) {
			this.requestFocus();
		}
	}



	// logger
	@Override
	public xLog log() {
		return xLog.getRoot()
				.get(LOG_NAME);
	}



}
