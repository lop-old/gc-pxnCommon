package com.poixson.commonapp.gui;

import java.awt.HeadlessException;
import java.awt.LayoutManager;
import java.io.Closeable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

import com.poixson.commonapp.gui.annotations.xWindowProperties;
import com.poixson.commonapp.gui.remapped.RemappedWindowAdapter;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsString;
import com.poixson.commonjava.xLogger.xLog;


public abstract class xWindow extends JFrame implements Closeable {
	private static final long serialVersionUID = 1L;

	private static final ConcurrentMap<String, xWindow> windows =
			new ConcurrentHashMap<String, xWindow>();

	public final String windowName;
	protected final AtomicBoolean closing     = new AtomicBoolean(false);
	private final   AtomicBoolean closeHooked = new AtomicBoolean(false);



	// new window instance
	public xWindow() throws HeadlessException {
		synchronized(windows) {
			// unique window name
			this.windowName =
				utilsString.ensureUnique(
					utilsString.getLastPart(
						".",
						this.getClass().getName()
					),
					windows.keySet()
				);
			windows.put(this.windowName, this);
		}
		log().finer("New window created: "+this.windowName);
		// annotations
		final xWindowProperties props = this.getClass().getAnnotation(xWindowProperties.class);
		if(props != null) {
			// window title
			final String title = props.title();
			if(utils.notEmpty(title))
				this.setTitle(title);
			// resizable
			this.setResizable(props.resizable());
		}
	}
	public xWindow(final String title) throws HeadlessException {
		this();
		this.setTitle(title);
	}
	public xWindow(final String title, final LayoutManager layout) throws HeadlessException {
		this(title);
		this.setLayout(layout);
	}



	public void autoHeight(final int width) {
		if(guiUtils.forceDispatchThread(this, "autoHeight")) return;
		this.pack();
		this.setSize(width, this.getHeight());
	}



	// show window
	public void Show() {
		if(guiUtils.forceDispatchThread(this, "Show")) return;
		this.setVisible(true);
		if(!this.isFocused()) {
			this.setVisible(true);
			this.requestFocus();
		}
	}



	protected void registerCloseHook() {
		if(!this.closeHooked.compareAndSet(false, true))
			return;
		if(guiUtils.forceDispatchThread(this, "registerCloseHook")) return;
		// window close event listener
		this.addWindowListener(
				RemappedWindowAdapter.get(
						this,
						"close"
				)
		);
	}
	@Override
	public void close() {
		if(guiUtils.forceDispatchThread(this, "close")) return;
		if(!this.closing.compareAndSet(false, true)) return;
		// close window
		log().fine("Closing window: "+this.windowName);
		this.dispose();
	}



	// logger
	public static xLog log() {
		return xLog.getRoot("GUI");
	}



}
