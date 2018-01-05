package com.poixson.tools.darrylbu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JComponent;


/**
 * The TextIcon will paint a String of text as an Icon. The Icon
 * can be used by any Swing component that supports icons.
 *
 * TextIcon supports two different layout styles:
 * <ul>
 * <li>Horizontally - does normal rendering of the text by using the
 *   Graphics.drawString(...) method
 * <li>Vertically - Each character is displayed on a separate line
 * </ul>
 *
 * TextIcon was designed to be rendered on a specific JComponent as it
 * requires FontMetrics information in order to calculate its size and to do
 * the rendering. Therefore, it should only be added to component it was
 * created for.
 *
 * By default the text will be rendered using the Font and foreground color
 * of its associated component. However, this class does allow you to override
 * these properties. Also starting in JDK6 the desktop renderering hints will
 * be used to renderer the text. For versions not supporting the rendering
 * hints antialiasing will be turned on.
 *
 * https://tips4java.wordpress.com/2009/04/02/text-icon/
 */
public class TextIcon implements Icon, PropertyChangeListener {

	public enum Layout {
		HORIZONTAL,
		VERTICAL
	}

	private JComponent component;

	private Layout layout;

	private String text;

	private Font font;

	private Color foreground;

	private int padding;

	// Used for the implementation of Icon interface
	private int iconWidth;
	private int iconHeight;

	// Used for Layout.VERTICAL to save reparsing the text every time the icon is repainted
	private String[] strings;
	private int[] stringWidths;



	/**
	 * Convenience constructor to create a TextIcon with a HORIZONTAL layout.
	 *
	 * @param component  the component to which the icon will be added
	 * @param text       the text to be rendered on the Icon
	 */
	public TextIcon(final JComponent component, final String text) {
		this(component, text, Layout.HORIZONTAL);
	}
	/**
	 * Create a TextIcon specifying all the properties.
	 *
	 * @param component  the component to which the icon will be added
	 * @param text       the text to be rendered on the Icon
	 * @param layout     specify the layout of the text. Must be one of
	 *                   the Layout enums: HORIZONTAL or VERTICAL
	 */
	public TextIcon(final JComponent component,
			final String text, final Layout layout) {
		this.component = component;
		this.layout = layout;
		this.setText(text);
		component.addPropertyChangeListener("font", this);
	}



	/**
	 * Get the Layout enum
	 *
	 * @return the Layout enum
	 */
	public Layout getLayout() {
		return this.layout;
	}



	/**
	 * Get the text String that will be rendered on the Icon
	 *
	 * @return the text of the Icon
	 */
	public String getText() {
		return this.text;
	}
	/**
	 * Set the text to be rendered on the Icon
	 *
	 * @param text  the text to be rendered on the Icon
	 */
	public void setText(final String text) {
		this.text = text;
		calculateIconDimensions();
	}



	/**
	 * Get the Font used to render the text. This will default to the Font
	 * of the component unless the Font has been overridden by using the
	 * setFont() method.
	 *
	 * @return the Font used to render the text
	 */
	public Font getFont() {
		if (this.font == null)
			return this.component.getFont();
		return this.font;
	}
	/**
	 * Set the Font to be used for rendering the text
	 *
	 * @param font  the Font to be used for rendering the text
	 */
	public void setFont(final Font font) {
		this.font = font;
		calculateIconDimensions();
	}



	/**
	 * Get the foreground Color used to render the text. This will default to
	 * the foreground Color of the component unless the foreground Color has
	 * been overridden by using the setForeground() method.
	 *
	 * @return the Color used to render the text
	 */
	public Color getForeground() {
		if (this.foreground == null)
			return this.component.getForeground();
		return this.foreground;
	}
	/**
	 * Set the foreground Color to be used for rendering the text
	 *
	 * @param foreground  the foreground Color to be used for rendering the text
	 */
	public void setForeground(final Color foreground) {
		this.foreground = foreground;
		this.component.repaint();
	}



	/**
	 * Get the padding used when rendering the text
	 *
	 * @return the padding specified in pixels
	 */
	public int getPadding() {
		return this.padding;
	}
	/**
	 * By default the size of the Icon is based on the size of the rendered
	 * text. You can specify some padding to be added to the start and end
	 * of the text when it is rendered.
	 *
	 * @param padding  the padding amount in pixels
	 */
	public void setPadding(final int padding) {
		this.padding = padding;
		this.calculateIconDimensions();
	}



	/**
	 * Calculate the size of the Icon using the FontMetrics of the Font.
	 */
	private void calculateIconDimensions() {
		final Font font = this.getFont();
		final FontMetrics fm = this.component.getFontMetrics( font );
		if (this.layout == Layout.HORIZONTAL) {
			this.iconWidth  = fm.stringWidth(this.text) + (this.padding * 2);
			this.iconHeight = fm.getHeight();
		} else
		if (this.layout == Layout.VERTICAL) {
			int maxWidth = 0;
			this.strings = new String[this.text.length()];
			this.stringWidths = new int[this.text.length()];
			// Find the widest character in the text string
			for (int i=0; i < this.text.length(); i++) {
				this.strings[i] = this.text.substring(i, i + 1);
				this.stringWidths[i] = fm.stringWidth( this.strings[i] );
				maxWidth = Math.max(maxWidth, this.stringWidths[i]);
			}
			// Add a minimum of 2 extra pixels, plus the leading value,
			// on each side of the character.
			this.iconWidth = maxWidth + ((fm.getLeading() + 2) * 2);
			// Decrease then normal gap betweens lines of text by taking into
			// account the descent.
			this.iconHeight = (fm.getHeight() - fm.getDescent()) * this.text.length();
			this.iconHeight += this.padding * 2;
		}
		this.component.revalidate();
	}



	//
	// Implement the Icon Interface
	//



	/**
	 * Gets the width of this icon.
	 *
	 * @return the width of the icon in pixels.
	 */
	@Override
	public int getIconWidth() {
		return this.iconWidth;
	}
	/**
	 * Gets the height of this icon.
	 *
	 * @return the height of the icon in pixels.
	 */
	@Override
	public int getIconHeight() {
		return this.iconHeight;
	}



	/**
	 * Paint the icons of this compound icon at the specified location
	 *
	 * @param c The component to which the icon is added
	 * @param g the graphics context
	 * @param x the X coordinate of the icon's top-left corner
	 * @param y the Y coordinate of the icon's top-left corner
	 */
	@Override
	public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
		final Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON
		);
		g2d.setFont(this.getFont() );
		g2d.setColor(this.getForeground());
		final FontMetrics fm = g2d.getFontMetrics();
		if (this.layout == Layout.HORIZONTAL) {
			g2d.translate(x, y + fm.getAscent());
			g2d.drawString(this.text, this.padding, 0);
		} else
		if (this.layout == Layout.VERTICAL) {
			int offsetY = fm.getAscent() - fm.getDescent() + this.padding;
			final int incrementY = fm.getHeight() - fm.getDescent();
			for (int i=0; i < this.text.length(); i++) {
				final int offsetX = Math.round((getIconWidth() - this.stringWidths[i]) / 2.0f);
				g2d.drawString(this.strings[i], x + offsetX, y + offsetY);
				offsetY += incrementY;
			}
		}
		g2d.dispose();
	}



	//
	// Implement the PropertyChangeListener interface
	//



	public void propertyChange(final PropertyChangeEvent event) {
		if (this.font == null) {
				calculateIconDimensions();
		}
	}



}
