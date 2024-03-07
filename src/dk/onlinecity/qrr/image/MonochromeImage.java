/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.image;

import dk.onlinecity.qrr.core.exceptions.QrrException;

/**
 * This class implements a monochrome image.
 * 
 * @author Thomas Duerlund (td@onlinecity.dk)
 */
public class MonochromeImage
{
	/** Monochrome image pixels. */
	private int[] pixels;
	/** Image width. */
	private int w;
	/** Image height. */
	private int h;
	/** Threshold value. */
	private int threshold;

	/**
	 * Constructs a new monochrome image from an RGB image.
	 * 
	 * @param rgb
	 *            RGB pixels.
	 * @param w
	 *            Image width.
	 * @param h
	 *            Image height.
	 */
	MonochromeImage(int[] rgb, int w, int h)
	{
		this.pixels = rgb;
		this.w = w;
		this.h = h;
	}

	public MonochromeImage(int[] pixels, int w, int h, int tw, int th, int gx, int gy)
	{
		this.pixels = pixels;
		this.w = w;
		this.h = h;
	}

	/**
	 * Sets the threshold value.
	 * 
	 * @param threshold
	 *            Threshold value.
	 */
	public void setThreshold(int threshold)
	{
		this.threshold = threshold;
	}

	public final int getThreshold()
	{
		return threshold;
	}

	/**
	 * Returns a pixel as a boolean.
	 * 
	 * @param x
	 *            X coordinate.
	 * @param y
	 *            Y coordinate.
	 * @return Pixel as a boolean.
	 */
	public boolean get(int x, int y)
	{
		if (0 <= x && x < w && 0 <= y && y < h) {
			return pixels[x + y * w] <= threshold;
		}
		throw new QrrException();
	}

	public boolean get(int[] p)
	{
		if (0 <= p[0] && p[0] < w && 0 <= p[1] && p[1] < h) {
			return pixels[p[0] + p[1] * w] <= threshold;
		}

		return false;
	}

	public int get(int i)
	{
		return pixels[i];
	}

	/**
	 * Returns a pixel as an integer value.
	 * 
	 * @param x
	 *            X coordinate.
	 * @param y
	 *            Y coordinate.
	 * @return Pixel value.
	 */
	public int getInt(int x, int y)
	{
		return (0 <= x && x < w && 0 <= y && y < h) ? pixels[x + y * w] : 0;
	}

	/**
	 * Returns the image width.
	 * 
	 * @return Image width.
	 */
	public int getWidth()
	{
		return w;
	}

	/**
	 * Returns the image height.
	 * 
	 * @return Image height.
	 */
	public int getHeight()
	{
		return h;
	}
}
