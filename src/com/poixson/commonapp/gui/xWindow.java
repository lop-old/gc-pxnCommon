package com.poixson.commonapp.gui;

import java.awt.HeadlessException;
import java.awt.LayoutManager;
import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;

import com.poixson.commonapp.app.xApp;
import com.poixson.commonapp.gui.annotations.xWindowProperties;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsString;


public abstract class xWindow extends JFrame implements Closeable {
	private static final long serialVersionUID = 1L;

	private static final Map<String, xWindow> windows = new ConcurrentHashMap<String, xWindow>();

	public final String windowName;



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
		xApp.log().finer("New window created: "+this.windowName);
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
		this.pack();
		this.setSize(width, this.getHeight());
	}



	// show window
	public void Show() {
		this.setVisible(true);
		if(!this.isFocused()) {
			this.setVisible(true);
			this.requestFocus();
		}
	}



	@Override
	public void close() {
		// run in event dispatch thread
		if(guiUtils.forceDispatchThread(this, "close")) return;
		// close window
		xApp.log().fine("Closing window: "+this.windowName);
		this.dispose();
	}



}
