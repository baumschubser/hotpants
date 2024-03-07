/*
 * Copyright (C) 2011 OnlineCity
 * Licensed under the MIT license, which can be read at: http://www.opensource.org/licenses/mit-license.php
 */

package dk.onlinecity.qrr.image;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import dk.onlinecity.qrr.perspective.CoordinateTranslator;

public final class Thumbnail
{
	public static final int THRESHOLD = 0;
	public static final int COLOR = 1;

	private final int[] pixels;
	private final CoordinateTranslator ct;
	private final int w;
	private final int h;
	private int threshold = 0;
	private int type = COLOR;
	private Image image;

	public Thumbnail(int[] pixels, CoordinateTranslator ct, int w, int h)
	{
		this.pixels = pixels;
		this.ct = ct;
		this.w = w;
		this.h = h;
	}

	public int get(int x, int y)
	{
		if (type == THRESHOLD) {
			return ImageUtil.greyscale(pixels[x + y * w]) <= threshold ? 0 : 0xffffff;
		}

		return pixels[x + y * w];
	}

	public Image getImage()
	{
		if (image == null) {
			image = Image.createImage(w, h);
			Graphics g = image.getGraphics();
			g.drawRGB(pixels, 0, w, 0, 0, w, h, false);
		}
		return image;
	}

	public final void setType(int type)
	{
		this.type = type;
	}

	public final void setThreshold(int threshold)
	{
		this.threshold = threshold;
	}

	public final int[] translateCoordinate(int x, int y)
	{
		return ct.get(x, y);
	}

	public final int[] translateCoordinate(int[] a)
	{
		return ct.get(a);
	}

	public final int getWidth()
	{
		return w;
	}

	public final int getHeight()
	{
		return h;
	}

	public final int[] getPixels()
	{
		if (type == THRESHOLD) {
			int l = pixels.length;
			int[] tPixels = new int[l];
			for (int i = 0; i < l; i++) {
				tPixels[i] = ImageUtil.greyscale(pixels[i]) <= threshold ? 0 : 0xffffff;
			}
			return tPixels;
		}

		return pixels;
	}
}
