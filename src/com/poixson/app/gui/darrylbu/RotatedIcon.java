package com.poixson.app.gui.darrylbu;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;


/**
 * The RotatedIcon allows you to change the orientation of an Icon by
 * rotating the Icon before it is painted. This class supports the following
 * orientations:
 *
 * <ul>
 *   <li>DOWN - rotated 90 degrees
 *   <li>UP (default) - rotated -90 degrees
 *   <li>UPSIDE_DOWN - rotated 180 degrees
 *   <li>ABOUT_CENTER - the icon is rotated by the specified degrees about its center.
 * </ul>
 *
 * https://tips4java.wordpress.com/2009/04/06/rotated-icon/
 */
public class RotatedIcon implements Icon {

	public enum Rotate {
		DOWN,
		UP,
		UPSIDE_DOWN,
		ABOUT_CENTER
	}

	private final Icon icon;

	private final Rotate rotate;

	private double degrees;
	private boolean circularIcon;



	/**
	 * Convenience constructor to create a RotatedIcon that is rotated DOWN.
	 *
	 * @param icon  the Icon to rotate
	 */
	public RotatedIcon(final Icon icon) {
		this(icon, Rotate.UP);
	}
	/**
	 * Create a RotatedIcon
	 *
	 * @param icon	the Icon to rotate
	 * @param rotate  the direction of rotation
	 */
	public RotatedIcon(final Icon icon, final Rotate rotate) {
		this.icon = icon;
		this.rotate = rotate;
	}
	/**
	 * Create a RotatedIcon. The icon will rotate about its center. This
	 * constructor will automatically set the Rotate enum to ABOUT_CENTER.
	 *
	 * @param icon	the Icon to rotate
	 * @param degrees   the degrees of rotation
	 */
	public RotatedIcon(final Icon icon, final double degrees) {
		this(icon, degrees, false);
	}
	/**
	 * Create a RotatedIcon. The icon will rotate about its center. This
	 * constructor will automatically set the Rotate enum to ABOUT_CENTER.
	 *
	 * @param icon	the Icon to rotate
	 * @param degrees   the degrees of rotation
	 * @param circularIcon treat the icon as circular so its size doesn't change
	 */
	public RotatedIcon(final Icon icon,
			final double degrees, final boolean circularIcon) {
		this(icon, Rotate.ABOUT_CENTER);
		this.setDegrees(degrees);
		this.setCircularIcon(circularIcon);
	}



	/**
	 * Gets the Icon to be rotated
	 *
	 * @return the Icon to be rotated
	 */
	public Icon getIcon() {
		return this.icon;
	}



	/**
	 * Gets the Rotate enum which indicates the direction of rotation
	 *
	 * @return the Rotate enum
	 */
	public Rotate getRotate() {
		return this.rotate;
	}
	/**
	 * Gets the degrees of rotation. Only used for Rotate.ABOUT_CENTER.
	 *
	 * @return the degrees of rotation
	 */
	public double getDegrees() {
		return this.degrees;
	}
	/**
	 * Set the degrees of rotation. Only used for Rotate.ABOUT_CENTER.
	 * This method only sets the degress of rotation, it will not cause
	 * the Icon to be repainted. You must invoke repaint() on any
	 * component using this icon for it to be repainted.
	 *
	 * @param degrees the degrees of rotation
	 */
	public void setDegrees(final double degrees) {
		this.degrees = degrees;
	}



	/**
	 * Is the image circular or rectangular? Only used for Rotate.ABOUT_CENTER.
	 * When true, the icon width/height will not change as the Icon is rotated.
	 *
	 * @return true for a circular Icon, false otherwise
	 */
	public boolean isCircularIcon() {
		return this.circularIcon;
	}
	/**
	 * Set the Icon as circular or rectangular. Only used for Rotate.ABOUT_CENTER.
	 * When true, the icon width/height will not change as the Icon is rotated.
	 *
	 * @param true for a circular Icon, false otherwise
	 */
	public void setCircularIcon(final boolean circularIcon) {
		this.circularIcon = circularIcon;
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
		if (this.rotate == Rotate.ABOUT_CENTER) {
			if (this.circularIcon) {
				return this.icon.getIconWidth();
			}
			final double rads = Math.toRadians(this.degrees);
			final double sin = Math.abs( Math.sin(rads) );
			final double cos = Math.abs( Math.cos(rads) );
			final int width =
				(int) Math.floor(
					(this.icon.getIconWidth()  * cos) +
					(this.icon.getIconHeight() * sin)
				);
			return width;
		}
		if (this.rotate == Rotate.UPSIDE_DOWN) {
			return this.icon.getIconWidth();
		}
		return this.icon.getIconHeight();
	}



	/**
	 * Gets the height of this icon.
	 *
	 * @return the height of the icon in pixels.
	 */
	@Override
	public int getIconHeight() {
		if (this.rotate == Rotate.ABOUT_CENTER) {
			if (this.circularIcon) {
				return this.icon.getIconHeight();
			}
			final double rads = Math.toRadians(this.degrees);
			final double sin = Math.abs( Math.sin(rads) );
			final double cos = Math.abs( Math.cos(rads) );
			final int height =
				(int) Math.floor(
					(this.icon.getIconHeight() * cos) +
					(this.icon.getIconWidth() * sin)
				);
			return height;
		}
		if (this.rotate == Rotate.UPSIDE_DOWN) {
			return this.icon.getIconHeight();
		}
		return this.icon.getIconWidth();
	}



	/**
	 * Paint the icons of this compound icon at the specified location
	 *
	 * @param c The component on which the icon is painted
	 * @param g the graphics context
	 * @param x the X coordinate of the icon's top-left corner
	 * @param y the Y coordinate of the icon's top-left corner
	 */
	@Override
	public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
		final Graphics2D g2d = (Graphics2D) g.create();
		final int cWidth  = this.icon.getIconWidth() / 2;
		final int cHeight = this.icon.getIconHeight() / 2;
		final int xAdjustment = (this.icon.getIconWidth() % 2) == 0 ? 0 : -1;
		final int yAdjustment = (this.icon.getIconHeight() % 2) == 0 ? 0 : -1;
		if (this.rotate == Rotate.DOWN) {
			g2d.translate(
				x + cHeight,
				y + cWidth
			);
			g2d.rotate( Math.toRadians(90.0) );
			this.icon.paintIcon(
				c,
				g2d,
				0 - cWidth,
				yAdjustment - cHeight
			);
		} else
		if (this.rotate == Rotate.UP) {
			g2d.translate(
				x + cHeight,
				y + cWidth
			);
			g2d.rotate( Math.toRadians(270.0) );
			this.icon.paintIcon(
				c,
				g2d,
				xAdjustment - cWidth,
				0 - cHeight
			);
		} else
		if (this.rotate == Rotate.UPSIDE_DOWN) {
			g2d.translate(
				x + cWidth,
				y + cHeight
			);
			g2d.rotate( Math.toRadians(180.0) );
			this.icon.paintIcon(
				c,
				g2d,
				xAdjustment - cWidth,
				yAdjustment - cHeight
			);
		} else
		if (this.rotate == Rotate.ABOUT_CENTER) {
			g2d.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON
			);
			g2d.setClip(
				x,
				y,
				this.getIconWidth(),
				this.getIconHeight()
			);
			g2d.translate(
				(this.getIconWidth()  - this.icon.getIconWidth())  / 2,
				(this.getIconHeight() - this.icon.getIconHeight()) / 2
			);
			g2d.rotate(
				Math.toRadians(this.degrees),
				x + cWidth,
				y + cHeight
			);
			this.icon.paintIcon(
				c,
				g2d,
				x,
				y
			);
		}
		g2d.dispose();
	}



}
